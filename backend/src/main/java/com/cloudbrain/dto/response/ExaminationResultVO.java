package com.cloudbrain.dto.response;

import com.cloudbrain.entity.ExaminationResult;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ExaminationResultVO {

    private String resultId;
    private String orderId;
    private String resultData;
    private String referenceRange;
    private Integer isAbnormal;
    private String aiAnalysis;
    private String doctorOpinion;
    private String reportFileUrl;
    private LocalDateTime resultTime;
    private LocalDateTime createTime;

    public static ExaminationResultVO from(ExaminationResult r) {
        return ExaminationResultVO.builder()
                .resultId(r.getResultId())
                .orderId(r.getOrderId())
                .resultData(r.getResultData())
                .referenceRange(r.getReferenceRange())
                .isAbnormal(r.getIsAbnormal())
                .aiAnalysis(r.getAiAnalysis())
                .doctorOpinion(r.getDoctorOpinion())
                .reportFileUrl(r.getReportFileUrl())
                .resultTime(r.getResultTime())
                .createTime(r.getCreateTime())
                .build();
    }
}
