package com.cloudbrain.service.appointment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.response.DoctorVO;
import com.cloudbrain.entity.Doctor;
import com.cloudbrain.entity.User;
import com.cloudbrain.mapper.DoctorMapper;
import com.cloudbrain.mapper.UserMapper;
import com.cloudbrain.service.appointment.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
    public DoctorVO getCurrentDoctor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("未登录");
        }
        User currentUser = (User) auth.getPrincipal();
        Doctor doc = this.getOne(new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getUserId, currentUser.getUserId()));
        if (doc == null) {
            throw new BusinessException("当前用户不是医生");
        }
        return toVO(doc);
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
