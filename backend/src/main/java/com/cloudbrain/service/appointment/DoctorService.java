package com.cloudbrain.service.appointment;

import com.cloudbrain.dto.request.DoctorUpdateRequest;
import com.cloudbrain.dto.response.DoctorVO;
import java.util.List;

public interface DoctorService {
    List<DoctorVO> listByDepartment(String departmentId);
    List<DoctorVO> listAll();
    List<DoctorVO> adminListAll();
    List<DoctorVO> adminListByDepartment(String departmentId);
    DoctorVO getDetail(String doctorId);
    DoctorVO getCurrentDoctor();
    void updateDoctor(String doctorId, DoctorUpdateRequest request);
    void toggleAvailable(String doctorId);
}
