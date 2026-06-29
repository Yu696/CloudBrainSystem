package com.cloudbrain.dto.response.pharmacy;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class StockVO {

    private String drugId;
    private String drugName;
    private String warehouseId;
    private String warehouseName;
    private Integer currentStock;
    private Integer minStock;
    private Integer maxStock;
    private String batchNo;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
