package com.cloudbrain.dto.request.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** AI-11 病历生成请求 */
@Data
public class RecordGenerateRequest {

    @NotBlank(message = "患者ID不能为空")
    private String patientId;

    @NotBlank(message = "医生ID不能为空")
    private String doctorId;

    /** 关联预约ID（可选） */
    private String appointmentId;

    @NotBlank(message = "对话文本不能为空")
    private String dialogueText;

    /** AI-13 病历确认归档请求体 */
    @Data
    public static class ConfirmBody {
        private Object recordPreview;
        private String recordId;
    }
}
