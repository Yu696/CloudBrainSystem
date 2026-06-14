-- =============================================================
-- 阶段一 初始化数据
-- 角色、管理员、科室、医生
-- =============================================================

USE cloudbrain;

-- ---------- 1. 角色 ----------
INSERT INTO `role` (`role_id`, `role_name`, `role_code`, `description`) VALUES
('ROLE_ADMIN',  '超级管理员', 'admin',  '系统最高权限'),
('ROLE_DOCTOR', '医生',       'doctor', '医生角色'),
('ROLE_PATIENT','患者',       'patient','患者角色');

-- ---------- 2. 用户（密码统一为 123456，BCrypt 加密） ----------
-- admin / 123456
INSERT INTO `user` (`user_id`, `username`, `password`, `real_name`, `phone`, `user_type`, `status`) VALUES
('USR_admin001', 'admin',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800000000', 1, 1);

-- 医生账号
INSERT INTO `user` (`user_id`, `username`, `password`, `real_name`, `phone`, `user_type`, `status`) VALUES
('USR_doc001', 'doctor1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张伟',   '13800000001', 0, 1),
('USR_doc002', 'doctor2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李娜',   '13800000002', 0, 1),
('USR_doc003', 'doctor3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王强',   '13800000003', 0, 1),
('USR_doc004', 'doctor4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '赵丽',   '13800000004', 0, 1),
('USR_doc005', 'doctor5', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '孙鹏',   '13800000005', 0, 1),
('USR_doc006', 'doctor6', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '陈敏',   '13800000006', 0, 1),
('USR_doc007', 'doctor7', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '刘洋',   '13800000007', 0, 1),
('USR_doc008', 'doctor8', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '周婷',   '13800000008', 0, 1),
('USR_doc009', 'doctor9', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '吴刚',   '13800000009', 0, 1),
('USR_doc010', 'doctor10','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '黄芳',   '13800000010', 0, 1);

-- ---------- 3. 用户-角色关联 ----------
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES
('USR_admin001', 'ROLE_ADMIN'),
('USR_doc001',   'ROLE_DOCTOR'),
('USR_doc002',   'ROLE_DOCTOR'),
('USR_doc003',   'ROLE_DOCTOR'),
('USR_doc004',   'ROLE_DOCTOR'),
('USR_doc005',   'ROLE_DOCTOR'),
('USR_doc006',   'ROLE_DOCTOR'),
('USR_doc007',   'ROLE_DOCTOR'),
('USR_doc008',   'ROLE_DOCTOR'),
('USR_doc009',   'ROLE_DOCTOR'),
('USR_doc010',   'ROLE_DOCTOR');

-- ---------- 4. 系统管理用户 ----------
INSERT INTO `system_user` (`user_id`, `admin_type`) VALUES
('USR_admin001', 0);

-- ---------- 5. 科室 ----------
INSERT INTO `department` (`department_id`, `name`, `parent_id`, `category`, `description`, `location`, `sort_order`, `status`) VALUES
('DEPT_001', '内科',         '0',            '内科',   '内科疾病诊治',           '门诊楼 2F', 1, 1),
('DEPT_002', '外科',         '0',            '外科',   '外科手术及创伤诊治',      '门诊楼 3F', 2, 1),
('DEPT_003', '妇产科',       '0',            '妇产科', '妇女及产科疾病诊治',      '门诊楼 4F', 3, 1),
('DEPT_004', '儿科',         '0',            '儿科',   '儿童疾病诊治',           '门诊楼 1F', 4, 1),
('DEPT_005', '眼科',         '0',            '其他',   '眼部疾病诊治',           '门诊楼 5F', 5, 1),
('DEPT_006', '耳鼻喉科',     '0',            '其他',   '耳鼻喉疾病诊治',          '门诊楼 5F', 6, 1),
('DEPT_007', '口腔科',       '0',            '其他',   '口腔疾病诊治',           '门诊楼 6F', 7, 1),
('DEPT_008', '皮肤科',       '0',            '其他',   '皮肤疾病诊治',           '门诊楼 6F', 8, 1),
('DEPT_009', '神经内科',     '0',            '内科',   '神经系统疾病诊治',        '门诊楼 2F', 9, 1),
('DEPT_010', '心血管内科',   '0',            '内科',   '心血管系统疾病诊治',      '门诊楼 2F', 10, 1),
('DEPT_011', '骨科',         '0',            '外科',   '骨骼关节疾病诊治',        '门诊楼 3F', 11, 1),
('DEPT_012', '泌尿外科',     '0',            '外科',   '泌尿系统疾病诊治',        '门诊楼 3F', 12, 1);

-- ---------- 6. 医生 ----------
INSERT INTO `doctor` (`doctor_id`, `user_id`, `department_id`, `title`, `specialty`, `introduction`, `consultation_fee`, `max_daily_patients`, `available`) VALUES
('DOC_001', 'USR_doc001', 'DEPT_001', '主任医师', '高血压、冠心病',               '从事内科临床工作20年，擅长心血管疾病诊治。',               20.00, 30, 1),
('DOC_002', 'USR_doc002', 'DEPT_001', '副主任医师', '糖尿病、甲状腺疾病',          '内分泌科专家，擅长糖尿病综合管理。',                        15.00, 30, 1),
('DOC_003', 'USR_doc003', 'DEPT_002', '主任医师', '普外科手术、腹腔镜',           '普外科主任，完成腹腔镜手术超2000例。',                      25.00, 25, 1),
('DOC_004', 'USR_doc004', 'DEPT_003', '主任医师', '产科高危妊娠',                  '从事妇产科工作15年，高危妊娠管理经验丰富。',                 20.00, 20, 1),
('DOC_005', 'USR_doc005', 'DEPT_004', '副主任医师', '儿童呼吸系统疾病',            '儿科专家，擅长儿童哮喘及过敏性疾病的诊治。',                 15.00, 35, 1),
('DOC_006', 'USR_doc006', 'DEPT_005', '主任医师', '白内障、青光眼',               '眼科主任，擅长白内障超声乳化手术。',                        20.00, 30, 1),
('DOC_007', 'USR_doc007', 'DEPT_006', '副主任医师', '过敏性鼻炎、中耳炎',          '耳鼻喉科专家，擅长鼻内镜微创手术。',                        15.00, 30, 1),
('DOC_008', 'USR_doc008', 'DEPT_007', '主治医师', '口腔正畸、种植牙',             '口腔全科医生，擅长牙齿正畸和种植修复。',                    15.00, 20, 1),
('DOC_009', 'USR_doc009', 'DEPT_009', '主任医师', '脑血管疾病、帕金森病',          '神经内科学科带头人，脑血管疾病诊治经验丰富。',               25.00, 25, 1),
('DOC_010', 'USR_doc010', 'DEPT_010', '副主任医师', '心力衰竭、心律失常',          '心内科专家，擅长介入治疗和心脏康复。',                      20.00, 25, 1);
