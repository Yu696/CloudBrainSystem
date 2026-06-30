package com.cloudbrain.dto.response;

import com.cloudbrain.entity.ExaminationOrder;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ExaminationOrderVO {

    private String orderId;
    private String recordId;
    private String patientId;
    private String patientName;
    private String doctorId;
    private Integer examCategory;
    private String examName;
    private String examPurpose;
    private BigDecimal amount;
    private Integer status;
    private Integer isAiRecommended;
    private LocalDateTime createTime;

    public static ExaminationOrderVO from(ExaminationOrder o) {
        return ExaminationOrderVO.builder()
                .orderId(o.getOrderId())
                .recordId(o.getRecordId())
                .patientId(o.getPatientId())
                .patientName(null)
                .doctorId(o.getDoctorId())
                .examCategory(o.getExamCategory())
                .examName(o.getExamName())
                .examPurpose(o.getExamPurpose())
                .amount(o.getAmount())
                .status(o.getStatus())
                .isAiRecommended(o.getIsAiRecommended())
                .createTime(o.getCreateTime())
                .build();
    }
}
