package com.cloudbrain.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.PermissionUpdateRequest;
import com.cloudbrain.dto.request.RoleAssignRequest;
import com.cloudbrain.entity.Permission;
import com.cloudbrain.entity.Role;
import com.cloudbrain.entity.RolePermission;
import com.cloudbrain.entity.User;
import com.cloudbrain.entity.UserRole;
import com.cloudbrain.mapper.PermissionMapper;
import com.cloudbrain.mapper.RoleMapper;
import com.cloudbrain.mapper.RolePermissionMapper;
import com.cloudbrain.mapper.UserMapper;
import com.cloudbrain.mapper.UserRoleMapper;
import com.cloudbrain.service.user.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    /** 获取所有角色列表 */
    @Override
    public List<Role> listAllRoles() {
        return roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .orderByAsc(Role::getCreateTime));
    }

    /** 校验当前登录用户是否为管理员 */
    private void checkAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("未登录");
        }
        User user = (User) auth.getPrincipal();
        // 通过 userType 判断（1=管理员）
        if (user.getUserType() == null || user.getUserType() != 1) {
            throw new BusinessException("无操作权限，仅管理员可执行此操作");
        }
    }

    /** 为用户分配角色，校验用户、角色存在性及重复分配（仅管理员） */
    @Override
    @Transactional
    public void assignRole(RoleAssignRequest request) {
        checkAdmin();
        // 校验用户存在
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUserId, request.getUserId())) == 0) {
            throw new BusinessException("用户不存在");
        }
        // 校验角色存在
        if (roleMapper.selectCount(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleId, request.getRoleId())) == 0) {
            throw new BusinessException("角色不存在");
        }
        // 校验重复分配
        if (userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, request.getUserId())
                .eq(UserRole::getRoleId, request.getRoleId())) > 0) {
            throw new BusinessException("该用户已拥有此角色");
        }

        UserRole userRole = new UserRole();
        userRole.setUserId(request.getUserId());
        userRole.setRoleId(request.getRoleId());
        userRoleMapper.insert(userRole);
    }

    /** 查询指定角色的权限列表，按排序字段升序返回 */
    @Override
    public List<Permission> getPermissions(String roleId) {
        List<String> permissionIds = rolePermissionMapper.selectList(
                        new LambdaQueryWrapper<RolePermission>()
                                .eq(RolePermission::getRoleId, roleId))
                .stream()
                .map(RolePermission::getPermissionId)
                .toList();
        if (permissionIds.isEmpty()) {
            return List.of();
        }
        return permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>()
                        .in(Permission::getPermissionId, permissionIds)
                        .orderByAsc(Permission::getSortOrder));
    }

    /** 查询指定用户的角色信息（返回第一个角色） */
    @Override
    public Role getUserRole(String userId) {
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            throw new BusinessException("该用户未分配角色");
        }
        Role role = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getRoleId, userRoles.get(0).getRoleId()));
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return role;
    }

    /** 更新角色权限配置，先清空原权限再批量插入新权限（仅管理员） */
    @Override
    @Transactional
    public void updatePermission(PermissionUpdateRequest request) {
        checkAdmin();
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, request.getRoleId()));

        for (String permissionId : request.getPermissionIds()) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(request.getRoleId());
            rp.setPermissionId(permissionId);
            rolePermissionMapper.insert(rp);
        }
    }
}
