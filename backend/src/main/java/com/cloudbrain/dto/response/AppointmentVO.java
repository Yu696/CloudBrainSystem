package com.cloudbrain.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AppointmentVO {

    private String appointmentId;
    private String patientId;
    private String patientName;
    private String doctorId;
    private String doctorName;
    private String departmentId;
    private String departmentName;
    private String scheduleId;
    private String timeSlotId;
    private LocalDate appointmentDate;
    private String timeSlotDesc;
    private Integer appointmentType;
    private String symptoms;
    private Integer source;
    private Integer status;
    private Integer paymentStatus;
    private BigDecimal totalFee;
    private String cancelReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
