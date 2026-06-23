package com.cloudbrain.service.appointment;

import com.cloudbrain.dto.request.DepartmentCreateRequest;
import com.cloudbrain.entity.Department;
import java.util.List;

public interface DepartmentService {
    List<Department> listAll();
    List<Department> listByCategory(String category);
    Department getByDepartmentId(String departmentId);
    Department create(DepartmentCreateRequest request);
    void update(String departmentId, DepartmentCreateRequest request);
    void delete(String departmentId);
}
