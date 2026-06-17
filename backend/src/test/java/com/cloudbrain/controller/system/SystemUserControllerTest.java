package com.cloudbrain.controller.system;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.SystemUserAddRequest;
import com.cloudbrain.dto.request.SystemUserStatusRequest;
import com.cloudbrain.dto.request.SystemUserUpdateRequest;
import com.cloudbrain.dto.response.SystemUserVO;
import com.cloudbrain.service.system.SystemUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SystemUserControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("SystemUserController 单元测试")
class SystemUserControllerTest {

    @Configuration
    @EnableAutoConfiguration(exclude = {
            SecurityAutoConfiguration.class,
            SecurityFilterAutoConfiguration.class,
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class
    })
    @ComponentScan(
            basePackages = {"com.cloudbrain.controller.system", "com.cloudbrain.common"},
            useDefaultFilters = false,
            includeFilters = @ComponentScan.Filter({Controller.class, RestControllerAdvice.class}))
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SystemUserService systemUserService;

    private static final String USER_ID = "USER_001";
    private static final String USERNAME = "admin";

    private SystemUserVO mockUserVO() {
        return SystemUserVO.builder()
                .userId(USER_ID)
                .username(USERNAME)
                .realName("管理员")
                .phone("13800138000")
                .email("admin@cloudbrain.com")
                .userType(0)
                .adminType(1)
                .roleId("ROLE_001")
                .roleName("系统管理员")
                .status(1)
                .createTime(LocalDateTime.now())
                .build();
    }

    // ======================== 正案例 ========================

    @Nested
    @DisplayName("正常操作")
    class HappyPath {

        @Test
        @DisplayName("新增用户 — 返回 userId")
        void add_success() throws Exception {
            when(systemUserService.addSystemUser(any(SystemUserAddRequest.class)))
                    .thenReturn(USER_ID);

            mockMvc.perform(post("/api/system/user/add")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"newadmin","password":"123456","roleId":"ROLE_001"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.userId").value(USER_ID));
        }

        @Test
        @DisplayName("用户列表 — 返回用户数组")
        void list_success() throws Exception {
            when(systemUserService.listSystemUsers())
                    .thenReturn(List.of(mockUserVO()));

            mockMvc.perform(get("/api/system/user/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].userId").value(USER_ID))
                    .andExpect(jsonPath("$.data[0].username").value(USERNAME))
                    .andExpect(jsonPath("$.data[0].roleName").value("系统管理员"));
        }

        @Test
        @DisplayName("用户详情 — 返回用户信息")
        void detail_success() throws Exception {
            when(systemUserService.getSystemUserDetail(USER_ID))
                    .thenReturn(mockUserVO());

            mockMvc.perform(get("/api/system/user/detail")
                            .param("userId", USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.userId").value(USER_ID))
                    .andExpect(jsonPath("$.data.email").value("admin@cloudbrain.com"));
        }

        @Test
        @DisplayName("修改用户 — 返回 success=true")
        void update_success() throws Exception {
            doNothing().when(systemUserService).updateSystemUser(any(SystemUserUpdateRequest.class));

            mockMvc.perform(put("/api/system/user/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId":"USER_001","username":"newname","roleId":"ROLE_002"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.success").value(true));
        }

        @Test
        @DisplayName("启用/禁用用户 — 返回 success=true")
        void status_success() throws Exception {
            doNothing().when(systemUserService).updateStatus(any(SystemUserStatusRequest.class));

            mockMvc.perform(put("/api/system/user/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId":"USER_001","status":0}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.success").value(true));
        }

        @Test
        @DisplayName("删除用户 — 返回 success=true")
        void delete_success() throws Exception {
            doNothing().when(systemUserService).deleteSystemUser(USER_ID);

            mockMvc.perform(delete("/api/system/user/delete")
                            .param("userId", USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.success").value(true));
        }
    }

    // ======================== 参数校验失败 ========================

    @Nested
    @DisplayName("参数校验失败")
    class ValidationError {

        @Test
        @DisplayName("新增用户 — 缺少必填字段返回 400")
        void add_missingRequiredFields() throws Exception {
            mockMvc.perform(post("/api/system/user/add")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("新增用户 — 密码不足6位返回 400")
        void add_passwordTooShort() throws Exception {
            mockMvc.perform(post("/api/system/user/add")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"admin","password":"123","roleId":"ROLE_001"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("修改用户 — 缺少必填字段返回 400")
        void update_missingRequiredFields() throws Exception {
            mockMvc.perform(put("/api/system/user/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("启用/禁用 — 缺少必填字段返回 400")
        void status_missingRequiredFields() throws Exception {
            mockMvc.perform(put("/api/system/user/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }
    }

    // ======================== 业务异常 ========================

    @Nested
    @DisplayName("Service 抛出业务异常")
    class ServiceException {

        @Test
        @DisplayName("新增用户 — 用户名重复")
        void add_duplicateUsername() throws Exception {
            when(systemUserService.addSystemUser(any(SystemUserAddRequest.class)))
                    .thenThrow(new BusinessException(400, "用户名已存在"));

            mockMvc.perform(post("/api/system/user/add")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"existing","password":"123456","roleId":"ROLE_001"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("用户名已存在"));
        }

        @Test
        @DisplayName("用户详情 — 不存在的 userId")
        void detail_userNotFound() throws Exception {
            when(systemUserService.getSystemUserDetail("NOT_EXIST"))
                    .thenThrow(new BusinessException(404, "用户不存在"));

            mockMvc.perform(get("/api/system/user/detail")
                            .param("userId", "NOT_EXIST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }

        @Test
        @DisplayName("修改用户 — 不存在的用户")
        void update_userNotFound() throws Exception {
            doThrow(new BusinessException(404, "用户不存在"))
                    .when(systemUserService).updateSystemUser(any(SystemUserUpdateRequest.class));

            mockMvc.perform(put("/api/system/user/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId":"NOT_EXIST","username":"admin","roleId":"ROLE_001"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }

        @Test
        @DisplayName("删除用户 — 有关联数据无法删除")
        void delete_hasAssociations() throws Exception {
            doThrow(new BusinessException(400, "该用户有关联数据，无法删除"))
                    .when(systemUserService).deleteSystemUser("USER_001");

            mockMvc.perform(delete("/api/system/user/delete")
                            .param("userId", "USER_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("该用户有关联数据，无法删除"));
        }
    }
}
