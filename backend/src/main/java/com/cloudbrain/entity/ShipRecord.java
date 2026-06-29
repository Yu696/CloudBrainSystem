package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ship_record")
public class ShipRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("record_id")
    private String recordId;

    @TableField("prescription_id")
    private String prescriptionId;

    @TableField("patient_id")
    private String patientId;

    @TableField("operator_id")
    private String operatorId;

    @TableField("ship_num")
    private Integer shipNum;

    @TableField("pay_amount")
    private BigDecimal payAmount;

    @TableField("ship_time")
    private LocalDateTime shipTime;

    @TableField("print_status")
    private Integer printStatus;

    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
