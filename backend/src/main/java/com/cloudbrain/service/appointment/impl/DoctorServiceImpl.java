package com.cloudbrain.service.appointment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.DoctorUpdateRequest;
import com.cloudbrain.dto.response.DoctorVO;
import com.cloudbrain.entity.Doctor;
import com.cloudbrain.entity.User;
import com.cloudbrain.mapper.DoctorMapper;
import com.cloudbrain.mapper.UserMapper;
import com.cloudbrain.service.appointment.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    private final UserMapper userMapper;

    @Override
    public List<DoctorVO> listByDepartment(String departmentId) {
        List<Doctor> doctors = this.list(new LambdaQueryWrapper<Doctor>().eq(Doctor::getDepartmentId, departmentId));
        return doctors.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<DoctorVO> listAll() {
        List<Doctor> doctors = this.list(new LambdaQueryWrapper<Doctor>().eq(Doctor::getAvailable, 1));
        return doctors.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public DoctorVO getDetail(String doctorId) {
        Doctor doc = this.getOne(new LambdaQueryWrapper<Doctor>().eq(Doctor::getDoctorId, doctorId));
        if (doc == null) {
            throw new BusinessException("医生不存在");
        }
        return toVO(doc);
    }

    @Override
    public DoctorVO getByUserId(String userId) {
        Doctor doc = this.getOne(new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
        if (doc == null) {
            throw new BusinessException("医生不存在");
        }
        return toVO(doc);
    }

    @Override
    public void updateDoctor(DoctorUpdateRequest request) {
        Doctor doctor;
        if (request.getDoctorId() != null) {
            doctor = this.getOne(new LambdaQueryWrapper<Doctor>().eq(Doctor::getDoctorId, request.getDoctorId()));
        } else if (request.getUserId() != null) {
            doctor = this.getOne(new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, request.getUserId()));
        } else {
            throw new BusinessException("医生ID或用户ID不能为空");
        }
        if (doctor == null) {
            throw new BusinessException("医生不存在");
        }
        if (request.getSpecialty() != null) {
            doctor.setSpecialty(request.getSpecialty());
        }
        if (request.getIntroduction() != null) {
            doctor.setIntroduction(request.getIntroduction());
        }
        this.updateById(doctor);
    }

    private DoctorVO toVO(Doctor doc) {
        String realName = null;
        if (doc.getUserId() != null) {
            User user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUserId, doc.getUserId()));
            if (user != null) {
                realName = user.getRealName();
            }
        }
        return DoctorVO.builder()
                .doctorId(doc.getDoctorId())
                .userId(doc.getUserId())
                .departmentId(doc.getDepartmentId())
                .realName(realName)
                .title(doc.getTitle())
                .specialty(doc.getSpecialty())
                .introduction(doc.getIntroduction())
                .consultationFee(doc.getConsultationFee())
                .maxDailyPatients(doc.getMaxDailyPatients())
                .available(doc.getAvailable())
                .build();
    }
}
