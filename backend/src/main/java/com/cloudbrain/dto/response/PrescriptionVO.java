package com.cloudbrain.dto.response;

import com.cloudbrain.entity.Prescription;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PrescriptionVO {

    private String prescriptionId;
    private String recordId;
    private String patientId;
    private String doctorId;
    private String prescriptionDesc;
    private BigDecimal totalAmount;
    private Integer status;
    private Integer auditStatus;
    private LocalDateTime diagnosisAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<PrescriptionItemVO> items;

    public static PrescriptionVO from(Prescription p) {
        return PrescriptionVO.builder()
                .prescriptionId(p.getPrescriptionId())
                .recordId(p.getRecordId())
                .patientId(p.getPatientId())
                .doctorId(p.getDoctorId())
                .prescriptionDesc(p.getPrescriptionDesc())
                .totalAmount(p.getTotalAmount())
                .status(p.getStatus())
                .auditStatus(p.getAuditStatus())
                .diagnosisAt(p.getDiagnosisAt())
                .createTime(p.getCreateTime())
                .updateTime(p.getUpdateTime())
                .build();
    }
}
