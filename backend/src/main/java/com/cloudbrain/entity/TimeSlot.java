package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("time_slot")
public class TimeSlot {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("slot_id")
    private String slotId;

    @TableField("schedule_id")
    private String scheduleId;

    @TableField("doctor_id")
    private String doctorId;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    private Integer status = 0;

    @TableField("lock_token")
    private String lockToken;

    @TableField("locked_by")
    private String lockedBy;

    @TableField("lock_expiry_time")
    private LocalDateTime lockExpiryTime;

    @TableField("appointment_id")
    private String appointmentId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
