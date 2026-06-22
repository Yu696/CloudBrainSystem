package com.cloudbrain.service.medical.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.MedicalRecordCreateRequest;
import com.cloudbrain.dto.request.MedicalRecordUpdateRequest;
import com.cloudbrain.dto.response.MedicalRecordVO;
import com.cloudbrain.entity.MedicalRecord;
import com.cloudbrain.entity.Appointment;
import com.cloudbrain.entity.Doctor;
import com.cloudbrain.entity.Patient;
import com.cloudbrain.entity.User;
import com.cloudbrain.mapper.AppointmentMapper;
import com.cloudbrain.mapper.DoctorMapper;
import com.cloudbrain.mapper.MedicalRecordMapper;
import com.cloudbrain.mapper.PatientMapper;
import com.cloudbrain.mapper.UserMapper;
import com.cloudbrain.service.medical.MedicalRecordService;
import com.cloudbrain.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl extends ServiceImpl<MedicalRecordMapper, MedicalRecord> implements MedicalRecordService {

    private final AppointmentMapper appointmentMapper;
    private final PatientMapper patientMapper;
    private final DoctorMapper doctorMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public MedicalRecordVO createRecord(MedicalRecordCreateRequest request) {
        MedicalRecord record = new MedicalRecord();
        record.setRecordId(UUIDUtil.generateRecordId());
        record.setPatientId(request.getPatientId());
        record.setDoctorId(request.getDoctorId());
        record.setAppointmentId(request.getAppointmentId());
        record.setChiefComplaint(request.getChiefComplaint());
        record.setPresentIllness(request.getPresentIllness());
        record.setPastHistory(request.getPastHistory());
        record.setPersonalHistory(request.getPersonalHistory());
        record.setFamilyHistory(request.getFamilyHistory());
        record.setPhysicalExam(request.getPhysicalExam());
        record.setAuxiliaryExam(request.getAuxiliaryExam());
        record.setDiagnosis(request.getDiagnosis());
        record.setTreatmentOpinion(request.getTreatmentOpinion());
        record.setStatus(0); // 草稿
        record.setIsAiGenerated(0);
        save(record);

        return MedicalRecordVO.from(record);
    }

    @Override
    public List<MedicalRecordVO> listRecords(String patientId, String doctorId) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<MedicalRecord>()
                .eq(patientId != null && !patientId.isEmpty(), MedicalRecord::getPatientId, patientId)
                .eq(doctorId != null && !doctorId.isEmpty(), MedicalRecord::getDoctorId, doctorId)
                .orderByDesc(MedicalRecord::getCreateTime);

        return this.list(wrapper).stream()
                .map(this::enrichNames)
                .toList();
    }

    private MedicalRecordVO enrichNames(MedicalRecord r) {
        MedicalRecordVO vo = MedicalRecordVO.from(r);

        if (r.getPatientId() != null) {
            Patient p = patientMapper.selectOne(
                    new LambdaQueryWrapper<Patient>().eq(Patient::getPatientId, r.getPatientId()));
            if (p != null) vo.setPatientName(p.getName());
        }
        if (r.getDoctorId() != null) {
            Doctor d = doctorMapper.selectOne(
                    new LambdaQueryWrapper<Doctor>().eq(Doctor::getDoctorId, r.getDoctorId()));
            if (d != null && d.getUserId() != null) {
                User u = userMapper.selectOne(
                        new LambdaQueryWrapper<User>().eq(User::getUserId, d.getUserId()));
                if (u != null) vo.setDoctorName(u.getRealName());
            }
        }
        return vo;
    }

    @Override
    public MedicalRecordVO getRecordDetail(String recordId) {
        MedicalRecord record = this.getOne(new LambdaQueryWrapper<MedicalRecord>()
                .eq(MedicalRecord::getRecordId, recordId));
        if (record == null) {
            throw new BusinessException("病历不存在");
        }
        return MedicalRecordVO.from(record);
    }

    @Override
    @Transactional
    public void updateRecord(MedicalRecordUpdateRequest request) {
        MedicalRecord record = this.getOne(new LambdaQueryWrapper<MedicalRecord>()
                .eq(MedicalRecord::getRecordId, request.getRecordId()));
        if (record == null) {
            throw new BusinessException("病历不存在");
        }

        if (request.getChiefComplaint() != null) record.setChiefComplaint(request.getChiefComplaint());
        if (request.getPresentIllness() != null) record.setPresentIllness(request.getPresentIllness());
        if (request.getPastHistory() != null) record.setPastHistory(request.getPastHistory());
        if (request.getPersonalHistory() != null) record.setPersonalHistory(request.getPersonalHistory());
        if (request.getFamilyHistory() != null) record.setFamilyHistory(request.getFamilyHistory());
        if (request.getPhysicalExam() != null) record.setPhysicalExam(request.getPhysicalExam());
        if (request.getAuxiliaryExam() != null) record.setAuxiliaryExam(request.getAuxiliaryExam());
        if (request.getDiagnosis() != null) record.setDiagnosis(request.getDiagnosis());
        if (request.getTreatmentOpinion() != null) record.setTreatmentOpinion(request.getTreatmentOpinion());

        updateById(record);
    }

    @Override
    @Transactional
    public void completeRecord(String recordId) {
        MedicalRecord record = this.getOne(new LambdaQueryWrapper<MedicalRecord>()
                .eq(MedicalRecord::getRecordId, recordId));
        if (record == null) {
            throw new BusinessException("病历不存在");
        }
        record.setStatus(1); // 已完成
        record.setDiagnosisTime(LocalDateTime.now());
        updateById(record);

        // 同步更新关联预约状态为已完成
        if (record.getAppointmentId() != null && !record.getAppointmentId().isEmpty()) {
            Appointment apt = appointmentMapper.selectOne(new LambdaQueryWrapper<Appointment>()
                    .eq(Appointment::getAppointmentId, record.getAppointmentId()));
            if (apt != null) {
                apt.setStatus(2); // 已完成
                appointmentMapper.updateById(apt);
            }
        }
    }

    @Override
    @Transactional
    public void deleteRecord(String recordId) {
        MedicalRecord record = this.getOne(new LambdaQueryWrapper<MedicalRecord>()
                .eq(MedicalRecord::getRecordId, recordId));
        if (record == null) {
            throw new BusinessException("病历不存在");
        }
        this.removeById(record.getId());
    }
}
