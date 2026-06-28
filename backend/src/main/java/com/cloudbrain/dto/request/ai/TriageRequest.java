package com.cloudbrain.dto.request.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/** AI-01/02/03 智能分诊请求 */
@Data
public class TriageRequest {

    @NotBlank(message = "患者ID不能为空")
    private String patientId;

    @NotBlank(message = "主诉不能为空")
    private String chiefComplaint;

    /** 附加信息（duration, otherSymptoms 等） */
    private Map<String, Object> additionalInfo;
}
