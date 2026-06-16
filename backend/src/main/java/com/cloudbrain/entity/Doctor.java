package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("doctor")
public class Doctor {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("doctor_id")
    private String doctorId;

    @TableField("user_id")
    private String userId;

    @TableField("department_id")
    private String departmentId;

    private String title;

    private String specialty;

    private String introduction;

    @TableField("consultation_fee")
    private BigDecimal consultationFee = BigDecimal.ZERO;

    @TableField("max_daily_patients")
    private Integer maxDailyPatients = 30;

    private Integer available = 1;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
