package com.cloudbrain.service.appointment;

import com.cloudbrain.entity.Department;
import java.util.List;

public interface DepartmentService {
    List<Department> listAll();
    List<Department> listByCategory(String category);
    Department getByDepartmentId(String departmentId);
}
