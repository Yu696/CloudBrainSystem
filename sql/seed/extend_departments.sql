-- =============================================================
-- 扩展科室体系：新增 6 个科室 + 对应医生 + 疾病知识库数据
-- 新增科室：呼吸内科、消化内科、内分泌科、风湿免疫科、肿瘤科、感染科
-- 新增医生：每个科室 1 名
-- 新增疾病知识：每个科室 3 条（18 条）
-- =============================================================

USE cloudbrain;

-- ==================== 1. 新增科室 ====================

INSERT INTO `department` (`department_id`, `name`, `parent_id`, `category`, `description`, `location`, `sort_order`, `status`) VALUES
('DEPT_013', '呼吸内科', '0', '内科', '呼吸系统疾病诊治', '门诊楼 2F', 13, 1),
('DEPT_014', '消化内科', '0', '内科', '消化系统疾病诊治', '门诊楼 2F', 14, 1),
('DEPT_015', '内分泌科', '0', '内科', '内分泌代谢疾病诊治', '门诊楼 2F', 15, 1),
('DEPT_016', '风湿免疫科', '0', '内科', '风湿免疫疾病诊治', '门诊楼 2F', 16, 1),
('DEPT_017', '肿瘤科', '0', '其他', '肿瘤综合诊治', '门诊楼 3F', 17, 1),
('DEPT_018', '感染科', '0', '其他', '感染性疾病诊治', '门诊楼 1F（发热门诊）', 18, 1);

-- ==================== 2. 新增用户（医生账号，密码统一为 123456） ====================

INSERT INTO `user` (`user_id`, `username`, `password`, `real_name`, `phone`, `user_type`, `status`) VALUES
('USR_doc011', 'doctor11', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '钱医生', '13800000011', 0, 1),
('USR_doc012', 'doctor12', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '何医生', '13800000012', 0, 1),
('USR_doc013', 'doctor13', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '冯医生', '13800000013', 0, 1),
('USR_doc014', 'doctor14', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '褚医生', '13800000014', 0, 1),
('USR_doc015', 'doctor15', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '蒋医生', '13800000015', 0, 1),
('USR_doc016', 'doctor16', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '韩医生', '13800000016', 0, 1);

-- ==================== 3. 用户-角色关联 ====================

INSERT INTO `user_role` (`user_id`, `role_id`) VALUES
('USR_doc011', 'ROLE_DOCTOR'),
('USR_doc012', 'ROLE_DOCTOR'),
('USR_doc013', 'ROLE_DOCTOR'),
('USR_doc014', 'ROLE_DOCTOR'),
('USR_doc015', 'ROLE_DOCTOR'),
('USR_doc016', 'ROLE_DOCTOR');

-- ==================== 4. 新增医生信息 ====================

INSERT INTO `doctor` (`doctor_id`, `user_id`, `department_id`, `title`, `specialty`, `introduction`, `consultation_fee`, `max_daily_patients`, `available`) VALUES
('DOC_011', 'USR_doc011', 'DEPT_013', '主任医师', '慢性阻塞性肺疾病、支气管哮喘',
 '呼吸科专家，从事呼吸内科临床工作25年，擅长慢阻肺和哮喘的规范化治疗。',
 20.00, 30, 1),
('DOC_012', 'USR_doc012', 'DEPT_014', '副主任医师', '消化性溃疡、肝硬化、胰腺炎',
 '消化内科专家，擅长内镜下诊断和治疗，完成胃肠镜超5000例。',
 15.00, 25, 1),
('DOC_013', 'USR_doc013', 'DEPT_015', '主任医师', '甲状腺疾病、糖尿病、痛风',
 '内分泌科专家，擅长甲状腺疾病和代谢性疾病的综合治疗。',
 20.00, 30, 1),
('DOC_014', 'USR_doc014', 'DEPT_016', '副主任医师', '类风湿关节炎、系统性红斑狼疮',
 '风湿免疫科专家，擅长自身免疫性疾病的早期诊断和个体化治疗。',
 15.00, 25, 1),
('DOC_015', 'USR_doc015', 'DEPT_017', '主任医师', '肺癌、乳腺癌、消化道肿瘤',
 '肿瘤科专家，擅长恶性肿瘤的化疗、靶向治疗和免疫治疗。',
 25.00, 20, 1),
('DOC_016', 'USR_doc016', 'DEPT_018', '副主任医师', '病毒性肝炎、结核病、HIV相关疾病',
 '感染科专家，从事感染性疾病诊治20年，对疑难感染有丰富经验。',
 15.00, 25, 1);

-- ==================== 5. 新增疾病知识库记录（DK_034 ~ DK_051） ====================

-- ----- 呼吸内科（DEPT_013）3条 -----

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000034', '慢性支气管炎', 'J42', '呼吸系统', 'DEPT_013',
 '["慢性咳嗽","咳痰","喘息","气短","反复呼吸道感染"]',
 '吸烟、空气污染、职业粉尘接触、反复感染、年龄',
 '咳嗽咳痰每年持续≥3个月、连续≥2年，排除其他原因',
 '戒烟、支气管扩张剂、祛痰药、氧疗、疫苗接种',
 '支气管哮喘、COPD、支气管扩张症', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000035', '支气管哮喘', 'J45.9', '呼吸系统', 'DEPT_013',
 '["发作性喘息","呼吸困难","胸闷","咳嗽（夜间/清晨加重）","哮鸣音"]',
 '过敏体质、家族史、呼吸道感染、运动、冷空气',
 '肺功能（可逆性气道阻塞）+ 激发/舒张试验',
 '吸入糖皮质激素+β2受体激动剂、白三烯调节剂、脱敏治疗',
 'COPD、心源性哮喘、声带功能障碍', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000036', '慢性阻塞性肺疾病', 'J44.9', '呼吸系统', 'DEPT_013',
 '["进行性呼吸困难","慢性咳嗽","咳痰","桶状胸","活动耐力下降"]',
 '吸烟（首要）、职业粉尘、生物燃料暴露、α1-AT缺乏',
 '肺功能（FEV1/FVC＜70%）+ 症状评估（CAT/mMRC评分）',
 '戒烟、长效支气管扩张剂、ICS、肺康复、长期氧疗、肺减容',
 '支气管哮喘、支气管扩张症、间质性肺病', 1);

-- ----- 消化内科（DEPT_014）3条 -----

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000037', '消化性溃疡', 'K27', '消化系统', 'DEPT_014',
 '["上腹周期性疼痛（空腹/夜间痛）","反酸嗳气","恶心呕吐","黑便","呕血"]',
 '幽门螺杆菌感染、NSAIDs药物、饮酒、吸烟、精神紧张',
 '胃镜+病理活检、幽门螺杆菌检测、上消化道钡餐',
 '根除Hp（四联疗法）、PPI抑酸、胃黏膜保护剂、手术（并发症）',
 '慢性胃炎、胃癌、胃食管反流病', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000038', '肝硬化', 'K74.6', '消化系统', 'DEPT_014',
 '["乏力","食欲减退","腹胀","黄疸","腹水","蜘蛛痣","脾大"]',
 '病毒性肝炎（乙肝/丙肝）、酒精、脂肪肝、自身免疫',
 '肝功能+凝血、肝脏超声/CT、FibroScan、肝活检',
 '病因治疗（抗病毒/戒酒）、保肝、并发症处理（腹水/出血）、肝移植',
 '慢性肝炎、肝癌、布加综合征', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000039', '急性胰腺炎', 'K85', '消化系统', 'DEPT_014',
 '["急性上腹剧痛放射至背部","恶心呕吐","发热","腹胀","腹膜刺激征"]',
 '胆结石、高脂血症、饮酒、ERCP术后、药物',
 '血淀粉酶/脂肪酶（≥3倍上限）+ 腹部CT',
 '禁食胃肠减压、补液、镇痛、抑制胰酶分泌、重症ICU支持',
 '消化性溃疡穿孔、肠梗阻、急性胆囊炎', 1);

-- ----- 内分泌科（DEPT_015）3条 -----

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000040', '甲状腺功能亢进症', 'E05.9', '内分泌系统', 'DEPT_015',
 '["心悸手抖","多汗怕热","体重下降","食欲亢进","突眼","烦躁易怒"]',
 '自身免疫（Graves病）、遗传、精神应激、碘摄入过量',
 '甲状腺功能（TSH↓、FT3/FT4↑）+ TRAb抗体、甲状腺B超',
 '抗甲状腺药物（甲巯咪唑/PTU）、放射性碘131、甲状腺次全切除',
 '焦虑症、单纯性甲状腺肿、桥本甲状腺炎', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000041', '痛风', 'M10', '内分泌系统', 'DEPT_015',
 '["关节红肿胀痛（首发大脚趾）","夜间突发","发热","反复发作致关节畸形"]',
 '高尿酸血症、高嘌呤饮食、饮酒、肥胖、肾功能不全',
 '关节液尿酸盐结晶+血尿酸＞420μmol/L、关节B超/CT',
 '急性期（NSAIDs/秋水仙碱）、降尿酸（别嘌醇/非布司他/苯溴马隆）',
 '化脓性关节炎、假性痛风、类风湿性关节炎', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000042', '骨质疏松症', 'M81.9', '内分泌系统', 'DEPT_015',
 '["骨痛（腰背为主）","身高变矮","驼背","脆性骨折（腕/髋/椎体）"]',
 '绝经、高龄、低钙摄入、缺乏运动、糖皮质激素、吸烟',
 'DXA骨密度（T≤-2.5）、X线、骨折风险评估（FRAX）',
 '钙剂+VitD、双膦酸盐、降钙素、PTH类似物、抗RANKL单抗',
 '骨软化症、多发性骨髓瘤、转移性骨肿瘤', 1);

-- ----- 风湿免疫科（DEPT_016）3条 -----

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000043', '类风湿性关节炎', 'M06.9', '风湿免疫系统', 'DEPT_016',
 '["对称性小关节肿痛（晨僵＞1h）","关节畸形","活动受限","皮下结节","乏力发热"]',
 '自身免疫、遗传（HLA-DR4）、吸烟、感染触发',
 'RF/抗CCP抗体↑、ESR/CRP↑、关节X线/MRI',
 'DMARDs（甲氨蝶呤/来氟米特）、生物制剂（TNF-α抑制剂）、激素',
 '骨关节炎、痛风、银屑病关节炎', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000044', '系统性红斑狼疮', 'M32', '风湿免疫系统', 'DEPT_016',
 '["面部蝶形红斑","关节痛","发热","光敏感","口腔溃疡","脱发","多器官受累"]',
 '自身免疫、雌激素、遗传、紫外线、药物诱发',
 'ANA/抗dsDNA抗体/抗Sm抗体阳性、补体降低、多系统受累',
 '羟氯喹、糖皮质激素、免疫抑制剂（霉酚酸酯/环磷酰胺）、生物制剂',
 '皮肌炎、混合性结缔组织病、药物性狼疮', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000045', '干燥综合征', 'M35.0', '风湿免疫系统', 'DEPT_016',
 '["口干（需频繁饮水）","眼干（异物感）","腮腺肿大","关节痛","龋齿多发"]',
 '自身免疫、遗传、感染触发',
 '抗SSA/抗SSB抗体阳性、唇腺活检（灶性淋巴细胞浸润）、Schirmer试验',
 '人工泪液/唾液、非甾体抗炎药、羟氯喹、免疫抑制剂',
 '类风湿性关节炎、IgG4相关病、药物性口干', 1);

-- ----- 肿瘤科（DEPT_017）3条 -----

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000046', '肺癌', 'C34.9', '肿瘤', 'DEPT_017',
 '["刺激性咳嗽","痰中带血","胸痛","气短","声音嘶哑","体重下降"]',
 '吸烟（首要）、二手烟、空气污染、职业暴露（石棉/氡）、家族史',
 '胸部CT+病理（支气管镜/穿刺活检/胸腔积液细胞学）、基因检测',
 '手术切除、化疗、靶向治疗（EGFR/ALK）、免疫治疗（PD-1/PD-L1）',
 '肺结核、肺炎、良性肺结节', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000047', '胃癌', 'C16.9', '肿瘤', 'DEPT_017',
 '["上腹隐痛","食欲减退","黑便","呕吐","体重明显下降","贫血"]',
 '幽门螺杆菌感染、慢性萎缩性胃炎、腌制饮食、吸烟饮酒、家族史',
 '胃镜+病理活检、上消化道钡餐、CT分期',
 '胃癌根治术、化疗（铂类/氟尿嘧啶类）、靶向（HER2）、免疫治疗',
 '消化性溃疡、胃淋巴瘤、胃间质瘤', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000048', '乳腺癌', 'C50', '肿瘤', 'DEPT_017',
 '["乳房无痛性肿块","乳头溢液","皮肤橘皮样改变","腋窝淋巴结肿大"]',
 '雌激素暴露、BRCA基因突变、初潮早/绝经晚、未育、肥胖',
 '乳腺B超+钼靶、穿刺活检（空心针/麦默通）、免疫组化（ER/PR/HER2）',
 '保乳/全切术、放疗、化疗、内分泌治疗（他莫昔芬/AI）、靶向（曲妥珠单抗）',
 '乳腺纤维瘤、乳腺增生、乳腺炎', 1);

-- ----- 感染科（DEPT_018）3条 -----

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000049', '病毒性肝炎（乙型）', 'B19', '感染性疾病', 'DEPT_018',
 '["乏力","食欲减退","恶心","右上腹不适","尿黄如茶","黄疸","肝区疼痛"]',
 '乙肝病毒（HBV）传播（血液/母婴/性接触）、免疫力低下',
 '乙肝五项（HBsAg+）、HBV-DNA定量、肝功能+腹部B超',
 '抗病毒（恩替卡韦/替诺福韦/TAF）、保肝降酶、干扰素、定期肝癌筛查',
 '其他病毒性肝炎、酒精性肝病、药物性肝损伤', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000050', '肺结核', 'A16.2', '感染性疾病', 'DEPT_018',
 '["慢性咳嗽（＞2周）","咳痰咯血","午后低热","盗汗","乏力消瘦","胸痛"]',
 '结核分枝杆菌感染（空气飞沫传播）、免疫力低下、营养不良',
 '胸部CT+痰涂片镜检/培养、T-SPOT/IGRA、Xpert MTB/RIF',
 '抗结核四联（HRZE）×2月→异烟肼+利福平×4月、DOTS督导',
 '肺炎、肺癌、支气管扩张症', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000051', '艾滋病', 'B24', '感染性疾病', 'DEPT_018',
 '["长期低热","乏力","反复感染（口腔念珠菌/PCP）","淋巴结肿大","腹泻","体重显著下降"]',
 'HIV传播（血液/性接触/母婴）、不安全性行为、共用针具',
 'HIV抗体筛查+确认试验（Western Blot）、CD4+T细胞计数+病毒载量',
 '抗病毒治疗（ART：替诺福韦/拉米夫定/多替拉韦）、机会感染预防',
 '其他免疫缺陷病、血液系统疾病、结核病', 1);
