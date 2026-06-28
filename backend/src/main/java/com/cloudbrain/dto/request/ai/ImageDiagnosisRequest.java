package com.cloudbrain.dto.request.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** AI 影像诊断请求（预留接口） */
@Data
public class ImageDiagnosisRequest {

    @NotBlank(message = "影像ID不能为空")
    private String imageId;

    @NotNull(message = "诊断类型不能为空")
    private Integer diagnosisType;

    @NotBlank(message = "患者ID不能为空")
    private String patientId;

    @NotBlank(message = "医生ID不能为空")
    private String doctorId;
}
