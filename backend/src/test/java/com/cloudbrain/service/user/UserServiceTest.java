package com.cloudbrain.service.user;

import com.cloudbrain.CloudbrainTest;
import com.cloudbrain.TestAuthUtils;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.LoginRequest;
import com.cloudbrain.dto.request.RegisterRequest;
import com.cloudbrain.dto.request.ResetPasswordRequest;
import com.cloudbrain.dto.request.UserUpdateRequest;
import com.cloudbrain.dto.response.LoginResponse;
import com.cloudbrain.dto.response.UserInfoVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试。
 * 所有写操作通过 @Transactional 自动回滚。
 * 使用 TestAuthUtils 设置 SecurityContext。
 */
@CloudbrainTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    // ======================== 登录 ========================

    @Test
    @Order(1)
    @DisplayName("登录 — 正确用户名和密码")
    void testLoginCorrect() {
        LoginRequest req = new LoginRequest();
        req.setUserName("admin");
        req.setPassword("123456");

        LoginResponse resp = userService.login(req);
        assertNotNull(resp);
        assertNotNull(resp.getToken());
        assertNotNull(resp.getUserInfo());
        assertEquals("admin", resp.getUserInfo().getUserName());
    }

    @Test
    @Order(2)
    @DisplayName("登录 — 错误密码抛异常")
    void testLoginWrongPassword() {
        LoginRequest req = new LoginRequest();
        req.setUserName("admin");
        req.setPassword("wrongpassword");

        assertThrows(BusinessException.class, () -> userService.login(req));
    }

    @Test
    @Order(3)
    @DisplayName("登录 — 不存在的用户名抛异常")
    void testLoginUserNotFound() {
        LoginRequest req = new LoginRequest();
        req.setUserName("nonexistent_user_xyz");
        req.setPassword("123456");

        assertThrows(BusinessException.class, () -> userService.login(req));
    }

    // ======================== getCurrentUser ========================

    @Test
    @Order(4)
    @DisplayName("获取当前用户 — 已认证")
    void testGetCurrentUserAuthenticated() {
        TestAuthUtils.setupAuth("USR_pat001");
        try {
            UserInfoVO vo = userService.getCurrentUser();
            assertNotNull(vo);
            assertEquals("USR_pat001", vo.getUserId());
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    @Test
    @Order(5)
    @DisplayName("获取当前用户 — 未认证抛异常")
    void testGetCurrentUserNotAuthenticated() {
        TestAuthUtils.clearAuth();
        assertThrows(BusinessException.class, () -> userService.getCurrentUser());
    }

    // ======================== 更新用户 ========================

    @Test
    @Order(6)
    @DisplayName("更新用户 — 修改姓名和手机号")
    void testUpdateUser() {
        TestAuthUtils.setupAuth("USR_pat001");
        try {
            UserUpdateRequest req = new UserUpdateRequest();
            req.setRealName("更新姓名测试");
            req.setPhone("13900000000");

            userService.updateUser(req);

            UserInfoVO vo = userService.getCurrentUser();
            assertEquals("更新姓名测试", vo.getRealName());
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    // ======================== 注册 ========================

    @Test
    @Order(7)
    @DisplayName("注册 — 正常注册新用户")
    void testRegisterSuccess() {
        RegisterRequest req = new RegisterRequest();
        req.setUserName("test_register_" + System.currentTimeMillis());
        req.setPassword("test123456");
        req.setRealName("注册测试用户");
        req.setPhone("138" + String.format("%08d", System.currentTimeMillis() % 100000000));

        String userId = userService.register(req);
        assertNotNull(userId);
        assertTrue(userId.startsWith("USR_"));
    }

    @Test
    @Order(8)
    @DisplayName("注册 — 重复用户名抛异常")
    void testRegisterDuplicate() {
        RegisterRequest req = new RegisterRequest();
        req.setUserName("admin"); // 已存在
        req.setPassword("test123456");
        req.setRealName("重复注册");

        assertThrows(BusinessException.class, () -> userService.register(req));
    }

    // ======================== 重置密码 ========================

    @Test
    @Order(9)
    @DisplayName("重置密码 — 旧密码正确")
    void testResetPasswordCorrect() {
        TestAuthUtils.setupAuth("USR_pat001");
        try {
            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setOldPassword("123456");
            req.setNewPassword("newpassword123");

            userService.resetPassword(req);

            // 验证新密码可登录
            LoginRequest loginReq = new LoginRequest();
            loginReq.setUserName("patient1");
            loginReq.setPassword("newpassword123");
            LoginResponse resp = userService.login(loginReq);
            assertNotNull(resp.getToken());
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    @Test
    @Order(10)
    @DisplayName("重置密码 — 旧密码错误抛异常")
    void testResetPasswordWrongOld() {
        TestAuthUtils.setupAuth("USR_pat001");
        try {
            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setOldPassword("wrong_old_password");
            req.setNewPassword("newpassword123");

            assertThrows(BusinessException.class, () -> userService.resetPassword(req));
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    // ======================== 用户列表（管理员） ========================

    @Test
    @Order(11)
    @DisplayName("用户列表 — 管理员按类型筛选")
    void testListAllUsersByType() {
        TestAuthUtils.setupAuth("USR_admin001");
        try {
            List<UserInfoVO> list = userService.listAllUsers(2);
            assertNotNull(list);
            assertFalse(list.isEmpty(), "应有患者用户");
            list.forEach(u -> assertEquals(2, u.getUserType(), "应全是患者"));
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    @Test
    @Order(12)
    @DisplayName("用户列表 — 非管理员抛异常")
    void testListAllUsersNotAdmin() {
        TestAuthUtils.setupAuth("USR_pat001");
        try {
            assertThrows(BusinessException.class, () -> userService.listAllUsers(null));
        } finally {
            TestAuthUtils.clearAuth();
        }
    }
}
