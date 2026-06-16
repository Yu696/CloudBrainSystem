package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("appointment")
public class Appointment {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("appointment_id")
    private String appointmentId;

    @TableField("patient_id")
    private String patientId;

    @TableField("doctor_id")
    private String doctorId;

    @TableField("department_id")
    private String departmentId;

    @TableField("schedule_id")
    private String scheduleId;

    @TableField("time_slot_id")
    private String timeSlotId;

    @TableField("appointment_date")
    private LocalDate appointmentDate;

    @TableField("time_slot_desc")
    private String timeSlotDesc;

    @TableField("appointment_type")
    private Integer appointmentType;

    private String symptoms;

    private Integer source;

    private Integer status = 0;

    @TableField("payment_status")
    private Integer paymentStatus = 0;

    @TableField("total_fee")
    private BigDecimal totalFee = BigDecimal.ZERO;

    @TableField("cancel_reason")
    private String cancelReason;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
