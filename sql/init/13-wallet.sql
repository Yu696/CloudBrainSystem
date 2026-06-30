-- =============================================================
-- 阶段三补充：钱包功能
-- patient 表新增 balance 字段 + 钱包交易记录表
-- =============================================================

USE cloudbrain;

-- 患者表新增钱包余额
ALTER TABLE patient ADD COLUMN `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '钱包余额' AFTER `status`;

-- 钱包交易记录表
CREATE TABLE `wallet_transaction` (
    `id`              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `transaction_id`  VARCHAR(32)     NOT NULL                 COMMENT '交易唯一标识',
    `patient_id`      VARCHAR(32)     NOT NULL                 COMMENT '患者 ID',
    `type`            TINYINT         NOT NULL                 COMMENT '类型：0=充值 1=挂花费 2=药费 3=检查费',
    `amount`          DECIMAL(10,2)   NOT NULL                 COMMENT '交易金额（充正/消负）',
    `balance_after`   DECIMAL(10,2)   NOT NULL                 COMMENT '交易后余额',
    `ref_id`          VARCHAR(32)     DEFAULT NULL             COMMENT '关联业务 ID',
    `remark`          VARCHAR(255)    DEFAULT NULL             COMMENT '备注',
    `create_time`     DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_transaction_id` (`transaction_id`),
    KEY `idx_patient_id` (`patient_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包交易记录表';
