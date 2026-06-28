package com.cloudbrain.dto.response.ai;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** AI-01/02/03 智能分诊响应 */
@Data
@Builder
public class TriageVO {

    private String triageId;
    private String chiefComplaint;

    private DepartmentInfo recommendedDepartment;
    private List<DepartmentInfo> alternativeDepartments;
    private List<DiseaseMatch> diseaseMatches;
    private List<DoctorInfo> recommendedDoctors;

    private BigDecimal confidenceScore;
    private Boolean needsManualReview;
    private String analysisDetail;
    private String aiModel;
    private Integer responseTimeMs;

    @Data
    @Builder
    public static class DepartmentInfo {
        private String departmentId;
        private String departmentName;
        private BigDecimal confidence;
    }

    @Data
    @Builder
    public static class DoctorInfo {
        private String doctorId;
        private String doctorName;
        private String title;
        private String departmentName;
        private BigDecimal matchScore;
    }

    @Data
    @Builder
    public static class DiseaseMatch {
        private String diseaseName;
        private String icdCode;
        private BigDecimal confidence;
        private List<String> matchedSymptoms;
    }

    /** AI 服务不可用时的降级响应 */
    public static TriageVO fallback(String chiefComplaint) {
        return TriageVO.builder()
                .triageId("FALLBACK")
                .chiefComplaint(chiefComplaint)
                .recommendedDepartment(DepartmentInfo.builder()
                        .departmentId("FALLBACK")
                        .departmentName("全科医学科")
                        .confidence(BigDecimal.ZERO)
                        .build())
                .confidenceScore(BigDecimal.ZERO)
                .needsManualReview(true)
                .analysisDetail("AI 服务暂时不可用，建议人工分诊")
                .aiModel("fallback")
                .build();
    }
}
