package com.cloudbrain.dto.request.pharmacy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StockAdjustRequest {

    @NotBlank(message = "药品ID不能为空")
    private String drugId;

    @NotNull(message = "调整数量不能为空")
    private Integer quantity;  // 正数=入库，负数=出库

    private String warehouseId;

    private String batchNo;

    private LocalDate productionDate;

    private LocalDate expiryDate;

    private Integer minStock;

    private Integer maxStock;

    private String remark;
}
