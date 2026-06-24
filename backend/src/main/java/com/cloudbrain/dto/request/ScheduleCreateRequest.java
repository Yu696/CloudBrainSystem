package com.cloudbrain.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleCreateRequest {

    @NotBlank(message = "医生ID不能为空")
    private String doctorId;

    @NotBlank(message = "科室ID不能为空")
    private String departmentId;

    @NotNull(message = "排班日期不能为空")
    private LocalDate scheduleDate;

    @NotNull(message = "班次不能为空")
    private Integer workShift;       // 0=上午 1=下午 2=晚班

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    @Min(value = 5, message = "时段长度不能少于 5 分钟")
    private Integer slotDuration;    // 默认 30 分钟，最小 5

    private Integer maxPatients;     // 默认 20
    private String remark;
}
