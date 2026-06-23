package com.cloudbrain.service.appointment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.DoctorUpdateRequest;
import com.cloudbrain.dto.response.DoctorVO;
import com.cloudbrain.entity.Department;
import com.cloudbrain.entity.Doctor;
import com.cloudbrain.entity.User;
import com.cloudbrain.mapper.DepartmentMapper;
import com.cloudbrain.mapper.DoctorMapper;
import com.cloudbrain.mapper.UserMapper;
import com.cloudbrain.service.appointment.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    public List<DoctorVO> listByDepartment(String departmentId) {
        List<Doctor> doctors = this.list(new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getDepartmentId, departmentId)
                .eq(Doctor::getAvailable, 1));
        return doctors.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<DoctorVO> listAll() {
        List<Doctor> doctors = this.list(new LambdaQueryWrapper<Doctor>().eq(Doctor::getAvailable, 1));
        return doctors.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<DoctorVO> adminListAll() {
        List<Doctor> doctors = this.list();
        return doctors.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<DoctorVO> adminListByDepartment(String departmentId) {
        List<Doctor> doctors = this.list(new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getDepartmentId, departmentId));
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

    @Override
    @Transactional
    public void updateDoctor(String doctorId, DoctorUpdateRequest request) {
        Doctor doc = this.getOne(new LambdaQueryWrapper<Doctor>().eq(Doctor::getDoctorId, doctorId));
        if (doc == null) {
            throw new BusinessException("医生不存在");
        }
        if (request.getDepartmentId() != null) doc.setDepartmentId(request.getDepartmentId());
        if (request.getTitle() != null) doc.setTitle(request.getTitle());
        if (request.getSpecialty() != null) doc.setSpecialty(request.getSpecialty());
        if (request.getIntroduction() != null) doc.setIntroduction(request.getIntroduction());
        if (request.getConsultationFee() != null) doc.setConsultationFee(request.getConsultationFee());
        if (request.getMaxDailyPatients() != null) doc.setMaxDailyPatients(request.getMaxDailyPatients());
        this.updateById(doc);
    }

    @Override
    @Transactional
    public void toggleAvailable(String doctorId) {
        Doctor doc = this.getOne(new LambdaQueryWrapper<Doctor>().eq(Doctor::getDoctorId, doctorId));
        if (doc == null) {
            throw new BusinessException("医生不存在");
        }
        doc.setAvailable(doc.getAvailable() == 1 ? 0 : 1);
        this.updateById(doc);
    }

    private DoctorVO toVO(Doctor doc) {
        String realName = null;
        String departmentName = null;
        if (doc.getUserId() != null) {
            User user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUserId, doc.getUserId()));
            if (user != null) {
                realName = user.getRealName();
            }
        }
        if (doc.getDepartmentId() != null) {
            Department dept = departmentMapper.selectOne(
                    new LambdaQueryWrapper<Department>().eq(Department::getDepartmentId, doc.getDepartmentId()));
            if (dept != null) {
                departmentName = dept.getName();
            }
        }
        return DoctorVO.builder()
                .doctorId(doc.getDoctorId())
                .userId(doc.getUserId())
                .departmentId(doc.getDepartmentId())
                .departmentName(departmentName)
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
