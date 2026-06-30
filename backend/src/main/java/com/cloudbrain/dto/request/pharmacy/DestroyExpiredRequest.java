package com.cloudbrain.dto.request.pharmacy;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DestroyExpiredRequest {
    @NotBlank(message = "药品ID不能为空")
    private String drugId;

    private String warehouseId;

    private String batchNo;
}
