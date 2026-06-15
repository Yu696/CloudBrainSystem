package com.cloudbrain.service.user;

import com.cloudbrain.dto.request.RoleAssignRequest;
import com.cloudbrain.dto.request.PermissionUpdateRequest;
import com.cloudbrain.entity.Permission;
import com.cloudbrain.entity.Role;

import java.util.List;

public interface RoleService {

    /** 为用户分配角色，校验用户、角色存在且未重复分配 */
    void assignRole(RoleAssignRequest request);

    /** 查询指定角色的权限列表 */
    List<Permission> getPermissions(String roleId);

    /** 查询指定用户的角色信息 */
    Role getUserRole(String userId);

    /** 更新角色权限配置（先清后插） */
    void updatePermission(PermissionUpdateRequest request);
}
