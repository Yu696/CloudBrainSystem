package com.cloudbrain.dto.response.pharmacy;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class DispenseResultVO {

    private String recordId;
    private String prescriptionId;
    private Integer shipNum;
    private BigDecimal payAmount;
    private LocalDateTime shipTime;
    private Integer printStatus;
}
