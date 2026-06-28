package com.cloudbrain.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.SystemUserAddRequest;
import com.cloudbrain.dto.request.SystemUserStatusRequest;
import com.cloudbrain.dto.request.SystemUserUpdateRequest;
import com.cloudbrain.dto.response.SystemUserVO;
import com.cloudbrain.entity.Doctor;
import com.cloudbrain.entity.Role;
import com.cloudbrain.entity.SystemUser;
import com.cloudbrain.entity.User;
import com.cloudbrain.entity.UserRole;
import com.cloudbrain.mapper.DoctorMapper;
import com.cloudbrain.mapper.RoleMapper;
import com.cloudbrain.mapper.SystemUserMapper;
import com.cloudbrain.mapper.UserMapper;
import com.cloudbrain.mapper.UserRoleMapper;
import com.cloudbrain.service.system.SystemUserService;
import com.cloudbrain.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemUserServiceImpl extends ServiceImpl<SystemUserMapper, SystemUser> implements SystemUserService {

    private final UserMapper userMapper;
    private final SystemUserMapper systemUserMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final DoctorMapper doctorMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public String addSystemUser(SystemUserAddRequest request) {
        checkAdmin();

        // 校验用户名唯一性
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 查询角色
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleId, request.getRoleId()));
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 创建 user 记录
        User user = new User();
        user.setUserId(UUIDUtil.generateUserId());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getUsername());
        user.setUserType(mapRoleCodeToUserType(role.getRoleCode()));
        user.setStatus(1);
        userMapper.insert(user);

        // 根据角色创建对应的专业表记录
        createSpecializedRecord(role.getRoleCode(), user.getUserId(), request.getDepartmentId());

        // 创建 user_role 关联
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getUserId());
        userRole.setRoleId(request.getRoleId());
        userRoleMapper.insert(userRole);

        return user.getUserId();
    }

    /** 根据角色编码创建对应的专业表记录 */
    private void createSpecializedRecord(String roleCode, String userId, String departmentId) {
        switch (roleCode) {
            case "admin" -> {
                SystemUser su = new SystemUser();
                su.setUserId(userId);
                su.setAdminType(0);
                systemUserMapper.insert(su);
            }
            case "doctor" -> {
                Doctor doctor = new Doctor();
                doctor.setDoctorId(UUIDUtil.generateDoctorId());
                doctor.setUserId(userId);
                doctor.setDepartmentId(departmentId != null && !departmentId.isBlank() ? departmentId : "0");
                doctorMapper.insert(doctor);
            }
            // patient 无需创建额外记录
        }
    }

    /** 角色编码 → userType */
    private Integer mapRoleCodeToUserType(String roleCode) {
        return switch (roleCode) {
            case "admin" -> 1;
            case "doctor" -> 0;
            case "patient" -> 2;
            default -> 2;
        };
    }

    @Override
    public List<SystemUserVO> listSystemUsers() {
        checkAdmin();

        List<SystemUser> systemUsers = systemUserMapper.selectList(null);
        return systemUsers.stream()
                .map(su -> buildSystemUserVO(su.getUserId()))
                .toList();
    }

    @Override
    public SystemUserVO getSystemUserDetail(String userId) {
        checkAdmin();
        return buildSystemUserVO(userId);
    }

    @Override
    @Transactional
    public void updateSystemUser(SystemUserUpdateRequest request) {
        checkAdmin();

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUserId, request.getUserId()));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新用户名（校验唯一性）
        if (!user.getUsername().equals(request.getUsername())) {
            Long count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
            if (count > 0) {
                throw new BusinessException("用户名已存在");
            }
            user.setUsername(request.getUsername());
            user.setRealName(request.getUsername());
            userMapper.updateById(user);
        }

        // 更新角色
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, request.getUserId()));
        UserRole userRole = new UserRole();
        userRole.setUserId(request.getUserId());
        userRole.setRoleId(request.getRoleId());
        userRoleMapper.insert(userRole);
    }

    @Override
    @Transactional
    public void updateStatus(SystemUserStatusRequest request) {
        checkAdmin();

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUserId, request.getUserId()));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setStatus(request.getStatus());
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void deleteSystemUser(String userId) {
        checkAdmin();

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验是否有关联业务数据
        // checkBusinessReference(userId);

        // 删除 system_user 记录
        systemUserMapper.delete(new LambdaQueryWrapper<SystemUser>().eq(SystemUser::getUserId, userId));

        // 删除 user_role 关联
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));

        // 逻辑删除 user
        user.setDeleted(1);
        userMapper.updateById(user);
    }

    /** 校验当前用户是否为管理员 */
    private void checkAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("未登录");
        }
        User current = (User) auth.getPrincipal();
        if (current.getUserType() == null || current.getUserType() != 1) {
            throw new BusinessException("无操作权限，仅管理员可执行此操作");
        }
    }

    /** 构建系统用户 VO */
    private SystemUserVO buildSystemUserVO(String userId) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        if (user == null) return null;

        SystemUser systemUser = systemUserMapper.selectOne(
                new LambdaQueryWrapper<SystemUser>().eq(SystemUser::getUserId, userId));

        // 查询角色
        String roleId = null;
        String roleName = null;
        UserRole userRole = userRoleMapper.selectOne(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (userRole != null) {
            roleId = userRole.getRoleId();
            Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleId, roleId));
            if (role != null) {
                roleName = role.getRoleName();
            }
        }

        return SystemUserVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .userType(user.getUserType())
                .adminType(systemUser != null ? systemUser.getAdminType() : null)
                .roleId(roleId)
                .roleName(roleName)
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .build();
    }
}
