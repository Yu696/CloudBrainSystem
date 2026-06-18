package com.cloudbrain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SystemUserStatusRequest {

    @NotBlank(message = "用户ID不能为空")
    private String userId;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
