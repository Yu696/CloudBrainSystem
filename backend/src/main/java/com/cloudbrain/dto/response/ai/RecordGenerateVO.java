package com.cloudbrain.dto.response.ai;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** AI-11 病历生成响应 */
@Data
@Builder
public class RecordGenerateVO {

    private String generationId;
    private RecordPreview recordPreview;
    private Boolean isConfirmed;
    private String aiModel;
    private Integer responseTimeMs;

    @Data
    @Builder
    public static class RecordPreview {
        private String chiefComplaint;
        private String presentIllness;
        private String pastHistory;
        private String physicalExam;
        private String preliminaryDiagnosis;
        private String treatmentPlan;
    }

    /** AI 服务不可用时的降级响应 */
    public static RecordGenerateVO fallback() {
        return RecordGenerateVO.builder()
                .generationId("FALLBACK")
                .isConfirmed(false)
                .aiModel("fallback")
                .build();
    }
}
