package com.cloudbrain.dto.request.pharmacy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WarehouseRequest {

    @NotBlank(message = "仓库名称不能为空")
    private String name;

    private String location;

    private String adminId;

    @NotNull(message = "仓库类型不能为空")
    private Integer type;
}
