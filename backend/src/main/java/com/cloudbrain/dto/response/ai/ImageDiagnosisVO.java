package com.cloudbrain.dto.response.ai;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** AI 影像诊断响应（预留接口） */
@Data
@Builder
public class ImageDiagnosisVO {

    private String diagnosisId;
    private String imageId;
    private Integer diagnosisType;
    private String diagnosisTypeName;
    private Findings findings;
    private BigDecimal confidenceScore;
    private Integer status;
    private String aiModel;

    @Data
    @Builder
    public static class Findings {
        private List<AbnormalRegion> abnormalRegions;
        private String summaryDetail;
    }

    @Data
    @Builder
    public static class AbnormalRegion {
        private String location;
        private String size;
        private String description;
        private BigDecimal confidence;
    }

    /** AI 服务不可用时的降级响应 */
    public static ImageDiagnosisVO fallback(String imageId) {
        return ImageDiagnosisVO.builder()
                .diagnosisId("FALLBACK")
                .imageId(imageId)
                .diagnosisTypeName("影像诊断")
                .findings(Findings.builder()
                        .summaryDetail("AI 服务暂时不可用，请人工阅片")
                        .build())
                .confidenceScore(BigDecimal.ZERO)
                .status(3)
                .aiModel("fallback")
                .build();
    }
}
