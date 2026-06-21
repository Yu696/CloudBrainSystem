-- =============================================================
-- 通知服务
-- 未来微服务：cloudbrain_notify
-- =============================================================

USE cloudbrain;

-- 1. 通知记录表
CREATE TABLE `notification` (
    `id`              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `notification_id` VARCHAR(32)     NOT NULL                 COMMENT '通知唯一标识',
    `recipient_id`    VARCHAR(32)     NOT NULL                 COMMENT '接收人（user_id）',
    `title`           VARCHAR(100)    NOT NULL                 COMMENT '通知标题',
    `content`         TEXT            NOT NULL                 COMMENT '通知内容',
    `notification_type` TINYINT       NOT NULL                 COMMENT '类型：0=告警 1=系统通知 2=审核结果 3=排班提醒',
    `source_service`  VARCHAR(50)     DEFAULT NULL             COMMENT '来源服务',
    `source_id`       VARCHAR(32)     DEFAULT NULL             COMMENT '关联业务 ID',
    `is_read`         TINYINT         NOT NULL DEFAULT 0       COMMENT '是否已读：0=未读 1=已读',
    `read_time`       DATETIME        DEFAULT NULL             COMMENT '阅读时间',
    `create_time`     DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_notification_id` (`notification_id`),
    KEY `idx_recipient_id` (`recipient_id`),
    KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知记录表';

-- 2. WebSocket 会话表
CREATE TABLE `websocket_session` (
    `id`              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `session_id`      VARCHAR(64)     NOT NULL                 COMMENT '会话唯一标识',
    `user_id`         VARCHAR(32)     NOT NULL                 COMMENT '用户 ID',
    `connected_at`    DATETIME        NOT NULL                 COMMENT '连接时间',
    `last_heartbeat`  DATETIME        DEFAULT NULL             COMMENT '最后心跳时间',
    `disconnected_at` DATETIME        DEFAULT NULL             COMMENT '断开时间',
    `status`          TINYINT         NOT NULL                 COMMENT '状态：0=离线 1=在线',
    `client_info`     VARCHAR(255)    DEFAULT NULL             COMMENT '客户端信息',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session_id` (`session_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='WebSocket会话表';

-- 3. 告警规则表
CREATE TABLE `alert_rule` (
    `id`                   BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `rule_id`              VARCHAR(32)     NOT NULL                 COMMENT '规则唯一标识',
    `rule_name`            VARCHAR(100)    NOT NULL                 COMMENT '规则名称',
    `rule_type`            TINYINT         NOT NULL                 COMMENT '类型：0=处方风险 1=库存预警 2=系统异常',
    `condition_expression` TEXT            NOT NULL                 COMMENT '触发条件（JSON）',
    `severity`             TINYINT         NOT NULL DEFAULT 1       COMMENT '严重级别：1=低 2=中 3=高',
    `recipients`           TEXT            DEFAULT NULL             COMMENT '通知对象（JSON）',
    `is_active`            TINYINT         NOT NULL DEFAULT 1       COMMENT '是否启用：0=禁用 1=启用',
    `create_time`          DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rule_id` (`rule_id`),
    KEY `idx_rule_type` (`rule_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警规则表';
