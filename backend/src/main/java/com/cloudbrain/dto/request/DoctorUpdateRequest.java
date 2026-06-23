package com.cloudbrain.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DoctorUpdateRequest {
    private String departmentId;
    private String title;
    private String specialty;
    private String introduction;
    private BigDecimal consultationFee;
    private Integer maxDailyPatients;
}
