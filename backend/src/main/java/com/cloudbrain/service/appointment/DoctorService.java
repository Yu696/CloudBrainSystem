package com.cloudbrain.service.appointment;

import com.cloudbrain.dto.response.DoctorVO;
import java.util.List;

public interface DoctorService {
    List<DoctorVO> listByDepartment(String departmentId);
    List<DoctorVO> listAll();
    DoctorVO getDetail(String doctorId);
}
