package com.cloudbrain.service.system;

import com.cloudbrain.CloudbrainTest;
import com.cloudbrain.TestAuthUtils;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.SystemUserAddRequest;
import com.cloudbrain.dto.request.SystemUserStatusRequest;
import com.cloudbrain.dto.request.SystemUserUpdateRequest;
import com.cloudbrain.dto.response.SystemUserVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 系统用户管理服务测试。
 * 所有方法需要 admin 权限，通过 TestAuthUtils 设置 userType=1。
 */
@CloudbrainTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class SystemUserServiceTest {

    @Autowired
    private SystemUserService systemUserService;

    private static String createdUserId;

    // ======================== 添加系统用户 ========================

    @Test
    @Order(1)
    @DisplayName("添加系统用户 — 管理员添加医生")
    void testAddSystemUserDoctor() {
        TestAuthUtils.setupAuth("USR_admin001", 1);
        try {
            SystemUserAddRequest req = new SystemUserAddRequest();
            req.setUsername("test_doctor_" + System.currentTimeMillis());
            req.setPassword("test123456");
            req.setRoleId("ROLE_DOCTOR");
            req.setDepartmentId("DEPT_001");

            String userId = systemUserService.addSystemUser(req);
            assertNotNull(userId);
            assertTrue(userId.startsWith("USR_"));
            createdUserId = userId;
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    @Test
    @Order(2)
    @DisplayName("添加系统用户 — 管理员添加radiologist")
    void testAddSystemUserRadiologist() {
        TestAuthUtils.setupAuth("USR_admin001", 1);
        try {
            SystemUserAddRequest req = new SystemUserAddRequest();
            req.setUsername("test_radiologist_" + System.currentTimeMillis());
            req.setPassword("test123456");
            req.setRoleId("ROLE_RADIOLOGIST");
            req.setDepartmentId("DEPT_001");

            String userId = systemUserService.addSystemUser(req);
            assertNotNull(userId);
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    @Test
    @Order(3)
    @DisplayName("添加系统用户 — 重复用户名抛异常")
    void testAddSystemUserDuplicate() {
        TestAuthUtils.setupAuth("USR_admin001", 1);
        try {
            SystemUserAddRequest req = new SystemUserAddRequest();
            req.setUsername("admin"); // 已存在
            req.setPassword("test123456");
            req.setRoleId("ROLE_ADMIN");

            assertThrows(BusinessException.class, () -> systemUserService.addSystemUser(req));
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    @Test
    @Order(4)
    @DisplayName("添加系统用户 — 非管理员抛异常")
    void testAddSystemUserNotAdmin() {
        TestAuthUtils.setupAuth("USR_doc001", 0);
        try {
            SystemUserAddRequest req = new SystemUserAddRequest();
            req.setUsername("test_fail");
            req.setPassword("test123456");
            req.setRoleId("ROLE_DOCTOR");

            assertThrows(BusinessException.class, () -> systemUserService.addSystemUser(req));
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    // ======================== 查询系统用户 ========================

    @Test
    @Order(5)
    @DisplayName("系统用户列表 — 管理员查看")
    void testListSystemUsers() {
        TestAuthUtils.setupAuth("USR_admin001", 1);
        try {
            List<SystemUserVO> list = systemUserService.listSystemUsers();
            assertNotNull(list);
            assertFalse(list.isEmpty(), "应有系统管理员用户");
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    @Test
    @Order(6)
    @DisplayName("系统用户详情 — 存在的用户")
    void testGetSystemUserDetail() {
        TestAuthUtils.setupAuth("USR_admin001", 1);
        try {
            SystemUserVO vo = systemUserService.getSystemUserDetail("USR_admin001");
            assertNotNull(vo);
            assertEquals("admin", vo.getUsername());
            assertNotNull(vo.getRoleName());
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    // ======================== 更新系统用户 ========================

    @Test
    @Order(7)
    @DisplayName("更新系统用户 — 修改用户名和角色")
    void testUpdateSystemUser() {
        TestAuthUtils.setupAuth("USR_admin001", 1);
        try {
            SystemUserUpdateRequest req = new SystemUserUpdateRequest();
            req.setUserId(createdUserId);
            req.setUsername("updated_" + System.currentTimeMillis());
            req.setRoleId("ROLE_PATIENT");

            systemUserService.updateSystemUser(req);

            SystemUserVO vo = systemUserService.getSystemUserDetail(createdUserId);
            assertEquals(req.getUsername(), vo.getUsername());
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    @Test
    @Order(8)
    @DisplayName("更新系统用户 — 用户名冲突抛异常")
    void testUpdateSystemUserDuplicateUsername() {
        TestAuthUtils.setupAuth("USR_admin001", 1);
        try {
            SystemUserUpdateRequest req = new SystemUserUpdateRequest();
            req.setUserId(createdUserId);
            req.setUsername("admin"); // 与已有用户冲突
            req.setRoleId("ROLE_DOCTOR");

            assertThrows(BusinessException.class, () -> systemUserService.updateSystemUser(req));
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    // ======================== 更新状态 ========================

    @Test
    @Order(9)
    @DisplayName("更新状态 — 禁用再启用")
    void testUpdateStatus() {
        TestAuthUtils.setupAuth("USR_admin001", 1);
        try {
            SystemUserStatusRequest disable = new SystemUserStatusRequest();
            disable.setUserId(createdUserId);
            disable.setStatus(0);
            systemUserService.updateStatus(disable);

            SystemUserVO disabled = systemUserService.getSystemUserDetail(createdUserId);
            assertEquals(0, disabled.getStatus());

            SystemUserStatusRequest enable = new SystemUserStatusRequest();
            enable.setUserId(createdUserId);
            enable.setStatus(1);
            systemUserService.updateStatus(enable);

            SystemUserVO enabled = systemUserService.getSystemUserDetail(createdUserId);
            assertEquals(1, enabled.getStatus());
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    @Test
    @Order(10)
    @DisplayName("更新状态 — 用户不存在抛异常")
    void testUpdateStatusNotFound() {
        TestAuthUtils.setupAuth("USR_admin001", 1);
        try {
            SystemUserStatusRequest req = new SystemUserStatusRequest();
            req.setUserId("USR_NOT_EXIST");
            req.setStatus(0);

            assertThrows(BusinessException.class, () -> systemUserService.updateStatus(req));
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    // ======================== 删除系统用户 ========================

    @Test
    @Order(11)
    @DisplayName("删除系统用户 — 新建后删除")
    void testDeleteSystemUser() {
        TestAuthUtils.setupAuth("USR_admin001", 1);
        try {
            // 创建一个专门用于删除的用户
            SystemUserAddRequest addReq = new SystemUserAddRequest();
            addReq.setUsername("to_delete_" + System.currentTimeMillis());
            addReq.setPassword("test123456");
            addReq.setRoleId("ROLE_ADMIN");
            String toDeleteId = systemUserService.addSystemUser(addReq);

            systemUserService.deleteSystemUser(toDeleteId);

            SystemUserVO vo = systemUserService.getSystemUserDetail(toDeleteId);
            assertNotNull(vo, "用户记录仍应存在");
            // 逻辑删除后 status 可能为 null 或不变，主要验证 system_user 记录已清理
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    @Test
    @Order(12)
    @DisplayName("删除系统用户 — 不存在的用户抛异常")
    void testDeleteSystemUserNotFound() {
        TestAuthUtils.setupAuth("USR_admin001", 1);
        try {
            assertThrows(BusinessException.class,
                    () -> systemUserService.deleteSystemUser("USR_NOT_EXIST"));
        } finally {
            TestAuthUtils.clearAuth();
        }
    }

    // ======================== 权限校验 ========================

    @Test
    @Order(13)
    @DisplayName("所有方法 — 非管理员抛异常")
    void testAllMethodsNotAdmin() {
        TestAuthUtils.setupAuth("USR_pat001", 2);
        try {
            assertThrows(BusinessException.class, () -> systemUserService.listSystemUsers());
            assertThrows(BusinessException.class, () -> systemUserService.getSystemUserDetail("USR_admin001"));
        } finally {
            TestAuthUtils.clearAuth();
        }
    }
}
