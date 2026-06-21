package com.cloudbrain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AppointmentCancelRequest {

    @NotBlank(message = "预约ID不能为空")
    private String appointmentId;

    private String reason;
}
