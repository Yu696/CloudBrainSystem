-- =============================================================
-- M02 患者档案
-- 未来微服务：cloudbrain_patient
-- =============================================================

USE cloudbrain;

CREATE TABLE `patient` (
    `id`                  BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `patient_id`          VARCHAR(32)     NOT NULL                 COMMENT '患者唯一标识',
    `user_id`             VARCHAR(32)     DEFAULT NULL             COMMENT '关联用户 ID',
    `medical_record_no`   VARCHAR(32)     NOT NULL                 COMMENT '病历号（系统生成）',
    `name`                VARCHAR(50)     NOT NULL                 COMMENT '患者姓名',
    `id_card`             VARCHAR(18)     NOT NULL                 COMMENT '身份证号',
    `gender`              TINYINT         NOT NULL                 COMMENT '性别：0=未知 1=男 2=女',
    `birth_date`          DATE            NOT NULL                 COMMENT '出生日期',
    `age`                 INT             DEFAULT NULL                 COMMENT '年龄（业务层计算，定期更新）',
    `phone`               VARCHAR(20)     NOT NULL                 COMMENT '联系电话',
    `emergency_phone`     VARCHAR(20)     DEFAULT NULL             COMMENT '紧急联系人电话',
    `address`             VARCHAR(255)    DEFAULT NULL             COMMENT '家庭住址',
    `blood_type`          VARCHAR(10)     DEFAULT NULL             COMMENT '血型：A/B/AB/O/其他',
    `allergy_history`     TEXT            DEFAULT NULL             COMMENT '过敏史',
    `genetic_diseases`    TEXT            DEFAULT NULL             COMMENT '遗传病史',
    `medical_history`     TEXT            DEFAULT NULL             COMMENT '既往病史',
    `qr_code_url`         VARCHAR(255)    DEFAULT NULL             COMMENT '二维码 URL',
    `id_card_photo_front` VARCHAR(255)    DEFAULT NULL             COMMENT '身份证正面照',
    `id_card_photo_back`  VARCHAR(255)    DEFAULT NULL             COMMENT '身份证反面照',
    `source`              TINYINT         NOT NULL DEFAULT 1       COMMENT '建档来源：1=医院建档 2=在线注册 3=转诊',
    `status`              TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：0=归档 1=正常',
    `create_time`         DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    `update_time`         DATETIME        NOT NULL DEFAULT NOW() ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`             TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除：0=正常 1=删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_patient_id` (`patient_id`),
    UNIQUE KEY `uk_medical_record_no` (`medical_record_no`),
    UNIQUE KEY `uk_id_card` (`id_card`),
    KEY `idx_name` (`name`),
    KEY `idx_phone` (`phone`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者档案表';
