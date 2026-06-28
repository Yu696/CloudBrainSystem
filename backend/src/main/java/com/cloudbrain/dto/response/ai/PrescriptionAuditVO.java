package com.cloudbrain.dto.response.ai;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** AI-08/09/10 处方审核响应 */
@Data
@Builder
public class PrescriptionAuditVO {

    private String auditId;
    private String overallResult;       // PASS / WARNING / REJECT / MANUAL
    private String overallResultName;   // 通过 / 注意 / 禁止使用 / 需人工审核

    private PatientContextSummary patientContext;
    private List<AuditItem> items;
    private String summary;
    private BigDecimal confidenceScore;
    private String aiModel;

    /** 审核结果常量 */
    public static final String PASS = "PASS";
    public static final String WARNING = "WARNING";
    public static final String REJECT = "REJECT";
    public static final String MANUAL = "MANUAL";

    @Data
    @Builder
    public static class PatientContextSummary {
        private String allergyHistory;
        private String medicalHistory;
        private List<String> currentMedications;
    }

    @Data
    @Builder
    public static class AuditItem {
        private String drugName;
        private String result;       // PASS / WARNING / REJECT
        private String resultName;   // 通过 / 注意 / 禁止使用
        private List<AuditCheck> checks;
    }

    @Data
    @Builder
    public static class AuditCheck {
        private String checkType;   // 过敏检测 / 药物相互作用 / 剂量检查 / 禁忌人群
        private String result;      // PASS / WARNING / REJECT
        private String detail;
    }

    /** AI 服务不可用时的降级响应 */
    public static PrescriptionAuditVO fallback() {
        return PrescriptionAuditVO.builder()
                .auditId("FALLBACK")
                .overallResult(MANUAL)
                .overallResultName("需人工审核")
                .summary("AI 服务暂时不可用，请人工审核处方")
                .confidenceScore(BigDecimal.ZERO)
                .aiModel("fallback")
                .build();
    }
}
