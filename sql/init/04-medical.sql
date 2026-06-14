-- =============================================================
-- M03 诊疗记录
-- 未来微服务：cloudbrain_medical
-- =============================================================

USE cloudbrain;

-- 1. 病历表
CREATE TABLE `medical_record` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `record_id`         VARCHAR(32)     NOT NULL                 COMMENT '病历唯一标识',
    `patient_id`        VARCHAR(32)     NOT NULL                 COMMENT '患者 ID',
    `doctor_id`         VARCHAR(32)     NOT NULL                 COMMENT '医生 ID',
    `appointment_id`    VARCHAR(32)     DEFAULT NULL             COMMENT '关联挂号 ID',
    `chief_complaint`   TEXT            DEFAULT NULL             COMMENT '主诉',
    `present_illness`   TEXT            DEFAULT NULL             COMMENT '现病史',
    `past_history`      TEXT            DEFAULT NULL             COMMENT '既往史',
    `personal_history`  TEXT            DEFAULT NULL             COMMENT '个人史',
    `family_history`    TEXT            DEFAULT NULL             COMMENT '家族史',
    `physical_exam`     TEXT            DEFAULT NULL             COMMENT '体格检查',
    `auxiliary_exam`    TEXT            DEFAULT NULL             COMMENT '辅助检查',
    `diagnosis`         TEXT            DEFAULT NULL             COMMENT '初步诊断',
    `treatment_opinion` TEXT            DEFAULT NULL             COMMENT '治疗意见',
    `status`            TINYINT         NOT NULL DEFAULT 0       COMMENT '状态：0=草稿 1=已完成 2=已归档',
    `is_ai_generated`   TINYINT         NOT NULL DEFAULT 0       COMMENT '是否 AI 生成',
    `ai_generation_id`  VARCHAR(32)     DEFAULT NULL             COMMENT '关联 AI 生成记录 ID',
    `diagnosis_time`    DATETIME        DEFAULT NULL             COMMENT '诊断时间',
    `create_time`       DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    `update_time`       DATETIME        NOT NULL DEFAULT NOW() ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_record_id` (`record_id`),
    KEY `idx_patient_id` (`patient_id`),
    KEY `idx_doctor_id` (`doctor_id`),
    KEY `idx_appointment_id` (`appointment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='病历表';

-- 2. 处方表
CREATE TABLE `prescription` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `prescription_id`   VARCHAR(32)     NOT NULL                 COMMENT '处方唯一标识',
    `record_id`         VARCHAR(32)     NOT NULL                 COMMENT '关联病历 ID',
    `patient_id`        VARCHAR(32)     NOT NULL                 COMMENT '患者 ID',
    `doctor_id`         VARCHAR(32)     NOT NULL                 COMMENT '医生 ID',
    `prescription_desc` TEXT            DEFAULT NULL             COMMENT '处方描述',
    `total_amount`      DECIMAL(10,2)   NOT NULL DEFAULT 0.00   COMMENT '总金额',
    `status`            TINYINT         NOT NULL                 COMMENT '状态：0=草稿 1=待审核 2=已审核 3=已发药 4=已作废',
    `audit_status`      TINYINT         NOT NULL DEFAULT 0       COMMENT 'AI 审核状态：0=未审核 1=通过 2=有风险 3=失败',
    `audit_id`          VARCHAR(32)     DEFAULT NULL             COMMENT '关联 AI 审核记录 ID',
    `diagnosis_at`      DATETIME        DEFAULT NULL             COMMENT '开方时间',
    `create_time`       DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    `update_time`       DATETIME        NOT NULL DEFAULT NOW() ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_prescription_id` (`prescription_id`),
    KEY `idx_record_id` (`record_id`),
    KEY `idx_patient_id` (`patient_id`),
    KEY `idx_doctor_id` (`doctor_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='处方表';

-- 3. 处方明细表
CREATE TABLE `prescription_item` (
    `id`              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `item_id`         VARCHAR(32)     NOT NULL                 COMMENT '明细唯一标识',
    `prescription_id` VARCHAR(32)     NOT NULL                 COMMENT '处方 ID',
    `drug_id`         VARCHAR(32)     NOT NULL                 COMMENT '药品 ID',
    `drug_name`       VARCHAR(100)    NOT NULL                 COMMENT '药品名称',
    `spec`            VARCHAR(50)     DEFAULT NULL             COMMENT '药品规格',
    `dosage`          VARCHAR(50)     NOT NULL                 COMMENT '单次剂量',
    `frequency`       VARCHAR(50)     NOT NULL                 COMMENT '用药频次',
    `administration`  VARCHAR(50)     DEFAULT NULL             COMMENT '给药途径',
    `days`            INT             NOT NULL                 COMMENT '用药天数',
    `quantity`        INT             NOT NULL                 COMMENT '数量',
    `unit_price`      DECIMAL(10,2)   NOT NULL                 COMMENT '单价',
    `subtotal`        DECIMAL(10,2)   NOT NULL                 COMMENT '小计',
    `remark`          VARCHAR(255)    DEFAULT NULL             COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_item_id` (`item_id`),
    KEY `idx_prescription_id` (`prescription_id`),
    KEY `idx_drug_id` (`drug_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='处方明细表';

-- 4. 检查单表
CREATE TABLE `examination_order` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `order_id`          VARCHAR(32)     NOT NULL                 COMMENT '检查单唯一标识',
    `record_id`         VARCHAR(32)     NOT NULL                 COMMENT '关联病历 ID',
    `patient_id`        VARCHAR(32)     NOT NULL                 COMMENT '患者 ID',
    `doctor_id`         VARCHAR(32)     NOT NULL                 COMMENT '医生 ID',
    `exam_category`     TINYINT         NOT NULL                 COMMENT '类别：0=实验室 1=影像 2=功能检查',
    `exam_name`         VARCHAR(100)    NOT NULL                 COMMENT '检查项目名称',
    `exam_purpose`      TEXT            DEFAULT NULL             COMMENT '检查目的',
    `amount`            DECIMAL(10,2)   NOT NULL DEFAULT 0.00   COMMENT '费用',
    `status`            TINYINT         NOT NULL DEFAULT 0       COMMENT '状态：0=已开单 1=已缴费 2=检查中 3=已完成 4=已取消',
    `is_ai_recommended` TINYINT         NOT NULL DEFAULT 0       COMMENT '是否 AI 推荐',
    `create_time`       DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY `idx_record_id` (`record_id`),
    KEY `idx_patient_id` (`patient_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='检查单表';

-- 5. 检查结果表
CREATE TABLE `examination_result` (
    `id`               BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `result_id`        VARCHAR(32)     NOT NULL                 COMMENT '结果唯一标识',
    `order_id`         VARCHAR(32)     NOT NULL                 COMMENT '关联检查单 ID',
    `result_data`      TEXT            DEFAULT NULL             COMMENT '检查结果数据（JSON）',
    `reference_range`  VARCHAR(255)    DEFAULT NULL             COMMENT '参考范围',
    `is_abnormal`      TINYINT         NOT NULL DEFAULT 0       COMMENT '是否异常：0=正常 1=异常',
    `ai_analysis`      TEXT            DEFAULT NULL             COMMENT 'AI 分析解读',
    `doctor_opinion`   TEXT            DEFAULT NULL             COMMENT '医生诊断意见',
    `report_file_url`  VARCHAR(255)    DEFAULT NULL             COMMENT '报告文件 URL',
    `result_time`      DATETIME        DEFAULT NULL             COMMENT '结果时间',
    `create_time`      DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_result_id` (`result_id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='检查结果表';

-- 6. 处方审核记录表
CREATE TABLE `prescription_audit_log` (
    `id`                  BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `audit_id`            VARCHAR(32)     NOT NULL                 COMMENT '审核唯一标识',
    `prescription_id`     VARCHAR(32)     NOT NULL                 COMMENT '处方 ID',
    `patient_id`          VARCHAR(32)     NOT NULL                 COMMENT '患者 ID',
    `audit_type`          TINYINT         NOT NULL                 COMMENT '类型：0=AI 审核 1=药师审核',
    `risk_level`          TINYINT         NOT NULL DEFAULT 0       COMMENT '风险等级：0=无 1=低 2=中 3=高',
    `audit_result`        TEXT            DEFAULT NULL             COMMENT '审核结果详情（JSON）',
    `drug_interactions`   TEXT            DEFAULT NULL             COMMENT '药物相互作用检测结果',
    `suggestions`         TEXT            DEFAULT NULL             COMMENT '用药建议',
    `audit_time`          DATETIME        NOT NULL DEFAULT NOW()   COMMENT '审核时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_audit_id` (`audit_id`),
    KEY `idx_prescription_id` (`prescription_id`),
    KEY `idx_risk_level` (`risk_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='处方审核记录表';
