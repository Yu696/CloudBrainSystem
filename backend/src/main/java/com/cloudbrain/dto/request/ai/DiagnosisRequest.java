package com.cloudbrain.dto.request.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/** AI-05/06 AI 诊断分析请求 */
@Data
public class DiagnosisRequest {

    @NotBlank(message = "患者ID不能为空")
    private String patientId;

    @NotBlank(message = "医生ID不能为空")
    private String doctorId;

    /** 诊断类型: 0=症状分析 1=影像诊断 */
    @NotNull(message = "诊断类型不能为空")
    private Integer diagnosisType;

    /** 症状数据（chiefComplaint, presentIllness, physicalExam, auxiliaryExam 等） */
    private Map<String, Object> symptomData;
}
