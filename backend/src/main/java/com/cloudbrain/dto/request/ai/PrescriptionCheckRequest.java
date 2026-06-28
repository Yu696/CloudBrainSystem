package com.cloudbrain.dto.request.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/** AI-08/09/10 处方审核请求 */
@Data
public class PrescriptionCheckRequest {

    private String prescriptionId;

    @NotBlank(message = "病历ID不能为空")
    private String recordId;

    @NotBlank(message = "患者ID不能为空")
    private String patientId;

    @NotBlank(message = "医生ID不能为空")
    private String doctorId;

    /** 处方药品列表 */
    @NotEmpty(message = "处方药品不能为空")
    private List<DrugItem> items;

    @Data
    public static class DrugItem {
        @NotBlank private String drugId;
        @NotBlank private String drugName;
        private String dosage;
        private String frequency;
        private Integer days;
    }
}
