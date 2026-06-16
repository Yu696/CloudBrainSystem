-- =============================================================
-- M01 用户中心 + M08 系统管理
-- 未来微服务：cloudbrain_user
-- =============================================================

USE cloudbrain;

-- 1. 角色表
CREATE TABLE `role` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `role_id`       VARCHAR(32)     NOT NULL                 COMMENT '角色唯一标识',
    `role_name`     VARCHAR(50)     NOT NULL                 COMMENT '角色名称',
    `role_code`     VARCHAR(50)     NOT NULL                 COMMENT '角色编码',
    `description`   VARCHAR(255)    DEFAULT NULL             COMMENT '角色描述',
    `status`        TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：0=禁用 1=启用',
    `create_time`   DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    `update_time`   DATETIME        NOT NULL DEFAULT NOW() ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_id` (`role_id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 2. 用户表
CREATE TABLE `user` (
    `id`              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `user_id`         VARCHAR(32)     NOT NULL                 COMMENT '用户唯一标识',
    `username`        VARCHAR(50)     NOT NULL                 COMMENT '用户名（登录账号）',
    `password`        VARCHAR(255)    NOT NULL                 COMMENT 'BCrypt 加密密码',
    `real_name`       VARCHAR(50)     NOT NULL                 COMMENT '真实姓名',
    `phone`           VARCHAR(20)     DEFAULT NULL             COMMENT '手机号',
    `email`           VARCHAR(100)    DEFAULT NULL             COMMENT '邮箱',
    `avatar_url`      VARCHAR(255)    DEFAULT NULL             COMMENT '头像 URL',
    `user_type`       TINYINT         NOT NULL DEFAULT 0       COMMENT '类型：0=医生 1=管理员 2=患者',
    `status`          TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：0=禁用 1=启用',
    `last_login_time` DATETIME        DEFAULT NULL             COMMENT '最后登录时间',
    `create_time`     DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    `update_time`     DATETIME        NOT NULL DEFAULT NOW() ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除：0=正常 1=删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_real_name` (`real_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表（医生/管理员/患者通用）';

-- 3. 用户-角色关联表
CREATE TABLE `user_role` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `user_id`     VARCHAR(32) NOT NULL                 COMMENT '用户 ID',
    `role_id`     VARCHAR(32) NOT NULL                 COMMENT '角色 ID',
    `create_time` DATETIME    NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-角色关联表';

-- 4. 权限表
CREATE TABLE `permission` (
    `id`              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `permission_id`   VARCHAR(32)     NOT NULL                 COMMENT '权限唯一标识',
    `permission_name` VARCHAR(50)     NOT NULL                 COMMENT '权限名称',
    `permission_code` VARCHAR(100)    NOT NULL                 COMMENT '权限编码',
    `parent_id`       VARCHAR(32)     DEFAULT '0'              COMMENT '父权限 ID',
    `type`            TINYINT         NOT NULL                 COMMENT '类型：1=菜单 2=按钮 3=API',
    `path`            VARCHAR(255)    DEFAULT NULL             COMMENT '前端路由/API 路径',
    `sort_order`      INT             NOT NULL DEFAULT 0       COMMENT '排序号',
    `create_time`     DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_id` (`permission_id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 5. 角色-权限关联表
CREATE TABLE `role_permission` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `role_id`       VARCHAR(32) NOT NULL                 COMMENT '角色 ID',
    `permission_id` VARCHAR(32) NOT NULL                 COMMENT '权限 ID',
    `create_time`   DATETIME    NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-权限关联表';

-- 6. 系统管理用户表（M08）
CREATE TABLE `system_user` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `user_id`       VARCHAR(32) NOT NULL                 COMMENT '关联用户 ID',
    `admin_type`    TINYINT     NOT NULL DEFAULT 0       COMMENT '管理员类型：0=超级管理员 1=系统管理员 2=审计员',
    `last_login_ip` VARCHAR(50) DEFAULT NULL             COMMENT '最后登录 IP',
    `create_time`   DATETIME    NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统管理用户表';

-- 7. 登录日志表
CREATE TABLE `login_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `user_id`     VARCHAR(32)  NOT NULL                 COMMENT '用户 ID',
    `ip_address`  VARCHAR(50)  DEFAULT NULL             COMMENT '登录 IP',
    `device`      VARCHAR(100) DEFAULT NULL             COMMENT '设备信息',
    `login_time`  DATETIME     NOT NULL DEFAULT NOW()   COMMENT '登录时间',
    `status`      TINYINT      NOT NULL                 COMMENT '结果：0=失败 1=成功',
    `fail_reason` VARCHAR(255) DEFAULT NULL             COMMENT '失败原因',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';
