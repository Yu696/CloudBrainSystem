package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("prescription")
public class Prescription {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("prescription_id")
    private String prescriptionId;

    @TableField("record_id")
    private String recordId;

    @TableField("patient_id")
    private String patientId;

    @TableField("doctor_id")
    private String doctorId;

    @TableField("prescription_desc")
    private String prescriptionDesc;

    @TableField("total_amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    private Integer status = 0;

    @TableField("audit_status")
    private Integer auditStatus = 0;

    @TableField("audit_id")
    private String auditId;

    @TableField("diagnosis_at")
    private LocalDateTime diagnosisAt;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
