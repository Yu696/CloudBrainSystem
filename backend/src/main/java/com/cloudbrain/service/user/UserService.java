package com.cloudbrain.service.user;

import com.cloudbrain.dto.request.LoginRequest;
import com.cloudbrain.dto.request.RegisterRequest;
import com.cloudbrain.dto.request.ResetPasswordRequest;
import com.cloudbrain.dto.request.UserUpdateRequest;
import com.cloudbrain.dto.response.LoginResponse;
import com.cloudbrain.dto.response.UserInfoVO;

import java.util.List;

public interface UserService {

    /** 用户登录，验证账号密码，返回 JWT Token 和用户信息 */
    LoginResponse login(LoginRequest request);

    /** 获取当前登录用户信息（含角色） */
    UserInfoVO getCurrentUser();

    /** 更新当前用户个人信息（真实姓名、手机号、邮箱） */
    void updateUser(UserUpdateRequest request);

    /** 重置密码，需验证旧密码正确性 */
    void resetPassword(ResetPasswordRequest request);

    /** 用户注册，校验用户名唯一性，返回用户 ID */
    String register(RegisterRequest request);

    /** 获取所有用户列表（含角色信息，仅管理员） */
    List<UserInfoVO> listAllUsers();
}
