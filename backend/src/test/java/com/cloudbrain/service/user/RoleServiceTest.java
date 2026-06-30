package com.cloudbrain.service.user;

import com.cloudbrain.CloudbrainTest;
import com.cloudbrain.TestAuthUtils;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.PermissionUpdateRequest;
import com.cloudbrain.dto.request.RoleAssignRequest;
import com.cloudbrain.dto.response.UserRoleVO;
import com.cloudbrain.entity.Permission;
import com.cloudbrain.entity.Role;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 角色服务测试。
 * 需要 admin 权限的方法通过 TestAuthUtils 设置 userType=1。
 */
@CloudbrainTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class RoleServiceTest {

    @Autowired
    private RoleService roleService;

    private static final String ROLE_DOCTOR = "ROLE_DOCTOR";
    private static final String ROLE_PATIENT = "ROLE_PATIENT";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    // ======================== 只读查询（无需 admin） ========================

    @Test
    @Order(1)
    @DisplayName("列出所有角色")
    void testListAllRoles() {
        List<Role> list = roleService.listAllRoles();
        assertNotNull(list);
        assertFalse(list.isEmpty(), "应有种子角色");
        assertTrue(list.stream().anyMatch(r -> "admin".equals(r.getRoleCode())));
        assertTrue(list.stream().anyMatch(r -> "doctor".equals(r.getRoleCode())));
        assertTrue(list.stream().anyMatch(r -> "patient".equals(r.getRoleCode())));
    }

    @Test
    @Order(2)
    @DisplayName("查询角色权限 — DOCTOR 角色")
    void testGetPermissions() {
        List<Permission> list = roleService.getPermissions(ROLE_DOCTOR);
        assertNotNull(list);
        assertFalse(list.isEmpty(), "医生角色应有权限配置");
    }

    @Test
    @Order(3)
    @DisplayName("查询角色权限 — 无权限的角色返回空列表")
    void testGetPermissionsEmptyRole() {
        List<Permission> list = roleService.getPermissions("ROLE_NOT_EXIST");
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("查询用户角色 — 已有角色的用户")
    void testGetUserRole() {
        UserRoleVO vo = roleService.getUserRole("USR_doc001");
        assertNotNull(vo);
        assertEquals("doctor", vo.getRoleCode());
    }

    @Test
    @Order(5)
    @DisplayName("查询用户角色 — 无角色的用户抛异常")
    void testGetUserRoleNoRole() {
        assertThrows(BusinessException.class,
                () -> roleService.getUserRole("USR_NO_ROLE_999"));
    }

    @Test
    @Order(6)
    @DisplayName("列出所有权限")
    void testListAllPermissions() {
        List<Permission> list = roleService.listAllPermissions();
        assertNotNull(list);
        assertFalse(list.isEmpty(), "应有权限种子数据");
    }

    // ======================== 分配角色（需 admin） ========================

    @Test
    @Order(7)
    @DisplayName("分配角色 — 管理员正常分配")
    void testAssignRoleSuccess() {
        TestAuthUtils.setupAuth("USR_admin001", 1);
        try {
            RoleAssignRequest req = new RoleAssignRequest();
            req.setUserId("USR_pat002");
            req.setRoleId(ROLE_DOCTOR);
            req.setDepartmentId("DEPT_001");

            roleService.assignRole(req);

            UserRoleVO vo = roleService.getUserRole("USR_pat002");
            assertEquals("doctor", vo.getRoleCode());
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    @Test
    @Order(8)
    @DisplayName("分配角色 — 非管理员抛异常")
    void testAssignRoleNotAdmin() {
        TestAuthUtils.setupAuth("USR_doc001", 0); // doctor, not admin
        try {
            RoleAssignRequest req = new RoleAssignRequest();
            req.setUserId("USR_pat002");
            req.setRoleId(ROLE_DOCTOR);

            assertThrows(BusinessException.class, () -> roleService.assignRole(req));
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    @Test
    @Order(9)
    @DisplayName("分配角色 — 用户不存在抛异常")
    void testAssignRoleUserNotFound() {
        TestAuthUtils.setupAuth("USR_admin001", 1);
        try {
            RoleAssignRequest req = new RoleAssignRequest();
            req.setUserId("USR_NOT_EXIST");
            req.setRoleId(ROLE_DOCTOR);

            assertThrows(BusinessException.class, () -> roleService.assignRole(req));
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    @Test
    @Order(10)
    @DisplayName("分配角色 — 角色不存在抛异常")
    void testAssignRoleRoleNotFound() {
        TestAuthUtils.setupAuth("USR_admin001", 1);
        try {
            RoleAssignRequest req = new RoleAssignRequest();
            req.setUserId("USR_pat002");
            req.setRoleId("ROLE_NOT_EXIST");

            assertThrows(BusinessException.class, () -> roleService.assignRole(req));
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    // ======================== 更新权限（需 admin） ========================

    @Test
    @Order(11)
    @DisplayName("更新权限 — 管理员正常更新")
    void testUpdatePermission() {
        TestAuthUtils.setupAuth("USR_admin001", 1);
        try {
            // 先获取当前权限
            List<Permission> current = roleService.getPermissions(ROLE_DOCTOR);
            List<String> existingIds = current.stream().map(Permission::getPermissionId).toList();

            PermissionUpdateRequest req = new PermissionUpdateRequest();
            req.setRoleId(ROLE_DOCTOR);
            req.setPermissionIds(existingIds); // 保持不变

            roleService.updatePermission(req);

            List<Permission> after = roleService.getPermissions(ROLE_DOCTOR);
            assertEquals(existingIds.size(), after.size());
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    @Test
    @Order(12)
    @DisplayName("更新权限 — 超级管理员权限不可修改")
    void testUpdatePermissionSuperAdmin() {
        TestAuthUtils.setupAuth("USR_admin001", 1);
        try {
            PermissionUpdateRequest req = new PermissionUpdateRequest();
            req.setRoleId(ROLE_ADMIN);
            req.setPermissionIds(List.of());

            assertThrows(BusinessException.class, () -> roleService.updatePermission(req));
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    @Test
    @Order(13)
    @DisplayName("更新权限 — 非管理员抛异常")
    void testUpdatePermissionNotAdmin() {
        TestAuthUtils.setupAuth("USR_doc001", 0);
        try {
            PermissionUpdateRequest req = new PermissionUpdateRequest();
            req.setRoleId(ROLE_DOCTOR);
            req.setPermissionIds(List.of());

            assertThrows(BusinessException.class, () -> roleService.updatePermission(req));
        } finally {
            TestAuthUtils.clearAuth();
        }
    }
}
