-- =============================================================
-- 新增检查医生（Radiologist）角色
-- 执行方式：可用于增量更新已有数据库或首次初始化
-- 首次初始化时由 flyway/liquibase 或手动执行
-- =============================================================

SET NAMES utf8mb4;
USE cloudbrain;

-- =============================================================
-- 1. 新增角色
-- =============================================================
INSERT IGNORE INTO `role` (`role_id`, `role_name`, `role_code`, `description`) VALUES
('ROLE_RADIOLOGIST', '检查医生', 'radiologist', '影像科医师，负责影像上传与初诊');

-- =============================================================
-- 2. 新增权限（如尚未存在）
--    权限 ID 沿用现有体系：
--    6  — 影像管理（菜单级）
--    61 — 影像上传（按钮级）
--    62 — 影像列表（按钮级）
--    63 — 影像初诊（按钮级）
-- =============================================================
INSERT IGNORE INTO `permission` (`permission_id`, `permission_name`, `permission_code`, `parent_id`, `type`, `path`, `sort_order`) VALUES
('6',  '影像管理', 'image:manage',      '0', 0, NULL, 6),
('61', '影像上传', 'image:upload',      '6', 1, NULL, 1),
('62', '影像列表', 'image:list',        '6', 1, NULL, 2),
('63', '影像初诊', 'image:diagnosis',   '6', 1, NULL, 3);

-- =============================================================
-- 3. 为检查医生分配权限
-- =============================================================
INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`) VALUES
-- 预约管理（查看挂号记录）
('ROLE_RADIOLOGIST', '2'),  ('ROLE_RADIOLOGIST', '23'),
-- 诊疗管理（检查管理）
('ROLE_RADIOLOGIST', '3'),  ('ROLE_RADIOLOGIST', '33'),
-- 患者管理（查看患者列表）
('ROLE_RADIOLOGIST', '4'),  ('ROLE_RADIOLOGIST', '42'),
-- 影像管理（上传、列表、初诊）
('ROLE_RADIOLOGIST', '6'),  ('ROLE_RADIOLOGIST', '61'),
('ROLE_RADIOLOGIST', '62'), ('ROLE_RADIOLOGIST', '63');

-- =============================================================
-- 4. 创建检查医生示例用户（密码: 123456）
--    如需创建自己的检查医生账号，修改下面的用户名/密码即可
-- =============================================================
INSERT IGNORE INTO `user` (`user_id`, `username`, `password`, `real_name`, `phone`, `user_type`, `status`) VALUES
('USR_rad001', 'radiologist1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '刘影像', '13800000012', 3, 1);

-- 为示例用户分配角色
INSERT IGNORE INTO `user_role` (`user_id`, `role_id`) VALUES
('USR_rad001', 'ROLE_RADIOLOGIST');

-- =============================================================
-- 5. 为示例用户创建医生记录（关联到内科，可后续修改为影像科）
-- =============================================================
INSERT IGNORE INTO `doctor` (`doctor_id`, `user_id`, `department_id`, `title`, `specialty`, `introduction`, `consultation_fee`, `max_daily_patients`, `available`) VALUES
('DOC_011', 'USR_rad001', 'DEPT_001', '主治医师', '影像诊断', '影像科医师，负责影像上传与初诊报告', 0.00, 50, 1);
