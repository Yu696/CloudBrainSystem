package com.cloudbrain.service.appointment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.DepartmentCreateRequest;
import com.cloudbrain.entity.Department;
import com.cloudbrain.mapper.DepartmentMapper;
import com.cloudbrain.service.appointment.DepartmentService;
import com.cloudbrain.util.UUIDUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Override
    public List<Department> listAll() {
        return this.list(new LambdaQueryWrapper<Department>()
                .orderByAsc(Department::getSortOrder));
    }

    @Override
    public List<Department> listByCategory(String category) {
        return this.list(new LambdaQueryWrapper<Department>()
                .eq(Department::getCategory, category)
                .orderByAsc(Department::getSortOrder));
    }

    @Override
    public Department getByDepartmentId(String departmentId) {
        Department dept = this.getOne(new LambdaQueryWrapper<Department>().eq(Department::getDepartmentId, departmentId));
        if (dept == null) {
            throw new BusinessException("科室不存在");
        }
        return dept;
    }

    @Override
    @Transactional
    public Department create(DepartmentCreateRequest request) {
        Department dept = new Department();
        dept.setDepartmentId(UUIDUtil.generateDeptId());
        dept.setName(request.getName());
        dept.setParentId(request.getParentId());
        dept.setCategory(request.getCategory());
        dept.setDescription(request.getDescription());
        dept.setLocation(request.getLocation());
        dept.setPhone(request.getPhone());
        dept.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        dept.setStatus(1);
        save(dept);
        return dept;
    }

    @Override
    @Transactional
    public void update(String departmentId, DepartmentCreateRequest request) {
        Department dept = getByDepartmentId(departmentId);
        if (request.getName() != null) dept.setName(request.getName());
        if (request.getParentId() != null) dept.setParentId(request.getParentId());
        if (request.getCategory() != null) dept.setCategory(request.getCategory());
        if (request.getDescription() != null) dept.setDescription(request.getDescription());
        if (request.getLocation() != null) dept.setLocation(request.getLocation());
        if (request.getPhone() != null) dept.setPhone(request.getPhone());
        if (request.getSortOrder() != null) dept.setSortOrder(request.getSortOrder());
        updateById(dept);
    }

    @Override
    @Transactional
    public void delete(String departmentId) {
        Department dept = getByDepartmentId(departmentId);
        removeById(dept.getId());
    }
}
