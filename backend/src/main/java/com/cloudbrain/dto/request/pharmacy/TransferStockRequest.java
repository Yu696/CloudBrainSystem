package com.cloudbrain.dto.request.pharmacy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TransferStockRequest {
    @NotBlank(message = "药品ID不能为空")
    private String drugId;

    @NotBlank(message = "源仓库ID不能为空")
    private String fromWarehouseId;

    @NotBlank(message = "目标仓库ID不能为空")
    private String toWarehouseId;

    @NotNull(message = "转移数量不能为空")
    @Positive(message = "转移数量必须大于0")
    private Integer quantity;

    private String batchNo;
}
