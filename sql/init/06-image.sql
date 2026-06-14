-- =============================================================
-- M05 影像管理
-- 未来微服务：cloudbrain_image
-- =============================================================

USE cloudbrain;

-- 1. 医学影像表
CREATE TABLE `medical_image` (
    `id`              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `image_id`        VARCHAR(32)     NOT NULL                 COMMENT '影像唯一标识',
    `examination_id`  VARCHAR(32)     DEFAULT NULL             COMMENT '关联检查单 ID',
    `patient_id`      VARCHAR(32)     NOT NULL                 COMMENT '患者 ID',
    `image_name`      VARCHAR(100)    NOT NULL                 COMMENT '影像文件名',
    `image_type`      TINYINT         NOT NULL                 COMMENT '影像类型：0=DICOM 1=JPG 2=PNG 3=其他',
    `file_path`       VARCHAR(255)    NOT NULL                 COMMENT '存储路径',
    `file_size`       BIGINT          DEFAULT NULL             COMMENT '文件大小（字节）',
    `width`           INT             DEFAULT NULL             COMMENT '图像宽度（像素）',
    `height`          INT             DEFAULT NULL             COMMENT '图像高度（像素）',
    `modality`        VARCHAR(20)     DEFAULT NULL             COMMENT '成像设备（CT/MRI/X光/超声）',
    `body_part`       VARCHAR(50)     DEFAULT NULL             COMMENT '身体部位',
    `study_uid`       VARCHAR(64)     DEFAULT NULL             COMMENT 'DICOM Study UID',
    `series_uid`      VARCHAR(64)     DEFAULT NULL             COMMENT 'DICOM Series UID',
    `upload_time`     DATETIME        NOT NULL DEFAULT NOW()   COMMENT '上传时间',
    `uploader_id`     VARCHAR(32)     DEFAULT NULL             COMMENT '上传人',
    `status`          TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：0=异常 1=正常',
    `create_time`     DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_image_id` (`image_id`),
    KEY `idx_examination_id` (`examination_id`),
    KEY `idx_patient_id` (`patient_id`),
    KEY `idx_modality` (`modality`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医学影像表';

-- 2. 影像标注表
CREATE TABLE `annotation` (
    `id`              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `annotation_id`   VARCHAR(32)     NOT NULL                 COMMENT '标注唯一标识',
    `image_id`        VARCHAR(32)     NOT NULL                 COMMENT '关联影像 ID',
    `annotator_id`    VARCHAR(32)     NOT NULL                 COMMENT '标注人',
    `annotation_type` VARCHAR(20)     NOT NULL                 COMMENT '标注类型',
    `coordinates`     TEXT            NOT NULL                 COMMENT '标注坐标数据（JSON）',
    `label`           VARCHAR(100)    DEFAULT NULL             COMMENT '标注标签',
    `measurement`     VARCHAR(100)    DEFAULT NULL             COMMENT '测量值',
    `description`     TEXT            DEFAULT NULL             COMMENT '标注描述',
    `create_time`     DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    `update_time`     DATETIME        NOT NULL DEFAULT NOW() ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_annotation_id` (`annotation_id`),
    KEY `idx_image_id` (`image_id`),
    KEY `idx_annotator_id` (`annotator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='影像标注表';

-- 3. 格式转换日志表
CREATE TABLE `conversion_log` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `image_id`      VARCHAR(32)     NOT NULL                 COMMENT '关联影像 ID',
    `source_format` VARCHAR(10)     NOT NULL                 COMMENT '源格式',
    `target_format` VARCHAR(10)     NOT NULL                 COMMENT '目标格式',
    `output_path`   VARCHAR(255)    NOT NULL                 COMMENT '输出路径',
    `output_size`   BIGINT          DEFAULT NULL             COMMENT '输出文件大小',
    `status`        TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：0=失败 1=成功',
    `create_time`   DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_image_id` (`image_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='格式转换日志表';

-- 4. 存储配置表
CREATE TABLE `storage_config` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `config_id`     VARCHAR(32)     NOT NULL                 COMMENT '配置唯一标识',
    `storage_type`  TINYINT         NOT NULL                 COMMENT '类型：0=本地 1=PACS 2=云备份',
    `endpoint`      VARCHAR(255)    DEFAULT NULL             COMMENT '存储端点',
    `bucket`        VARCHAR(100)    DEFAULT NULL             COMMENT '存储桶/路径',
    `access_key`    VARCHAR(100)    DEFAULT NULL             COMMENT '访问密钥（加密存储）',
    `secret_key`    VARCHAR(100)    DEFAULT NULL             COMMENT '秘密密钥（加密存储）',
    `max_capacity`  BIGINT          DEFAULT NULL             COMMENT '最大容量（字节）',
    `used_capacity` BIGINT          NOT NULL DEFAULT 0       COMMENT '已使用容量',
    `priority`      INT             NOT NULL DEFAULT 0       COMMENT '优先级',
    `status`        TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：0=禁用 1=启用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_id` (`config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='存储配置表';
