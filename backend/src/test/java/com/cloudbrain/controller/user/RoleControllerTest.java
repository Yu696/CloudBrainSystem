package com.cloudbrain.controller.user;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.config.TestDataSourceConfig;
import com.cloudbrain.dto.response.UserRoleVO;
import com.cloudbrain.entity.Permission;
import com.cloudbrain.entity.Role;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = RoleControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RoleController 单元测试")
class RoleControllerTest {

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
    private RoleService roleService;

    @MockBean
    private UserService userService;

    @Nested
    @DisplayName("正常操作")
    class HappyPath {
        @Test
        @DisplayName("角色列表")
        void testList() throws Exception {
            Role role = new Role();
            role.setRoleId("ROLE_DOCTOR");
            role.setRoleCode("doctor");
            role.setRoleName("医生");
            when(roleService.listAllRoles()).thenReturn(List.of(role));

            mockMvc.perform(get("/api/role/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("分配角色")
        void testAssignRole() throws Exception {
            doNothing().when(roleService).assignRole(any());

            mockMvc.perform(post("/api/role/assign")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userId\":\"USR_pat001\",\"roleId\":\"ROLE_DOCTOR\",\"departmentId\":\"DEPT_001\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("查询角色权限")
        void testPermissions() throws Exception {
            Permission perm = new Permission();
            perm.setPermissionId("PERM_001");
            perm.setPermissionName("查看病历");
            when(roleService.getPermissions("ROLE_DOCTOR")).thenReturn(List.of(perm));

            mockMvc.perform(get("/api/role/permissions").param("roleId", "ROLE_DOCTOR"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("查询用户角色")
        void testUserRole() throws Exception {
            UserRoleVO vo = new UserRoleVO();
            vo.setRoleCode("doctor");
            vo.setRoleName("医生");
            when(roleService.getUserRole("USR_doc001")).thenReturn(vo);

            mockMvc.perform(get("/api/role/user-role").param("userId", "USR_doc001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.roleCode").value("doctor"));
        }

        @Test
        @DisplayName("权限树")
        void testPermissionTree() throws Exception {
            Permission perm = new Permission();
            perm.setPermissionId("PERM_001");
            perm.setPermissionName("全部权限");
            when(roleService.listAllPermissions()).thenReturn(List.of(perm));

            mockMvc.perform(get("/api/role/permission-tree"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("更新权限配置")
        void testUpdatePermission() throws Exception {
            doNothing().when(roleService).updatePermission(any());

            mockMvc.perform(put("/api/role/update-permission")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"roleId\":\"ROLE_DOCTOR\",\"permissionIds\":[\"PERM_001\",\"PERM_002\"]}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("Service 抛出业务异常")
    class ServiceException {
        @Test
        @DisplayName("分配角色失败")
        void testAssignRoleFail() throws Exception {
            doThrow(new BusinessException("用户已分配该角色"))
                    .when(roleService).assignRole(any());

            mockMvc.perform(post("/api/role/assign")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userId\":\"USR_doc001\",\"roleId\":\"ROLE_DOCTOR\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("用户已分配该角色"));
        }
    }
}
