package com.cloudbrain.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class DoctorVO {
    private String doctorId;
    private String userId;
    private String departmentId;
    private String realName;
    private String title;
    private String specialty;
    private String introduction;
    private BigDecimal consultationFee;
    private Integer maxDailyPatients;
    private Integer available;
}
