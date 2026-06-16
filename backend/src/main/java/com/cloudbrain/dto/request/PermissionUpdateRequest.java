package com.cloudbrain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class PermissionUpdateRequest {
    @NotBlank(message = "角色ID不能为空")
    private String roleId;
    @NotEmpty(message = "权限ID列表不能为空")
    private List<String> permissionIds;
}
