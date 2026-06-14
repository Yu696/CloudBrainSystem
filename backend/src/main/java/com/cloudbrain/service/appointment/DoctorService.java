package com.cloudbrain.service.appointment;

import com.cloudbrain.entity.Doctor;
import java.util.List;

public interface DoctorService {
    List<Doctor> listByDepartment(String departmentId);
    List<Doctor> listAll();
    Doctor getDetail(String doctorId);
}
