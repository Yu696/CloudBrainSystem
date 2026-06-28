-- =============================================================
-- AI 调用统一日志表
-- 替换原有的 triage_log、diagnosis_result、generation_log
-- call_type: 0=分诊 1=诊断 2=处方审核
-- =============================================================

USE cloudbrain;

-- 旧表保留历史数据不动，新建统一日志表
CREATE TABLE `ai_call_log` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `call_id`           VARCHAR(32)     NOT NULL                 COMMENT '调用唯一标识',
    `call_type`         TINYINT         NOT NULL                 COMMENT '调用类型：0=分诊 1=诊断 2=处方审核',
    `patient_id`        VARCHAR(32)     DEFAULT NULL             COMMENT '患者 ID',
    `doctor_id`         VARCHAR(32)     DEFAULT NULL             COMMENT '医生 ID',
    `input_summary`     VARCHAR(255)    DEFAULT NULL             COMMENT '输入摘要（列表展示用）',
    `input_data`        TEXT            DEFAULT NULL             COMMENT '完整输入数据（JSON）',
    `output_data`       TEXT            DEFAULT NULL             COMMENT 'AI 输出原始数据（JSON）',
    `confidence_score`  DECIMAL(5,2)    DEFAULT NULL             COMMENT '置信度',
    `ai_model`          VARCHAR(50)     DEFAULT NULL             COMMENT 'AI 模型',
    `response_time_ms`  INT             DEFAULT NULL             COMMENT '响应耗时（毫秒）',
    `status`            TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：1=成功 2=失败',
    `error_message`     VARCHAR(500)    DEFAULT NULL             COMMENT '失败原因',
    `create_time`       DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_call_id` (`call_id`),
    KEY `idx_call_type` (`call_type`),
    KEY `idx_patient_id` (`patient_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 调用日志表（统一日志）';
