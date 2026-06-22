package com.cloudbrain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SystemUserUpdateRequest {

    @NotBlank(message = "用户ID不能为空")
    private String userId;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "角色不能为空")
    private String roleId;
}
