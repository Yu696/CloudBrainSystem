package com.cloudbrain.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.LoginRequest;
import com.cloudbrain.dto.request.RegisterRequest;
import com.cloudbrain.dto.request.ResetPasswordRequest;
import com.cloudbrain.dto.request.UserUpdateRequest;
import com.cloudbrain.dto.response.LoginResponse;
import com.cloudbrain.dto.response.UserInfoVO;
import com.cloudbrain.entity.Role;
import com.cloudbrain.entity.User;
import com.cloudbrain.entity.UserRole;
import com.cloudbrain.mapper.RoleMapper;
import com.cloudbrain.mapper.UserMapper;
import com.cloudbrain.mapper.UserRoleMapper;
import com.cloudbrain.security.JwtUtil;
import com.cloudbrain.service.user.UserService;
import com.cloudbrain.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = lambdaQuery().eq(User::getUsername, request.getUserName()).one();
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(),
                String.valueOf(user.getUserType()), null);

        user.setLastLoginTime(LocalDateTime.now());
        updateById(user);

        return new LoginResponse(token, buildUserInfo(user));
    }

    @Override
    public UserInfoVO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("未登录");
        }
        User user = (User) authentication.getPrincipal();
        return buildUserInfo(user);
    }

    @Override
    @Transactional
    public void updateUser(UserUpdateRequest request) {
        User current = getRawUser();
        if (request.getRealName() != null) current.setRealName(request.getRealName());
        if (request.getPhone() != null) current.setPhone(request.getPhone());
        if (request.getEmail() != null) current.setEmail(request.getEmail());
        updateById(current);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User current = getRawUser();
        if (!passwordEncoder.matches(request.getOldPassword(), current.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        current.setPassword(passwordEncoder.encode(request.getNewPassword()));
        updateById(current);
    }

    @Override
    @Transactional
    public String register(RegisterRequest request) {
        Long count = lambdaQuery().eq(User::getUsername, request.getUserName()).count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUserId(UUIDUtil.generateUserId());
        user.setUsername(request.getUserName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setUserType(request.getUserType() != null ? request.getUserType() : 2);
        user.setStatus(1);
        save(user);

        // 自动分配默认角色
        String defaultRoleId = switch (user.getUserType()) {
            case 1 -> "ROLE_ADMIN";
            case 0 -> "ROLE_DOCTOR";
            case 2 -> "ROLE_PATIENT";
            default -> "ROLE_PATIENT";
        };
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getUserId());
        userRole.setRoleId(defaultRoleId);
        userRoleMapper.insert(userRole);

        return user.getUserId();
    }

    /** 获取所有用户列表（含角色信息，仅管理员） */
    @Override
    public List<UserInfoVO> listAllUsers() {
        // 校验管理员权限
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("未登录");
        }
        User current = (User) auth.getPrincipal();
        if (current.getUserType() == null || current.getUserType() != 1) {
            throw new BusinessException("无操作权限，仅管理员可执行此操作");
        }

        List<User> users = lambdaQuery().orderByAsc(User::getCreateTime).list();
        return users.stream().map(this::buildUserInfo).toList();
    }

    /** 获取当前用户实体（含密码，仅内部使用） */
    private User getRawUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("未登录");
        }
        return (User) authentication.getPrincipal();
    }

    /** 构建带角色信息的用户 VO（一个用户只有一个角色） */
    private UserInfoVO buildUserInfo(User user) {
        String roleName = null;
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getUserId()));
        if (!userRoles.isEmpty()) {
            Role role = roleMapper.selectOne(
                    new LambdaQueryWrapper<Role>().eq(Role::getRoleId, userRoles.get(0).getRoleId()));
            roleName = role != null ? role.getRoleName() : null;
        }

        return UserInfoVO.builder()
                .userId(user.getUserId())
                .userName(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .userType(user.getUserType())
                .role(roleName)
                .build();
    }
}
