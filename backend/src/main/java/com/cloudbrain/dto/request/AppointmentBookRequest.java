package com.cloudbrain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentBookRequest {

    @NotBlank(message = "患者ID不能为空")
    private String patientId;

    @NotBlank(message = "医生ID不能为空")
    private String doctorId;

    @NotBlank(message = "科室ID不能为空")
    private String departmentId;

    @NotBlank(message = "时段ID不能为空")
    private String slotId;

    @NotNull(message = "预约类型不能为空")
    private Integer appointmentType;  // 0=普通 1=专家 2=急诊

    private String symptoms;
    private Integer source;           // 0=在线 1=窗口 2=自助机
}
