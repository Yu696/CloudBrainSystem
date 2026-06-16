package com.cloudbrain.service.appointment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.entity.Department;
import com.cloudbrain.mapper.DepartmentMapper;
import com.cloudbrain.service.appointment.DepartmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Override
    public List<Department> listAll() {
        return this.list();
    }

    @Override
    public List<Department> listByCategory(String category) {
        return this.list(new LambdaQueryWrapper<Department>().eq(Department::getCategory, category));
    }

    @Override
    public Department getByDepartmentId(String departmentId) {
        Department dept = this.getOne(new LambdaQueryWrapper<Department>().eq(Department::getDepartmentId, departmentId));
        if (dept == null) {
            throw new BusinessException("科室不存在");
        }
        return dept;
    }
}
