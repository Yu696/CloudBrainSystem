package com.cloudbrain.controller.user;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.dto.request.PermissionUpdateRequest;
import com.cloudbrain.dto.request.RoleAssignRequest;
import com.cloudbrain.entity.Permission;
import com.cloudbrain.entity.Role;
import com.cloudbrain.service.user.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色权限")
@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController extends BaseController {

    private final RoleService roleService;

    /** 为用户分配角色，需校验用户和角色存在且未重复分配 */
    @Operation(summary = "分配角色")
    @PostMapping("/assign")
    public Result<String> assignRole(@Valid @RequestBody RoleAssignRequest request) {
        roleService.assignRole(request);
        return success("分配成功");
    }

    /** 查询指定角色的权限列表，按排序字段升序返回 */
    @Operation(summary = "查询角色权限")
    @GetMapping("/permissions")
    public Result<List<Permission>> permissions(@RequestParam(required = false) String roleId) {
        return success(roleService.getPermissions(roleId));
    }

    /** 查询指定用户的角色信息 */
    @Operation(summary = "查询用户角色")
    @GetMapping("/user-role")
    public Result<Role> userRole(@RequestParam String userId) {
        return success(roleService.getUserRole(userId));
    }

    /** 更新角色权限配置，先清空原权限再批量插入新权限 */
    @Operation(summary = "更新权限配置")
    @PutMapping("/update-permission")
    public Result<String> updatePermission(@Valid @RequestBody PermissionUpdateRequest request) {
        roleService.updatePermission(request);
        return success("更新成功");
    }
}
