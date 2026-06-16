package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("payment_id")
    private String paymentId;

    @TableField("appointment_id")
    private String appointmentId;

    @TableField("patient_id")
    private String patientId;

    private BigDecimal amount;

    @TableField("payment_method")
    private Integer paymentMethod;

    @TableField("payment_status")
    private Integer paymentStatus = 0;

    @TableField("trade_no")
    private String tradeNo;

    @TableField("payment_time")
    private LocalDateTime paymentTime;

    @TableField("refund_time")
    private LocalDateTime refundTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
