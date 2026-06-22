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

-- 患者账号
INSERT INTO `user` (`user_id`, `username`, `password`, `real_name`, `phone`, `user_type`, `status`) VALUES
('USR_pat001',  'patient1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试患者', '13800000011', 2, 1);

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
('USR_doc010',   'ROLE_DOCTOR'),
('USR_pat001',   'ROLE_PATIENT');

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

-- ---------- 7. 患者 ----------
-- 关联了登录账号的患者
INSERT INTO `patient` (`patient_id`, `user_id`, `medical_record_no`, `name`, `id_card`, `gender`, `birth_date`, `phone`, `address`, `blood_type`, `status`) VALUES
('PAT_001', 'USR_pat001', 'MR202400001', '张三', '110101199001011234', 1, '1990-01-01', '13900000101', '北京市朝阳区XX小区1-101', 'A', 1);

-- 未关联登录账号的患者
INSERT INTO `patient` (`patient_id`, `medical_record_no`, `name`, `id_card`, `gender`, `birth_date`, `phone`, `address`, `blood_type`, `status`) VALUES
('PAT_002', 'MR202400002', '李四', '110101199203152345', 2, '1992-03-15', '13900000102', '北京市海淀区XX路2号', 'B', 1),
('PAT_003', 'MR202400003', '王五', '110101198507203456', 1, '1985-07-20', '13900000103', '北京市丰台区XX街3号', 'O', 1);

-- ---------- 8. 预约记录（测试待诊列表） ----------
INSERT INTO `appointment` (`appointment_id`, `patient_id`, `doctor_id`, `department_id`, `schedule_id`, `time_slot_id`, `appointment_date`, `time_slot_desc`, `appointment_type`, `symptoms`, `source`, `status`, `payment_status`, `total_fee`) VALUES
('APT_test001', 'PAT_001', 'DOC_001', 'DEPT_001', '', '', CURDATE(), '09:00-09:30', 1, '头痛、发热三天，体温38.5℃', 1, 1, 1, 20.00),
('APT_test002', 'PAT_002', 'DOC_001', 'DEPT_001', '', '', CURDATE(), '10:00-10:30', 1, '胸闷气短一周，活动后加重', 1, 1, 1, 20.00),
('APT_test003', 'PAT_003', 'DOC_002', 'DEPT_001', '', '', CURDATE(), '09:30-10:00', 2, '多饮多尿半月余，体重下降3kg', 1, 1, 1, 15.00);

-- ---------- 9. 病历记录（测试病历详情） ----------
INSERT INTO `medical_record` (`record_id`, `patient_id`, `doctor_id`, `appointment_id`, `chief_complaint`, `present_illness`, `past_history`, `personal_history`, `family_history`, `physical_exam`, `auxiliary_exam`, `diagnosis`, `treatment_opinion`, `status`, `is_ai_generated`, `diagnosis_time`, `create_time`) VALUES
('REC_test001', 'PAT_001', 'DOC_001', 'APT_test001',
    '头痛发热三天，体温最高38.5℃', '患者于3天前无明显诱因出现头痛，以双侧颞部为主，伴发热，体温最高38.5℃，无畏寒寒战，自服布洛芬后体温可降至37.5℃左右，停药后复升。',
    '既往体健，无高血压、糖尿病史。', '生于北京，无外地久居史，无烟酒嗜好。', '父母体健，无遗传病史。',
    'T 38.2℃, P 92次/分, R 20次/分, BP 120/80mmHg。咽部充血，扁桃体I°肿大，双肺呼吸音清，未闻及干湿啰音。',
    '血常规：WBC 12.5×10⁹/L, NEUT% 82%，CRP 48mg/L。新冠抗原阴性。', '急性上呼吸道感染', '1. 头孢呋辛酯 0.25g bid po ×3天 2. 布洛芬 0.2g prn 3. 多饮水，注意休息', 1, 0, NOW(), NOW()),

('REC_test002', 'PAT_002', 'DOC_001', 'APT_test002',
    '胸闷气短一周，活动后加重', '患者于一周前无明显诱因出现胸闷，伴气短，活动后加重，休息后可缓解，夜间可平卧。无心悸、胸痛。',
    '高血压病史5年，口服硝苯地平缓释片30mg qd，血压控制可。', '吸烟史10年，10支/天。', '父亲有高血压、冠心病史。',
    'T 36.5℃, P 78次/分, R 18次/分, BP 145/85mmHg。双肺呼吸音清，心率78次/分，律齐，各瓣膜听诊区未闻及病理性杂音。双下肢无水肿。',
    '心电图：窦性心律，ST-T改变。', '冠状动脉粥样硬化性心脏病（待查）', '1. 建议行冠脉CTA检查 2. 阿司匹林 100mg qd 3. 阿托伐他汀 20mg qn 4. 低盐低脂饮食', 1, 0, NOW(), NOW()),

('REC_test003', 'PAT_003', 'DOC_002', 'APT_test003',
    '多饮多尿半月，体重下降', '患者近半月来无明显诱因出现口干多饮，每日饮水量约4000ml，尿量增多，夜尿2-3次，伴体重下降约3kg，无发热。',
    '既往体健，无传染病史。', '会计，久坐。无烟酒嗜好。', '母亲有2型糖尿病史。',
    'T 36.8℃, P 76次/分, R 18次/分, BP 118/76mmHg。BMI 23.5。',
    '空腹血糖：8.9mmol/L，餐后2h血糖：15.2mmol/L，HbA1c：7.8%。', '2型糖尿病', '1. 二甲双胍缓释片 0.5g bid 2. 糖尿病饮食指导 3. 适量运动 4. 每周监测血糖', 1, 0, NOW(), NOW());

-- ---------- 10. 处方（测试处方页面） ----------
INSERT INTO `prescription` (`prescription_id`, `record_id`, `patient_id`, `doctor_id`, `prescription_desc`, `total_amount`, `status`, `audit_status`, `diagnosis_at`, `create_time`) VALUES
('PRS_test001', 'REC_test001', 'PAT_001', 'DOC_001', '上感处方', 85.60, 2, 2, NOW(), NOW()),
('PRS_test002', 'REC_test003', 'PAT_003', 'DOC_002', '糖尿病处方', 120.50, 2, 2, NOW(), NOW());

-- ---------- 11. 处方明细 ----------
INSERT INTO `prescription_item` (`item_id`, `prescription_id`, `drug_id`, `drug_name`, `spec`, `dosage`, `frequency`, `administration`, `days`, `quantity`, `unit_price`, `subtotal`) VALUES
('PRI_t01', 'PRS_test001', 'DRUG_001', '头孢呋辛酯片', '0.25g×12片', '0.25g', 'bid', '口服', 3, 1, 45.60, 45.60),
('PRI_t02', 'PRS_test001', 'DRUG_002', '布洛芬缓释胶囊', '0.3g×20粒', '0.3g', 'qd', '口服', 5, 1, 40.00, 40.00),
('PRI_t03', 'PRS_test002', 'DRUG_003', '盐酸二甲双胍缓释片', '0.5g×30片', '0.5g', 'bid', '口服', 30, 1, 65.50, 65.50),
('PRI_t04', 'PRS_test002', 'DRUG_004', '阿卡波糖片', '50mg×30片', '50mg', 'tid', '口服', 15, 1, 55.00, 55.00);

-- ---------- 12. 检查单（测试检查单页面） ----------
INSERT INTO `examination_order` (`order_id`, `record_id`, `patient_id`, `doctor_id`, `exam_category`, `exam_name`, `exam_purpose`, `amount`, `status`, `is_ai_recommended`, `create_time`) VALUES
('EXO_test001', 'REC_test001', 'PAT_001', 'DOC_001', 0, '血常规', '明确感染严重程度', 35.00, 3, 0, NOW()),
('EXO_test002', 'REC_test001', 'PAT_001', 'DOC_001', 1, 'X光胸片', '排除肺部感染', 150.00, 3, 0, NOW()),
('EXO_test003', 'REC_test002', 'PAT_002', 'DOC_001', 1, '冠脉CTA', '明确冠脉狭窄程度', 1800.00, 2, 0, NOW()),
('EXO_test004', 'REC_test003', 'PAT_003', 'DOC_002', 0, '糖化血红蛋白', '评估近三个月血糖水平', 80.00, 3, 0, NOW());

-- ---------- 13. 检查结果 ----------
INSERT INTO `examination_result` (`result_id`, `order_id`, `result_data`, `reference_range`, `is_abnormal`, `ai_analysis`, `doctor_opinion`, `result_time`, `create_time`) VALUES
('EXR_test001', 'EXO_test001', '{"WBC":12.5,"NEUT%":82,"LYM%":12,"RBC":4.8,"HGB":145,"PLT":210,"CRP":48}', 'WBC:3.5-9.5×10⁹/L, NEUT%:40-75%, CRP:<8mg/L', 1,
    '白细胞计数及中性粒细胞比例明显升高，C反应蛋白显著升高，提示细菌性感染。建议结合临床症状使用抗生素治疗。',
    '符合急性上呼吸道感染的实验室改变，同意AI分析意见。',
    NOW(), NOW()),
('EXR_test002', 'EXO_test002', '{"检查所见":"双肺纹理清晰，未见明显实质性病变。心影大小形态正常。双侧肋膈角锐利。"}', '无异常', 0,
    '胸部X线影像未见明显异常，肺部感染可排除。',
    '胸片正常，排除下呼吸道感染。',
    NOW(), NOW()),
('EXR_test003', 'EXO_test004', '{"HbA1c":7.8,"空腹血糖":8.9}', 'HbA1c:<6.0%, 空腹血糖:3.9-6.1mmol/L', 1,
    '糖化血红蛋白显著升高至7.8%，提示近2-3个月平均血糖水平偏高，符合2型糖尿病诊断标准。',
    '确诊2型糖尿病，建议启动药物治疗配合生活方式干预。',
    NOW(), NOW());
