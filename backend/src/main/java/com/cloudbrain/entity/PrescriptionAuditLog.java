package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("prescription_audit_log")
public class PrescriptionAuditLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("audit_id")
    private String auditId;

    @TableField("prescription_id")
    private String prescriptionId;

    @TableField("patient_id")
    private String patientId;

    @TableField("audit_type")
    private Integer auditType;

    @TableField("risk_level")
    private Integer riskLevel = 0;

    @TableField("audit_result")
    private String auditResult;

    @TableField("drug_interactions")
    private String drugInteractions;

    private String suggestions;

    @TableField("audit_time")
    private LocalDateTime auditTime;
}
