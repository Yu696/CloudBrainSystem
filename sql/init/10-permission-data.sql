-- =============================================================
-- 阶段一 权限种子数据
-- 执行顺序：应在 09-init-data.sql 之后执行
-- =============================================================

SET NAMES utf8mb4;
USE cloudbrain;

-- ---------- 1. 清理无效的角色-权限关联 ----------
-- 删除 role_permission 表中 role_id 格式不正确的记录（数字 ID 而非 ROLE_xxx 格式）
DELETE FROM `role_permission` WHERE `role_id` NOT LIKE 'ROLE\_%';

-- ---------- 2. 菜单级权限（type=0） ----------
INSERT INTO `permission` (`permission_id`, `permission_name`, `permission_code`, `parent_id`, `type`, `path`, `sort_order`) VALUES
('1',  '用户管理',   'user:manage',        '0', 0, NULL, 1),
('2',  '预约管理',   'appointment:manage', '0', 0, NULL, 2),
('3',  '诊疗管理',   'medical:manage',     '0', 0, NULL, 3),
('4',  '患者管理',   'patient:manage',     '0', 0, NULL, 4),
('5',  '系统管理',   'system:manage',      '0', 0, NULL, 5);

-- ---------- 3. 按钮级权限（type=1） ----------
-- 用户管理子权限
INSERT INTO `permission` (`permission_id`, `permission_name`, `permission_code`, `parent_id`, `type`, `path`, `sort_order`) VALUES
('11', '用户列表',   'user:list',       '1', 1, NULL, 1),
('12', '分配角色',   'user:assign',     '1', 1, NULL, 2),
('13', '权限配置',   'user:permission', '1', 1, NULL, 3);

-- 预约管理子权限
INSERT INTO `permission` (`permission_id`, `permission_name`, `permission_code`, `parent_id`, `type`, `path`, `sort_order`) VALUES
('21', '预约挂号',   'appointment:book',   '2', 1, NULL, 1),
('22', '排班管理',   'schedule:manage',    '2', 1, NULL, 2),
('23', '挂号记录',   'appointment:list',   '2', 1, NULL, 3),
('24', '取消预约',   'appointment:cancel', '2', 1, NULL, 4);

-- 诊疗管理子权限
INSERT INTO `permission` (`permission_id`, `permission_name`, `permission_code`, `parent_id`, `type`, `path`, `sort_order`) VALUES
('31', '病历管理',   'medical:record',       '3', 1, NULL, 1),
('32', '处方管理',   'medical:prescription', '3', 1, NULL, 2),
('33', '检查管理',   'medical:exam',         '3', 1, NULL, 3);

-- 患者管理子权限
INSERT INTO `permission` (`permission_id`, `permission_name`, `permission_code`, `parent_id`, `type`, `path`, `sort_order`) VALUES
('41', '新建档案',   'patient:create', '4', 1, NULL, 1),
('42', '患者列表',   'patient:list',   '4', 1, NULL, 2),
('43', '编辑档案',   'patient:edit',   '4', 1, NULL, 3);

-- 系统管理子权限
INSERT INTO `permission` (`permission_id`, `permission_name`, `permission_code`, `parent_id`, `type`, `path`, `sort_order`) VALUES
('51', '系统用户',   'system:user',   '5', 1, NULL, 1),
('52', '系统配置',   'system:config', '5', 1, NULL, 2);

-- ---------- 4. 角色-权限关联 ----------
-- 超级管理员：全部权限
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES
('ROLE_ADMIN', '1'),  ('ROLE_ADMIN', '11'), ('ROLE_ADMIN', '12'), ('ROLE_ADMIN', '13'),
('ROLE_ADMIN', '2'),  ('ROLE_ADMIN', '21'), ('ROLE_ADMIN', '22'), ('ROLE_ADMIN', '23'), ('ROLE_ADMIN', '24'),
('ROLE_ADMIN', '3'),  ('ROLE_ADMIN', '31'), ('ROLE_ADMIN', '32'), ('ROLE_ADMIN', '33'),
('ROLE_ADMIN', '4'),  ('ROLE_ADMIN', '41'), ('ROLE_ADMIN', '42'), ('ROLE_ADMIN', '43'),
('ROLE_ADMIN', '5'),  ('ROLE_ADMIN', '51'), ('ROLE_ADMIN', '52');

-- 医生：诊疗、预约（挂号记录）、患者查看
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES
('ROLE_DOCTOR', '3'), ('ROLE_DOCTOR', '31'), ('ROLE_DOCTOR', '32'), ('ROLE_DOCTOR', '33'),
('ROLE_DOCTOR', '2'), ('ROLE_DOCTOR', '23'),
('ROLE_DOCTOR', '4'), ('ROLE_DOCTOR', '42'), ('ROLE_DOCTOR', '43');

-- 患者：预约挂号、挂号记录、取消预约、病历查看
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES
('ROLE_PATIENT', '2'), ('ROLE_PATIENT', '21'), ('ROLE_PATIENT', '23'), ('ROLE_PATIENT', '24'),
('ROLE_PATIENT', '3'), ('ROLE_PATIENT', '31');
