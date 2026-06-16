package com.cloudbrain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleAssignRequest {
    @NotBlank(message = "用户ID不能为空")
    private String userId;
    @NotBlank(message = "角色ID不能为空")
    private String roleId;
}
