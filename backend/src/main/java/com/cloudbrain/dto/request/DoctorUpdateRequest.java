package com.cloudbrain.dto.request;

import lombok.Data;

@Data
public class DoctorUpdateRequest {
    private String doctorId;
    private String userId;        // 按 userId 查找（医生自编辑时用）
    private String specialty;
    private String introduction;
}
