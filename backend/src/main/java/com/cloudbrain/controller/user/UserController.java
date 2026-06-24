package com.cloudbrain.controller.user;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.dto.request.LoginRequest;
import com.cloudbrain.dto.request.RegisterRequest;
import com.cloudbrain.dto.request.ResetPasswordRequest;
import com.cloudbrain.dto.request.UserStatusRequest;
import com.cloudbrain.dto.request.UserUpdateRequest;
import com.cloudbrain.dto.response.LoginResponse;
import com.cloudbrain.dto.response.UserInfoVO;
import com.cloudbrain.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "用户中心")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserService userService;

    /** 用户登录，验证账号密码，返回 JWT Token 和用户信息 */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return success(userService.login(loginRequest));
    }

    /** 获取当前登录用户信息（含角色） */
    @Operation(summary = "获取用户信息")
    @GetMapping("/info")
    public Result<UserInfoVO> info() {
        return success(userService.getCurrentUser());
    }

    /** 更新当前用户个人信息 */
    @Operation(summary = "更新个人信息")
    @PutMapping("/update")
    public Result<String> update(@Valid @RequestBody UserUpdateRequest request) {
        userService.updateUser(request);
        return success("修改成功");
    }

    /** 重置密码（需验证旧密码） */
    @Operation(summary = "重置密码")
    @PutMapping("/reset-password")
    public Result<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return success("重置成功");
    }

    /** 获取所有用户列表（含角色信息，仅管理员），可按用户类型筛选 */
    @Operation(summary = "用户列表")
    @GetMapping("/list-all")
    public Result<List<UserInfoVO>> listAll(@RequestParam(required = false) Integer userType) {
        return success(userService.listAllUsers(userType));
    }

    /** 启用/禁用用户（仅管理员） */
    @Operation(summary = "启用/禁用用户")
    @PutMapping("/status")
    public Result<String> status(@Valid @RequestBody UserStatusRequest request) {
        userService.updateStatus(request.getUserId(), request.getStatus());
        return success(request.getStatus() == 1 ? "启用成功" : "禁用成功");
    }

    /** 删除用户（仅管理员） */
    @Operation(summary = "删除用户")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam String userId) {
        userService.deleteUser(userId);
        return success("删除成功");
    }

    /** 用户注册，返回用户 ID */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        String userId = userService.register(request);
        return success(Map.of("userId", userId));
    }
}
