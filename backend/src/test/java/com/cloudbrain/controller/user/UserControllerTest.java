package com.cloudbrain.controller.user;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.config.TestDataSourceConfig;
import com.cloudbrain.dto.response.LoginResponse;
import com.cloudbrain.dto.response.UserInfoVO;
import com.cloudbrain.service.user.RoleService;
import com.cloudbrain.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = UserControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController 单元测试")
class UserControllerTest {

    @Configuration
    @Import(TestDataSourceConfig.class)
    @EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
    @ComponentScan(basePackages = {"com.cloudbrain.controller.user", "com.cloudbrain.common"},
            useDefaultFilters = false,
            includeFilters = @ComponentScan.Filter({Controller.class, RestControllerAdvice.class}))
    static class TestConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RoleService roleService;

    @Nested
    @DisplayName("正常操作")
    class HappyPath {
        @Test
        @DisplayName("用户登录")
        void testLogin() throws Exception {
            UserInfoVO userInfo = UserInfoVO.builder().userId("USR_doc001").userName("doctor1").build();
            LoginResponse resp = new LoginResponse("jwt-token-xxx", userInfo);
            when(userService.login(any())).thenReturn(resp);

            mockMvc.perform(post("/api/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userName\":\"doctor1\",\"password\":\"123456\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.token").value("jwt-token-xxx"));
        }

        @Test
        @DisplayName("获取当前用户信息")
        void testInfo() throws Exception {
            UserInfoVO userInfo = UserInfoVO.builder().userId("USR_doc001").userName("doctor1").build();
            when(userService.getCurrentUser()).thenReturn(userInfo);

            mockMvc.perform(get("/api/user/info"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value("USR_doc001"));
        }

        @Test
        @DisplayName("更新个人信息")
        void testUpdate() throws Exception {
            doNothing().when(userService).updateUser(any());

            mockMvc.perform(put("/api/user/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userId\":\"USR_doc001\",\"realName\":\"新名字\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("重置密码")
        void testResetPassword() throws Exception {
            doNothing().when(userService).resetPassword(any());

            mockMvc.perform(put("/api/user/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userId\":\"USR_doc001\",\"oldPassword\":\"old123\",\"newPassword\":\"new456\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("管理员查看用户列表")
        void testListAll() throws Exception {
            UserInfoVO vo = UserInfoVO.builder().userId("USR_doc001").userName("doctor1").build();
            when(userService.listAllUsers(isNull())).thenReturn(List.of(vo));

            mockMvc.perform(get("/api/user/list-all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("启用/禁用用户")
        void testStatus() throws Exception {
            doNothing().when(userService).updateStatus("USR_doc001", 0);

            mockMvc.perform(put("/api/user/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userId\":\"USR_doc001\",\"status\":0}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("删除用户")
        void testDelete() throws Exception {
            doNothing().when(userService).deleteUser("USR_test");

            mockMvc.perform(delete("/api/user/delete").param("userId", "USR_test"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("用户注册")
        void testRegister() throws Exception {
            when(userService.register(any())).thenReturn("USR_new001");

            mockMvc.perform(post("/api/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userName\":\"newuser\",\"password\":\"123456\",\"realName\":\"新用户\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value("USR_new001"));
        }

        @Test
        @DisplayName("用户登出")
        void testLogout() throws Exception {
            doNothing().when(userService).logout(any());

            mockMvc.perform(post("/api/user/logout"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("Service 抛出业务异常")
    class ServiceException {
        @Test
        @DisplayName("登录失败")
        void testLoginFail() throws Exception {
            when(userService.login(any()))
                    .thenThrow(new BusinessException("用户名或密码错误"));

            mockMvc.perform(post("/api/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userName\":\"bad\",\"password\":\"wrong\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("用户名或密码错误"));
        }
    }
}
