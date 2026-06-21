-- =============================================================
-- M04 AI 辅助诊断
-- 未来微服务：cloudbrain_ai
-- =============================================================

USE cloudbrain;

-- 1. 智能分诊记录表
CREATE TABLE `triage_log` (
    `id`                        BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `triage_id`                 VARCHAR(32)     NOT NULL                 COMMENT '分诊唯一标识',
    `patient_id`                VARCHAR(32)     NOT NULL                 COMMENT '患者 ID',
    `chief_complaint`           TEXT            NOT NULL                 COMMENT '主诉内容（症状描述）',
    `recommended_department_id` VARCHAR(32)     DEFAULT NULL             COMMENT '推荐科室 ID',
    `recommended_department_name` VARCHAR(50)   DEFAULT NULL             COMMENT '推荐科室名称',
    `recommended_doctor_id`     VARCHAR(32)     DEFAULT NULL             COMMENT '推荐医生 ID',
    `recommended_doctor_name`   VARCHAR(50)     DEFAULT NULL             COMMENT '推荐医生姓名',
    `confidence_score`          DECIMAL(5,2)    DEFAULT NULL             COMMENT '置信度（0-100）',
    `analysis_detail`           TEXT            DEFAULT NULL             COMMENT '分析详情（JSON）',
    `status`                    TINYINT         NOT NULL DEFAULT 0       COMMENT '状态：0=处理中 1=完成 2=失败',
    `ai_model`                  VARCHAR(50)     DEFAULT NULL             COMMENT '使用的 AI 模型',
    `response_time_ms`          INT             DEFAULT NULL             COMMENT '响应耗时（毫秒）',
    `create_time`               DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_triage_id` (`triage_id`),
    KEY `idx_patient_id` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能分诊记录表';

-- 2. AI 诊断结果表
CREATE TABLE `diagnosis_result` (
    `id`                    BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `diagnosis_id`          VARCHAR(32)     NOT NULL                 COMMENT '诊断唯一标识',
    `patient_id`            VARCHAR(32)     NOT NULL                 COMMENT '患者 ID',
    `doctor_id`             VARCHAR(32)     DEFAULT NULL             COMMENT '关联医生 ID',
    `diagnosis_type`        TINYINT         NOT NULL                 COMMENT '类型：0=症状分析 1=影像诊断',
    `symptom_data`          TEXT            DEFAULT NULL             COMMENT '症状数据（JSON）',
    `disease_matches`       TEXT            DEFAULT NULL             COMMENT '疾病匹配结果（JSON）',
    `department_recommendations` TEXT       DEFAULT NULL             COMMENT '科室推荐（JSON）',
    `confidence_score`      DECIMAL(5,2)    DEFAULT NULL             COMMENT '综合置信度',
    `analysis_result`       TEXT            DEFAULT NULL             COMMENT '分析结论',
    `status`                TINYINT         NOT NULL DEFAULT 0       COMMENT '状态：0=待分析 1=分析中 2=完成 3=失败',
    `ai_model`              VARCHAR(50)     DEFAULT NULL             COMMENT 'AI 模型',
    `create_time`           DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    `update_time`           DATETIME        NOT NULL DEFAULT NOW() ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_diagnosis_id` (`diagnosis_id`),
    KEY `idx_patient_id` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI诊断结果表';

-- 3. AI 生成日志表
CREATE TABLE `generation_log` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `generation_id`     VARCHAR(32)     NOT NULL                 COMMENT '生成唯一标识',
    `target_type`       TINYINT         NOT NULL                 COMMENT '目标类型：0=病历 1=处方 2=诊断报告',
    `target_id`         VARCHAR(32)     DEFAULT NULL             COMMENT '关联目标 ID',
    `source_text`       TEXT            NOT NULL                 COMMENT '输入原文',
    `generated_content` TEXT            DEFAULT NULL             COMMENT '生成内容',
    `is_confirmed`      TINYINT         NOT NULL DEFAULT 0       COMMENT '是否经人工确认',
    `confirmed_by`      VARCHAR(32)     DEFAULT NULL             COMMENT '确认人',
    `prompt_template_id` VARCHAR(32)    DEFAULT NULL             COMMENT 'Prompt 模板 ID',
    `response_time_ms`  INT             DEFAULT NULL             COMMENT '响应耗时（毫秒）',
    `status`            TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：1=成功 2=失败',
    `create_time`       DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_generation_id` (`generation_id`),
    KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI生成日志表';

-- 4. Prompt 模板表
CREATE TABLE `prompt_template` (
    `id`              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `template_id`     VARCHAR(32)     NOT NULL                 COMMENT '模板唯一标识',
    `template_name`   VARCHAR(100)    NOT NULL                 COMMENT '模板名称',
    `template_type`   TINYINT         NOT NULL                 COMMENT '类型：0=分诊 1=病历生成 2=处方审核',
    `department_id`   VARCHAR(32)     DEFAULT NULL             COMMENT '适用科室',
    `content`         TEXT            NOT NULL                 COMMENT 'Prompt 模板内容',
    `version`         INT             NOT NULL DEFAULT 1       COMMENT '版本号',
    `variables`       VARCHAR(255)    DEFAULT NULL             COMMENT '变量定义（JSON）',
    `status`          TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：0=禁用 1=启用',
    `create_time`     DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    `update_time`     DATETIME        NOT NULL DEFAULT NOW() ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_id` (`template_id`),
    KEY `idx_template_type` (`template_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Prompt模板表';

-- 5. 疾病知识库表
CREATE TABLE `disease_knowledge` (
    `id`                  BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `disease_id`          VARCHAR(32)     NOT NULL                 COMMENT '疾病唯一标识',
    `disease_name`        VARCHAR(100)    NOT NULL                 COMMENT '疾病名称',
    `icd_code`            VARCHAR(20)     DEFAULT NULL             COMMENT 'ICD 编码',
    `category`            VARCHAR(50)     DEFAULT NULL             COMMENT '疾病分类',
    `related_department_id` VARCHAR(32)   DEFAULT NULL             COMMENT '关联科室 ID',
    `symptoms`            TEXT            DEFAULT NULL             COMMENT '典型症状（JSON）',
    `risk_factors`        TEXT            DEFAULT NULL             COMMENT '风险因素',
    `diagnosis_basis`     TEXT            DEFAULT NULL             COMMENT '诊断依据',
    `treatment_plan`      TEXT            DEFAULT NULL             COMMENT '治疗方案参考',
    `similarity_diseases` TEXT            DEFAULT NULL             COMMENT '相似疾病（JSON）',
    `status`              TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：0=禁用 1=启用',
    `create_time`         DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    `update_time`         DATETIME        NOT NULL DEFAULT NOW() ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_disease_id` (`disease_id`),
    UNIQUE KEY `uk_disease_name` (`disease_name`),
    KEY `idx_icd_code` (`icd_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='疾病知识库表';
