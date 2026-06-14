package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("schedule")
public class Schedule {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("schedule_id")
    private String scheduleId;

    @TableField("doctor_id")
    private String doctorId;

    @TableField("department_id")
    private String departmentId;

    @TableField("schedule_date")
    private LocalDate scheduleDate;

    @TableField("work_shift")
    private Integer workShift;

    @TableField("start_time")
    private LocalTime startTime;

    @TableField("end_time")
    private LocalTime endTime;

    @TableField("slot_duration")
    private Integer slotDuration = 30;

    @TableField("max_patients")
    private Integer maxPatients = 20;

    @TableField("available_slots")
    private Integer availableSlots;

    private Integer status = 1;

    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
