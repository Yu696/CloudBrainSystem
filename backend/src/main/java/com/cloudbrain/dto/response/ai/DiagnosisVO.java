package com.cloudbrain.dto.response.ai;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** AI-05/06/07 AI 诊断响应 */
@Data
@Builder
public class DiagnosisVO {

    private String diagnosisId;
    private Map<String, Object> symptomData;
    private List<DiseaseMatch> diseaseMatches;
    private List<DepartmentRecommendation> departmentRecommendations;
    private BigDecimal confidenceScore;
    private Boolean needsManualReview;
    private String analysisResult;
    private Integer status;
    private String aiModel;

    /** 诊断状态枚举 */
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_ANALYZING = 1;
    public static final int STATUS_COMPLETE = 2;
    public static final int STATUS_FAILED = 3;

    @Data
    @Builder
    public static class DiseaseMatch {
        private String diseaseName;
        private String icdCode;
        private BigDecimal confidence;
        private String diagnosisBasis;
        private List<String> differentialDiagnosis;
    }

    @Data
    @Builder
    public static class DepartmentRecommendation {
        private String departmentName;
        private BigDecimal confidence;
    }

    /** AI 服务不可用时的降级响应 */
    public static DiagnosisVO fallback() {
        return DiagnosisVO.builder()
                .diagnosisId("FALLBACK")
                .confidenceScore(BigDecimal.ZERO)
                .needsManualReview(true)
                .analysisResult("AI 服务暂时不可用，请人工判断")
                .status(STATUS_FAILED)
                .aiModel("fallback")
                .build();
    }
}
