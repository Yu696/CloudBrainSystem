-- =============================================================
-- M07 药库管理
-- 未来微服务：cloudbrain_pharmacy
-- =============================================================

USE cloudbrain;

-- 1. 药品表
CREATE TABLE `drug` (
    `id`              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `drug_id`         VARCHAR(32)     NOT NULL                 COMMENT '药品唯一标识',
    `drug_code`       VARCHAR(50)     DEFAULT NULL             COMMENT '药品编码（院内编码）',
    `drug_name`       VARCHAR(100)    NOT NULL                 COMMENT '药品名称',
    `generic_name`    VARCHAR(100)    DEFAULT NULL             COMMENT '通用名',
    `ingredients`     VARCHAR(255)    DEFAULT NULL             COMMENT '主要成分',
    `spec`            VARCHAR(50)     DEFAULT NULL             COMMENT '规格',
    `dosage_form`     VARCHAR(50)     DEFAULT NULL             COMMENT '剂型',
    `manufacturer`    VARCHAR(100)    DEFAULT NULL             COMMENT '生产厂家',
    `unit`            VARCHAR(10)     NOT NULL                 COMMENT '单位（盒/瓶/支）',
    `unit_price`      DECIMAL(10,2)   NOT NULL                 COMMENT '单价',
    `purchase_price`  DECIMAL(10,2)   DEFAULT NULL             COMMENT '进价',
    `usage_method`    VARCHAR(255)    DEFAULT NULL             COMMENT '用法',
    `cautious_crowd`  TEXT            DEFAULT NULL             COMMENT '禁忌人群',
    `side_effects`    TEXT            DEFAULT NULL             COMMENT '副作用',
    `drug_category`   VARCHAR(50)     DEFAULT NULL             COMMENT '药品分类',
    `prescription_type` TINYINT       NOT NULL DEFAULT 1       COMMENT '处方类型：0=OTC 1=处方药',
    `status`          TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：0=停用 1=正常',
    `create_time`     DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    `update_time`     DATETIME        NOT NULL DEFAULT NOW() ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_drug_id` (`drug_id`),
    UNIQUE KEY `uk_drug_code` (`drug_code`),
    KEY `idx_drug_name` (`drug_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药品表';

-- 2. 药品库存表
CREATE TABLE `drug_stock` (
    `id`              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `drug_id`         VARCHAR(32)     NOT NULL                 COMMENT '药品 ID',
    `warehouse_id`    VARCHAR(32)     DEFAULT NULL             COMMENT '仓库 ID',
    `current_stock`   INT             NOT NULL DEFAULT 0       COMMENT '当前库存数量',
    `min_stock`       INT             NOT NULL DEFAULT 10      COMMENT '最低库存预警线',
    `max_stock`       INT             NOT NULL DEFAULT 1000    COMMENT '最高库存限制',
    `batch_no`        VARCHAR(50)     DEFAULT NULL             COMMENT '批次号',
    `production_date` DATE            DEFAULT NULL             COMMENT '生产日期',
    `expiry_date`     DATE            DEFAULT NULL             COMMENT '有效期',
    `status`          TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：0=过期 1=正常 2=停用',
    `create_time`     DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    `update_time`     DATETIME        NOT NULL DEFAULT NOW() ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_drug_id` (`drug_id`),
    KEY `idx_expiry_date` (`expiry_date`),
    KEY `idx_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药品库存表';

-- 3. 仓库表
CREATE TABLE `warehouse` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `warehouse_id`  VARCHAR(32)     NOT NULL                 COMMENT '仓库唯一标识',
    `name`          VARCHAR(100)    NOT NULL                 COMMENT '仓库名称',
    `location`      VARCHAR(255)    DEFAULT NULL             COMMENT '仓库位置',
    `admin_id`      VARCHAR(32)     DEFAULT NULL             COMMENT '管理员 ID',
    `type`          TINYINT         NOT NULL DEFAULT 0       COMMENT '类型：0=药库 1=药房',
    `status`        TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：0=停用 1=正常',
    `create_time`   DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库表';

-- 4. 出药记录表
CREATE TABLE `ship_record` (
    `id`              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `record_id`       VARCHAR(32)     NOT NULL                 COMMENT '出药记录唯一标识',
    `prescription_id` VARCHAR(32)     NOT NULL                 COMMENT '关联处方 ID',
    `patient_id`      VARCHAR(32)     NOT NULL                 COMMENT '患者 ID',
    `operator_id`     VARCHAR(32)     DEFAULT NULL             COMMENT '操作人',
    `ship_num`        INT             NOT NULL                 COMMENT '出药总件数',
    `pay_amount`      DECIMAL(10,2)   NOT NULL DEFAULT 0.00   COMMENT '支付金额',
    `ship_time`       DATETIME        NOT NULL DEFAULT NOW()   COMMENT '出药时间',
    `print_status`    TINYINT         NOT NULL DEFAULT 0       COMMENT '打印状态：0=未打印 1=已打印',
    `remark`          VARCHAR(255)    DEFAULT NULL             COMMENT '备注',
    `create_time`     DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_record_id` (`record_id`),
    KEY `idx_prescription_id` (`prescription_id`),
    KEY `idx_patient_id` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出药记录表';

-- 5. 库存预警表
CREATE TABLE `stock_alert` (
    `id`             BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `drug_id`        VARCHAR(32)     NOT NULL                 COMMENT '药品 ID',
    `alert_type`     TINYINT         NOT NULL                 COMMENT '预警类型：0=低库存 1=过期预警 2=库存积压',
    `current_stock`  INT             NOT NULL                 COMMENT '当前库存',
    `threshold`      INT             NOT NULL                 COMMENT '预警阈值',
    `alert_message`  VARCHAR(255)    DEFAULT NULL             COMMENT '预警信息',
    `is_handled`     TINYINT         NOT NULL DEFAULT 0       COMMENT '是否已处理：0=未 1=已处理',
    `handled_by`     VARCHAR(32)     DEFAULT NULL             COMMENT '处理人',
    `handle_time`    DATETIME        DEFAULT NULL             COMMENT '处理时间',
    `create_time`    DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_drug_id` (`drug_id`),
    KEY `idx_alert_type` (`alert_type`),
    KEY `idx_is_handled` (`is_handled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存预警表';
