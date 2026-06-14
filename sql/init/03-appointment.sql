-- =============================================================
-- M06 预约管理
-- 未来微服务：cloudbrain_appointment
-- =============================================================

USE cloudbrain;

-- 1. 科室表
CREATE TABLE `department` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `department_id` VARCHAR(32)     NOT NULL                 COMMENT '科室唯一标识',
    `name`          VARCHAR(50)     NOT NULL                 COMMENT '科室名称',
    `parent_id`     VARCHAR(32)     DEFAULT '0'              COMMENT '父科室 ID',
    `category`      VARCHAR(20)     NOT NULL                 COMMENT '科室分类：内科/外科/妇产科/儿科/其他',
    `description`   TEXT            DEFAULT NULL             COMMENT '科室简介',
    `location`      VARCHAR(100)    DEFAULT NULL             COMMENT '科室位置',
    `phone`         VARCHAR(20)     DEFAULT NULL             COMMENT '联系电话',
    `sort_order`    INT             NOT NULL DEFAULT 0       COMMENT '排序号',
    `status`        TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：0=停诊 1=正常',
    `create_time`   DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_department_id` (`department_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科室表';

-- 2. 医生信息表
CREATE TABLE `doctor` (
    `id`                  BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `doctor_id`           VARCHAR(32)     NOT NULL                 COMMENT '医生唯一标识',
    `user_id`             VARCHAR(32)     NOT NULL                 COMMENT '关联用户 ID',
    `department_id`       VARCHAR(32)     NOT NULL                 COMMENT '所属科室',
    `title`               VARCHAR(50)     DEFAULT NULL             COMMENT '职称',
    `specialty`           VARCHAR(255)    DEFAULT NULL             COMMENT '专业擅长',
    `introduction`        TEXT            DEFAULT NULL             COMMENT '医生简介',
    `consultation_fee`    DECIMAL(10,2)   NOT NULL DEFAULT 0.00   COMMENT '挂号费',
    `max_daily_patients`  INT             NOT NULL DEFAULT 30     COMMENT '每日最大接诊量',
    `available`           TINYINT         NOT NULL DEFAULT 1       COMMENT '是否出诊：0=否 1=是',
    `create_time`         DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    `update_time`         DATETIME        NOT NULL DEFAULT NOW() ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_doctor_id` (`doctor_id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_department_id` (`department_id`),
    KEY `idx_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生信息表';

-- 3. 医生排班表
CREATE TABLE `schedule` (
    `id`               BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `schedule_id`      VARCHAR(32)     NOT NULL                 COMMENT '排班唯一标识',
    `doctor_id`        VARCHAR(32)     NOT NULL                 COMMENT '医生 ID',
    `department_id`    VARCHAR(32)     NOT NULL                 COMMENT '科室 ID',
    `schedule_date`    DATE            NOT NULL                 COMMENT '排班日期',
    `work_shift`       TINYINT         NOT NULL                 COMMENT '班次：0=上午 1=下午 2=晚班',
    `start_time`       TIME            NOT NULL                 COMMENT '开始时间',
    `end_time`         TIME            NOT NULL                 COMMENT '结束时间',
    `slot_duration`    INT             NOT NULL DEFAULT 30      COMMENT '每时段时长（分钟）',
    `max_patients`     INT             NOT NULL DEFAULT 20      COMMENT '最大预约人数',
    `available_slots`  INT             NOT NULL DEFAULT 20      COMMENT '剩余号源数',
    `status`           TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：0=停诊 1=正常 2=已满',
    `remark`           VARCHAR(255)    DEFAULT NULL             COMMENT '备注',
    `create_time`      DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    `update_time`      DATETIME        NOT NULL DEFAULT NOW() ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_schedule_id` (`schedule_id`),
    UNIQUE KEY `uk_doctor_date_shift` (`doctor_id`, `schedule_date`, `work_shift`),
    KEY `idx_department_id` (`department_id`),
    KEY `idx_schedule_date` (`schedule_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生排班表';

-- 4. 时段表
CREATE TABLE `time_slot` (
    `id`               BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `slot_id`          VARCHAR(32) NOT NULL                 COMMENT '时段唯一标识',
    `schedule_id`      VARCHAR(32) NOT NULL                 COMMENT '排班 ID',
    `doctor_id`        VARCHAR(32) NOT NULL                 COMMENT '医生 ID',
    `start_time`       DATETIME    NOT NULL                 COMMENT '时段开始时间',
    `end_time`         DATETIME    NOT NULL                 COMMENT '时段结束时间',
    `status`           TINYINT     NOT NULL                 COMMENT '状态：0=可用 1=锁定 2=已预约 3=已完成 4=已释放',
    `lock_token`       VARCHAR(64) DEFAULT NULL             COMMENT '锁定令牌',
    `locked_by`        VARCHAR(32) DEFAULT NULL             COMMENT '锁定者',
    `lock_expiry_time` DATETIME    DEFAULT NULL             COMMENT '锁定过期时间',
    `appointment_id`   VARCHAR(32) DEFAULT NULL             COMMENT '关联预约 ID',
    `create_time`      DATETIME    NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_slot_id` (`slot_id`),
    UNIQUE KEY `uk_schedule_time` (`schedule_id`, `start_time`, `end_time`),
    KEY `idx_doctor_id` (`doctor_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='时段表';

-- 5. 预约/挂号记录表
CREATE TABLE `appointment` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `appointment_id`    VARCHAR(32)     NOT NULL                 COMMENT '预约唯一标识',
    `patient_id`        VARCHAR(32)     NOT NULL                 COMMENT '患者 ID',
    `doctor_id`         VARCHAR(32)     NOT NULL                 COMMENT '医生 ID',
    `department_id`     VARCHAR(32)     NOT NULL                 COMMENT '科室 ID',
    `schedule_id`       VARCHAR(32)     DEFAULT NULL             COMMENT '排班 ID',
    `time_slot_id`      VARCHAR(32)     DEFAULT NULL             COMMENT '时段 ID',
    `appointment_date`  DATE            NOT NULL                 COMMENT '预约日期',
    `time_slot_desc`    VARCHAR(50)     NOT NULL                 COMMENT '时段描述',
    `appointment_type`  TINYINT         NOT NULL DEFAULT 0       COMMENT '类型：0=普通门诊 1=专家门诊 2=急诊',
    `symptoms`          TEXT            DEFAULT NULL             COMMENT '症状描述（主诉）',
    `source`            TINYINT         NOT NULL DEFAULT 0       COMMENT '来源：0=在线 1=窗口 2=自助机',
    `status`            TINYINT         NOT NULL                 COMMENT '状态：0=待支付 1=待就诊 2=就诊中 3=已完成 4=已取消 5=已过期',
    `payment_status`    TINYINT         NOT NULL DEFAULT 0       COMMENT '支付状态：0=未支付 1=已支付 2=已退款',
    `total_fee`         DECIMAL(10,2)   NOT NULL DEFAULT 0.00   COMMENT '总费用',
    `cancel_reason`     VARCHAR(255)    DEFAULT NULL             COMMENT '取消原因',
    `create_time`       DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    `update_time`       DATETIME        NOT NULL DEFAULT NOW() ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_appointment_id` (`appointment_id`),
    KEY `idx_patient_id` (`patient_id`),
    KEY `idx_doctor_id` (`doctor_id`),
    KEY `idx_appointment_date` (`appointment_date`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约挂号记录表';

-- 6. 支付记录表
CREATE TABLE `payment` (
    `id`               BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `payment_id`       VARCHAR(32)     NOT NULL                 COMMENT '支付唯一标识',
    `appointment_id`   VARCHAR(32)     NOT NULL                 COMMENT '关联预约 ID',
    `patient_id`       VARCHAR(32)     NOT NULL                 COMMENT '患者 ID',
    `amount`           DECIMAL(10,2)   NOT NULL                 COMMENT '支付金额',
    `payment_method`   TINYINT         NOT NULL                 COMMENT '方式：0=医保卡 1=现金 2=扫码 3=银行卡',
    `payment_status`   TINYINT         NOT NULL                 COMMENT '状态：0=待支付 1=支付中 2=已支付 3=已退款',
    `trade_no`         VARCHAR(64)     DEFAULT NULL             COMMENT '第三方交易流水号',
    `payment_time`     DATETIME        DEFAULT NULL             COMMENT '支付时间',
    `refund_time`      DATETIME        DEFAULT NULL             COMMENT '退款时间',
    `create_time`      DATETIME        NOT NULL DEFAULT NOW()   COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_id` (`payment_id`),
    KEY `idx_appointment_id` (`appointment_id`),
    KEY `idx_patient_id` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';
