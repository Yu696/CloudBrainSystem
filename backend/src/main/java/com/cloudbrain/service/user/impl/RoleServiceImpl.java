package com.cloudbrain.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.PermissionUpdateRequest;
import com.cloudbrain.dto.request.RoleAssignRequest;
import com.cloudbrain.dto.response.UserRoleVO;
import com.cloudbrain.entity.Doctor;
import com.cloudbrain.entity.Patient;
import com.cloudbrain.entity.Permission;
import com.cloudbrain.entity.Role;
import com.cloudbrain.entity.RolePermission;
import com.cloudbrain.entity.SystemUser;
import com.cloudbrain.entity.User;
import com.cloudbrain.entity.UserRole;
import com.cloudbrain.mapper.DoctorMapper;
import com.cloudbrain.mapper.PatientMapper;
import com.cloudbrain.mapper.PermissionMapper;
import com.cloudbrain.mapper.RoleMapper;
import com.cloudbrain.mapper.RolePermissionMapper;
import com.cloudbrain.mapper.SystemUserMapper;
import com.cloudbrain.mapper.UserMapper;
import com.cloudbrain.mapper.UserRoleMapper;
import com.cloudbrain.service.user.RoleService;
import com.cloudbrain.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final DoctorMapper doctorMapper;
    private final PatientMapper patientMapper;
    private final SystemUserMapper systemUserMapper;
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

    /** 为用户分配角色，一个用户只能有一个角色（仅管理员） */
    @Override
    @Transactional
    public void assignRole(RoleAssignRequest request) {
        checkAdmin();
        // 校验用户存在并获取用户对象
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserId, request.getUserId()));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 校验新角色存在
        Role newRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleId, request.getRoleId()));
        if (newRole == null) {
            throw new BusinessException("角色不存在");
        }

        // 查询旧角色，清理对应的专业表记录（doctor / patient / system_user）
        UserRole oldUserRole = userRoleMapper.selectOne(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, request.getUserId()));
        if (oldUserRole != null) {
            Role oldRole = roleMapper.selectOne(
                    new LambdaQueryWrapper<Role>().eq(Role::getRoleId, oldUserRole.getRoleId()));
            if (oldRole != null) {
                cleanupSpecializedRecord(oldRole.getRoleCode(), request.getUserId());
            }
        }

        // 删除用户原有角色
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, request.getUserId()));

        // 分配新角色
        UserRole userRole = new UserRole();
        userRole.setUserId(request.getUserId());
        userRole.setRoleId(request.getRoleId());
        userRoleMapper.insert(userRole);

        // 同步更新 userType
        user.setUserType(mapRoleToUserType(newRole));
        userMapper.updateById(user);

        // 创建或更新新角色的专业表记录
        createOrUpdateSpecializedRecord(newRole.getRoleCode(), request);
    }

    /** 清理旧角色的专业表记录 */
    private void cleanupSpecializedRecord(String roleCode, String userId) {
        switch (roleCode) {
            case "admin" -> systemUserMapper.delete(
                    new LambdaQueryWrapper<SystemUser>().eq(SystemUser::getUserId, userId));
            case "doctor", "radiologist" -> doctorMapper.delete(
                    new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
            case "patient" -> patientMapper.delete(
                    new LambdaQueryWrapper<Patient>().eq(Patient::getUserId, userId));
        }
    }

    /** 创建或更新新角色的专业表记录 */
    private void createOrUpdateSpecializedRecord(String roleCode, RoleAssignRequest request) {
        switch (roleCode) {
            case "admin" -> {
                Long count = systemUserMapper.selectCount(
                        new LambdaQueryWrapper<SystemUser>().eq(SystemUser::getUserId, request.getUserId()));
                if (count == 0) {
                    SystemUser su = new SystemUser();
                    su.setUserId(request.getUserId());
                    su.setAdminType(0);
                    systemUserMapper.insert(su);
                }
            }
            case "doctor", "radiologist" -> {
                Doctor doctor = doctorMapper.selectOne(
                        new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, request.getUserId()));
                if (doctor != null) {
                    if (request.getDepartmentId() != null) doctor.setDepartmentId(request.getDepartmentId());
                    if (request.getTitle() != null) doctor.setTitle(request.getTitle());
                    if (request.getConsultationFee() != null) doctor.setConsultationFee(request.getConsultationFee());
                    if (request.getSpecialty() != null) doctor.setSpecialty(request.getSpecialty());
                    if (request.getIntroduction() != null) doctor.setIntroduction(request.getIntroduction());
                    doctorMapper.updateById(doctor);
                } else {
                    doctor = new Doctor();
                    doctor.setDoctorId(UUIDUtil.generateDoctorId());
                    doctor.setUserId(request.getUserId());
                    doctor.setDepartmentId(request.getDepartmentId() != null ? request.getDepartmentId() : "0");
                    doctor.setTitle(request.getTitle());
                    doctor.setConsultationFee(request.getConsultationFee() != null ? request.getConsultationFee() : BigDecimal.ZERO);
                    doctor.setSpecialty(request.getSpecialty());
                    doctor.setIntroduction(request.getIntroduction());
                    doctorMapper.insert(doctor);
                }
            }
            // patient 记录由 PatientService 管理，不在角色分配时自动创建
        }
    }

    /** 根据角色编码映射 userType：admin=1, doctor=0, patient=2 */
    private Integer mapRoleToUserType(Role role) {
        if (role == null) return 2;
        return switch (role.getRoleCode()) {
            case "admin" -> 1;
            case "doctor" -> 0;
            case "patient" -> 2;
            case "radiologist" -> 3;
            default -> 2;
        };
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

    /** 查询指定用户的角色信息（返回第一个角色，若为医生则附带科室和职位） */
    @Override
    public UserRoleVO getUserRole(String userId) {
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

        UserRoleVO vo = new UserRoleVO();
        vo.setRoleId(role.getRoleId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleCode(role.getRoleCode());
        vo.setDescription(role.getDescription());
        vo.setStatus(role.getStatus());

        // 若为医生角色，附带科室、职位、挂号费和描述信息
        if ("doctor".equals(role.getRoleCode())) {
            Doctor doctor = doctorMapper.selectOne(
                    new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
            if (doctor != null) {
                vo.setDepartmentId(doctor.getDepartmentId());
                vo.setTitle(doctor.getTitle());
                vo.setConsultationFee(doctor.getConsultationFee());
                vo.setSpecialty(doctor.getSpecialty());
                vo.setIntroduction(doctor.getIntroduction());
            }
        }

        return vo;
    }

    /** 获取所有权限列表（按排序字段升序） */
    @Override
    public List<Permission> listAllPermissions() {
        return permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>()
                        .orderByAsc(Permission::getSortOrder));
    }

    /** 更新角色权限配置，先清空原权限再批量插入新权限（仅管理员） */
    @Override
    @Transactional
    public void updatePermission(PermissionUpdateRequest request) {
        checkAdmin();

        // 超级管理员权限不可修改
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleId, request.getRoleId()));
        if (role != null && "admin".equals(role.getRoleCode())) {
            throw new BusinessException("超级管理员权限不可修改");
        }

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
