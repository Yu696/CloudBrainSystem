package com.cloudbrain.service.user;

import com.cloudbrain.dto.request.RoleAssignRequest;
import com.cloudbrain.dto.request.PermissionUpdateRequest;
import com.cloudbrain.dto.response.UserRoleVO;
import com.cloudbrain.entity.Permission;
import com.cloudbrain.entity.Role;

import java.util.List;

public interface RoleService {

    /** 获取所有角色列表 */
    List<Role> listAllRoles();

    /** 为用户分配角色，校验用户、角色存在且未重复分配（仅管理员） */
    void assignRole(RoleAssignRequest request);

    /** 查询指定角色的权限列表 */
    List<Permission> getPermissions(String roleId);

    /** 查询指定用户的角色信息（若为医生则附带科室和职位） */
    UserRoleVO getUserRole(String userId);

    /** 获取所有权限列表（平铺） */
    List<Permission> listAllPermissions();

    /** 更新角色权限配置（先清后插，仅管理员） */
    void updatePermission(PermissionUpdateRequest request);
}
