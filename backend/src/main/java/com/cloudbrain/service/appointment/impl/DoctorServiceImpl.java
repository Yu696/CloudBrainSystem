package com.cloudbrain.service.appointment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.entity.Doctor;
import com.cloudbrain.mapper.DoctorMapper;
import com.cloudbrain.service.appointment.DoctorService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    @Override
    public List<Doctor> listByDepartment(String departmentId) {
        return this.list(new LambdaQueryWrapper<Doctor>().eq(Doctor::getDepartmentId, departmentId));
    }

    @Override
    public List<Doctor> listAll() {
        return this.list(new LambdaQueryWrapper<Doctor>().eq(Doctor::getAvailable, 1));
    }

    @Override
    public Doctor getDetail(String doctorId) {
        Doctor doc = this.getOne(new LambdaQueryWrapper<Doctor>().eq(Doctor::getDoctorId, doctorId));
        if (doc == null) {
            throw new BusinessException("医生不存在");
        }
        return doc;
    }
}
