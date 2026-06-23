package com.cloudbrain.service.patient.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.PatientCreateRequest;
import com.cloudbrain.dto.request.PatientUpdateRequest;
import com.cloudbrain.dto.response.PatientCreateVO;
import com.cloudbrain.dto.response.PatientInfoVO;
import com.cloudbrain.entity.Appointment;
import com.cloudbrain.entity.Patient;
import com.cloudbrain.mapper.AppointmentMapper;
import com.cloudbrain.mapper.PatientMapper;
import com.cloudbrain.service.patient.PatientService;
import com.cloudbrain.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class  PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

    private final AppointmentMapper appointmentMapper;

    @Override
    @Transactional
    public PatientCreateVO createPatient(PatientCreateRequest request) {
        // 校验身份证唯一性
        if (lambdaQuery().eq(Patient::getIdCard, request.getIdCard()).count() > 0) {
            throw new BusinessException("该身份证号已存在，请检查后重试");
        }
        // 校验手机号唯一性
        if (lambdaQuery().eq(Patient::getPhone, request.getPhone()).count() > 0) {
            throw new BusinessException("该手机号已被其他用户绑定，请更换手机号");
        }

        Patient patient = new Patient();
        patient.setPatientId(UUIDUtil.generatePatientId());
        patient.setMedicalRecordNo(UUIDUtil.generateMedicalRecordNo());
        patient.setName(request.getName());
        patient.setIdCard(request.getIdCard());
        patient.setGender(request.getGender());
        patient.setBirthDate(request.getBirthDate());
        patient.setPhone(request.getPhone());
        patient.setEmergencyPhone(request.getEmergencyPhone());
        patient.setAddress(request.getAddress());
        patient.setBloodType(request.getBloodType());
        patient.setAllergyHistory(request.getAllergyHistory());
        patient.setGeneticDiseases(request.getGeneticDiseases());
        patient.setMedicalHistory(request.getMedicalHistory());
        patient.setSource(request.getSource() != null ? request.getSource() : 1);
        patient.setStatus(1);
        save(patient);

        return new PatientCreateVO(patient.getPatientId(), patient.getMedicalRecordNo());
    }

    @Override
    public PatientInfoVO getPatientInfo(String patientId) {
        Patient patient = this.getOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getPatientId, patientId));
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }
        return PatientInfoVO.from(patient);
    }

    @Override
    public List<PatientInfoVO> listPatients(String name, String phone, String medicalRecordNo) {
        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<Patient>()
                .eq(name != null && !name.isEmpty(), Patient::getName, name)
                .eq(phone != null && !phone.isEmpty(), Patient::getPhone, phone)
                .eq(medicalRecordNo != null && !medicalRecordNo.isEmpty(), Patient::getMedicalRecordNo, medicalRecordNo)
                .orderByDesc(Patient::getCreateTime);

        return this.list(wrapper).stream()
                .map(PatientInfoVO::from)
                .toList();
    }

    @Override
    @Transactional
    public void updatePatient(PatientUpdateRequest request) {
        Patient patient = this.getOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getPatientId, request.getPatientId()));
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }

        // 校验唯一字段是否与其他患者冲突
        if (request.getPhone() != null && !request.getPhone().equals(patient.getPhone())) {
            if (lambdaQuery().eq(Patient::getPhone, request.getPhone()).count() > 0) {
                throw new BusinessException("该手机号已被其他用户绑定，请更换手机号");
            }
            patient.setPhone(request.getPhone());
        }

        if (request.getName() != null) patient.setName(request.getName());
        if (request.getGender() != null) patient.setGender(request.getGender());
        if (request.getBirthDate() != null) patient.setBirthDate(request.getBirthDate());
        if (request.getEmergencyPhone() != null) patient.setEmergencyPhone(request.getEmergencyPhone());
        if (request.getAddress() != null) patient.setAddress(request.getAddress());
        if (request.getBloodType() != null) patient.setBloodType(request.getBloodType());
        if (request.getAllergyHistory() != null) patient.setAllergyHistory(request.getAllergyHistory());
        if (request.getGeneticDiseases() != null) patient.setGeneticDiseases(request.getGeneticDiseases());
        if (request.getMedicalHistory() != null) patient.setMedicalHistory(request.getMedicalHistory());
        if (request.getStatus() != null) patient.setStatus(request.getStatus());

        updateById(patient);
    }

    @Override
    public boolean checkIdCard(String idCard) {
        Long count = lambdaQuery().eq(Patient::getIdCard, idCard).count();
        return count > 0;
    }

    @Override
    public PatientInfoVO findByUserId(String userId) {
        Patient patient = this.getOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getUserId, userId));
        if (patient == null) {
            throw new BusinessException("未找到关联的患者档案");
        }
        return PatientInfoVO.from(patient);
    }

    @Override
    public List<PatientInfoVO> listPatientsByDoctor(String doctorId, String name, String phone, String medicalRecordNo) {
        // 查询该医生的所有预约记录，获取去重后的 patientId 列表
        List<String> patientIds = appointmentMapper.selectList(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getDoctorId, doctorId)
                        .select(Appointment::getPatientId)
        ).stream().map(Appointment::getPatientId).distinct().toList();

        if (patientIds.isEmpty()) {
            return List.of();
        }

        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<Patient>()
                .in(Patient::getPatientId, patientIds)
                .eq(name != null && !name.isEmpty(), Patient::getName, name)
                .eq(phone != null && !phone.isEmpty(), Patient::getPhone, phone)
                .eq(medicalRecordNo != null && !medicalRecordNo.isEmpty(), Patient::getMedicalRecordNo, medicalRecordNo)
                .orderByDesc(Patient::getCreateTime);

        return this.list(wrapper).stream()
                .map(PatientInfoVO::from)
                .toList();
    }
}
