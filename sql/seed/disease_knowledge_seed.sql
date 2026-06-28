-- =============================================================
-- 疾病知识库种子数据（覆盖全部 12 个科室）
-- 包含与现有 3 条记录不重复的 30 条常见疾病
-- disease_id 从 DK_000000000000004 开始
-- =============================================================

USE cloudbrain;

-- ==================== 内科（DEPT_001）3条 ====================

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000004', '慢性胃炎', 'K29.7', '消化系统', 'DEPT_001',
 '["上腹痛","腹胀","嗳气","反酸","食欲不振","恶心"]',
 '幽门螺杆菌感染、长期饮食不规律、烟酒、NSAIDs药物',
 '胃镜+病理活检、幽门螺杆菌检测',
 '根除Hp、抑酸护胃、调整饮食作息、定期复查胃镜',
 '消化性溃疡、胃癌、胃食管反流病', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000005', '急性胃肠炎', 'K52.9', '消化系统', 'DEPT_001',
 '["腹痛","腹泻","恶心","呕吐","发热","脱水"]',
 '不洁饮食、细菌/病毒感染、食物过敏',
 '临床表现+粪便常规',
 '补液对症、抗感染、调整饮食、肠道菌群调节',
 '阑尾炎、肠梗阻、炎症性肠病', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000006', '缺铁性贫血', 'D50.9', '血液系统', 'DEPT_001',
 '["乏力","头晕","面色苍白","心悸","气短","食欲减退"]',
 '慢性失血、铁摄入不足、吸收障碍、妊娠',
 '血常规（小细胞低色素）+ 铁代谢指标',
 '补铁治疗、去除病因、富含铁饮食',
 '地中海贫血、慢性病贫血、巨幼细胞贫血', 1);

-- ==================== 外科（DEPT_002）2条 ====================

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000007', '急性阑尾炎', 'K35.9', '普外科', 'DEPT_002',
 '["转移性右下腹痛","恶心呕吐","发热","麦氏点压痛","反跳痛"]',
 '阑尾管腔阻塞、细菌感染',
 '临床表现+腹部B超/CT、血常规白细胞升高',
 '手术切除（阑尾切除术）、抗生素治疗',
 '胃肠炎、肠系膜淋巴结炎、右侧输尿管结石', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000008', '肠梗阻', 'K56.7', '普外科', 'DEPT_002',
 '["腹痛","呕吐","腹胀","停止排便排气","腹部绞痛"]',
 '腹部手术史、肿瘤、疝气、肠套叠',
 '立位腹部平片（气液平面）、CT检查',
 '禁食胃肠减压、补液纠正水电解质紊乱、手术解除梗阻',
 '急性胰腺炎、胃肠穿孔、阑尾炎', 1);

-- ==================== 妇产科（DEPT_003）3条 ====================

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000009', '子宫肌瘤', 'D25.9', '妇产科', 'DEPT_003',
 '["月经量增多","经期延长","下腹包块","腰腹坠胀","贫血","尿频"]',
 '雌激素水平高、年龄、未产、肥胖、家族史',
 '妇科B超、MRI检查',
 '药物治疗（GnRH-a）、肌瘤剔除术、子宫切除术、介入治疗',
 '卵巢肿瘤、子宫腺肌症、子宫内膜息肉', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000010', '卵巢囊肿', 'N83.2', '妇产科', 'DEPT_003',
 '["下腹不适","月经紊乱","腹部包块","腹胀","压迫症状"]',
 '内分泌失调、遗传因素、盆腔感染',
 '妇科B超、肿瘤标志物CA125',
 '观察随访、口服避孕药、腹腔镜囊肿剥除术',
 '卵巢肿瘤、输卵管积水、子宫内膜异位囊肿', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000011', '妊娠期高血压', 'O13.0', '妇产科', 'DEPT_003',
 '["妊娠20周后血压升高","水肿","蛋白尿","头痛","视力模糊"]',
 '初产妇、高龄妊娠、多胎妊娠、慢性高血压史',
 '血压监测+尿蛋白定量+肝肾功能',
 '休息降压、硫酸镁防治子痫、适时终止妊娠',
 '慢性高血压合并妊娠、妊娠期蛋白尿', 1);

-- ==================== 儿科（DEPT_004）3条 ====================

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000012', '小儿肺炎', 'J18.9', '儿科', 'DEPT_004',
 '["发热","咳嗽","气促","呼吸困难","肺部啰音","精神萎靡"]',
 '感染（病毒/细菌）、免疫力低下、营养不良',
 '胸片+血常规+CRP、病原学检查',
 '抗感染治疗、止咳化痰、氧疗、支持治疗',
 '急性支气管炎、支气管哮喘、肺结核', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000013', '手足口病', 'B08.4', '儿科', 'DEPT_004',
 '["发热","手足皮疹","口腔疱疹","咽痛","食欲减退"]',
 'EV71/CoxA16病毒感染、接触传播、5岁以下高发',
 '临床表现+病原学检测（咽拭子PCR）',
 '对症支持、退热、口腔护理、隔离观察',
 '水痘、疱疹性咽峡炎、麻疹', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000014', '小儿腹泻', 'A09.0', '儿科', 'DEPT_004',
 '["腹泻（水样便）","呕吐","发热","腹痛","脱水","哭时无泪"]',
 '轮状病毒感染、细菌感染、喂养不当、抗生素相关',
 '粪便常规+轮状病毒检测、脱水程度评估',
 '口服补液盐、蒙脱石散、补锌、继续喂养',
 '细菌性痢疾、食物中毒、肠套叠', 1);

-- ==================== 眼科（DEPT_005）2条 ====================

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000015', '白内障', 'H26.9', '眼科', 'DEPT_005',
 '["视力下降","视物模糊","怕光","视物重影","色觉改变"]',
 '年龄（老年性）、外伤、糖尿病、长期紫外线照射',
 '裂隙灯检查、视力检查、眼底检查',
 '白内障超声乳化+人工晶体植入术',
 '角膜白斑、玻璃体混浊、视网膜病变', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000016', '屈光不正', 'H52.1', '眼科', 'DEPT_005',
 '["远视力下降","近视物模糊","眼疲劳","头痛","眯眼视物"]',
 '遗传因素、长时间近距离用眼、不良用眼习惯',
 '视力检查、验光检查（散瞳验光）',
 '配戴眼镜、角膜接触镜、激光手术（LASIK/PRK）',
 '弱视、白内障、角膜疾病', 1);

-- ==================== 耳鼻喉科（DEPT_006）2条 ====================

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000017', '急性扁桃体炎', 'J03.9', '耳鼻喉科', 'DEPT_006',
 '["咽痛剧烈","发热","吞咽困难","扁桃体肿大充血","颌下淋巴结肿痛"]',
 '细菌（链球菌）感染、病毒感染、受凉疲劳',
 '咽部检查+血常规、咽拭子培养',
 '抗生素治疗（青霉素类）、退热止痛、漱口水',
 '咽炎、会厌炎、传染性单核细胞增多症', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000018', '过敏性鼻炎', 'J30.4', '耳鼻喉科', 'DEPT_006',
 '["阵发性喷嚏","流清涕","鼻塞","鼻痒","眼痒","嗅觉减退"]',
 '花粉/尘螨/动物皮屑过敏、遗传、空气污染',
 '过敏原检测（皮肤点刺/血清IgE）、鼻内镜检查',
 '抗组胺药、鼻用糖皮质激素、脱敏治疗、避免过敏原',
 '感冒（急性鼻炎）、鼻窦炎、鼻息肉', 1);

-- ==================== 口腔科（DEPT_007）1条 ====================

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000019', '龋齿', 'K02.9', '口腔科', 'DEPT_007',
 '["牙痛（冷热刺激痛）","牙齿变色","牙体缺损","食物嵌塞","口臭"]',
 '口腔卫生不良、高糖饮食、氟摄入不足、唾液分泌减少',
 '口腔检查、X线牙片',
 '充填治疗（补牙）、根管治疗、嵌体/冠修复',
 '牙髓炎、牙本质过敏、楔状缺损', 1);

-- ==================== 皮肤科（DEPT_008）3条 ====================

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000020', '湿疹', 'L30.9', '皮肤科', 'DEPT_008',
 '["皮肤红斑","丘疹","水疱","瘙痒剧烈","渗液","皮肤增厚"]',
 '过敏体质、环境刺激、精神压力、皮肤屏障受损',
 '临床表现为主、过敏原检测、皮肤活检',
 '外用糖皮质激素、保湿修复、抗组胺药、避免刺激',
 '接触性皮炎、银屑病、真菌感染', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000021', '荨麻疹', 'L50.9', '皮肤科', 'DEPT_008',
 '["风团（红色或肤色）","剧烈瘙痒","血管性水肿","自限性发作"]',
 '食物/药物过敏、感染、物理刺激（冷/热/压力）',
 '临床表现+过敏原检测、激发试验',
 '抗组胺药、糖皮质激素（重症）、避免诱因',
 '湿疹、多形红斑、药疹', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000022', '带状疱疹', 'B02.9', '皮肤科', 'DEPT_008',
 '["带状分布水疱","神经痛（烧灼/电击样）","发热","乏力","皮肤敏感"]',
 '水痘-带状疱疹病毒再激活、免疫力下降、老年',
 '临床表现+PCR病毒检测、血清抗体检测',
 '抗病毒（阿昔洛韦）、止痛、神经营养、预防后遗神经痛',
 '单纯疱疹、接触性皮炎、丹毒', 1);

-- ==================== 神经内科（DEPT_009）3条 ====================

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000023', '偏头痛', 'G43.9', '神经系统', 'DEPT_009',
 '["单侧搏动性头痛","恶心呕吐","畏光畏声","先兆症状（闪光/暗点）"]',
 '遗传因素、内分泌变化、睡眠不足、饮食诱发（红酒/奶酪）',
 '临床表现（国际头痛分类标准）、排除其他头痛原因',
 '急性期（曲普坦/NSAIDs）、预防（β受体阻滞剂/钙通道阻滞剂）',
 '紧张型头痛、丛集性头痛、颅内占位', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000024', '脑梗死', 'I63.9', '神经系统', 'DEPT_009',
 '["偏瘫","偏身感觉障碍","口角歪斜","言语不清","意识障碍","头晕共济失调"]',
 '高血压、糖尿病、高血脂、房颤、吸烟、年龄',
 '头颅CT/MRI、脑血管造影、病因筛查',
 '溶栓（4.5h内）、抗血小板/抗凝、他汀、康复训练',
 '脑出血、颅内肿瘤、代谢性脑病', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000025', '癫痫', 'G40.9', '神经系统', 'DEPT_009',
 '["意识丧失","全身抽搐","口吐白沫","牙关紧闭","失神发作","肢体异常运动"]',
 '脑外伤、颅内感染、脑血管病、遗传、脑肿瘤',
 '脑电图（含长程监测）、头颅MRI、排除继发性病因',
 '抗癫痫药物（丙戊酸/卡马西平/左乙拉西坦）、手术切除病灶',
 '晕厥、假性发作、短暂性脑缺血发作', 1);

-- ==================== 心血管内科（DEPT_010）3条 ====================

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000026', '冠状动脉粥样硬化性心脏病', 'I25.1', '心血管系统', 'DEPT_010',
 '["胸痛胸闷（劳累诱发）","心绞痛放射至左肩","气短","心悸","乏力"]',
 '高血压、高血脂、糖尿病、吸烟、肥胖、缺乏运动',
 '心电图+动态心电图、冠脉CTA/造影、心肌酶谱',
 '抗血小板+他汀、β受体阻滞剂、PCI介入/搭桥手术',
 '心肌炎、心包炎、肺栓塞', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000027', '心律失常', 'I49.9', '心血管系统', 'DEPT_010',
 '["心悸","心跳停顿感","头晕","乏力","黑矇","晕厥"]',
 '冠心病、高血压、心肌病、电解质紊乱、甲亢、饮酒',
 '心电图+动态心电图、心脏电生理检查',
 '抗心律失常药物、射频消融、起搏器植入',
 '焦虑症、心脏神经官能症、心肌缺血', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000028', '心力衰竭', 'I50.9', '心血管系统', 'DEPT_010',
 '["呼吸困难（活动后/夜间阵发性）","下肢水肿","乏力","端坐呼吸","咳嗽"]',
 '冠心病、高血压、心肌病、心脏瓣膜病、心律失常',
 '心脏超声（LVEF）、BNP/NT-proBNP、胸片',
 '利尿剂、ACEI/ARB、β受体阻滞剂、螺内酯、限盐限水',
 'COPD（慢性阻塞性肺病）、肝硬化腹水、肾病综合征', 1);

-- ==================== 骨科（DEPT_011）3条 ====================

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000029', '腰椎间盘突出症', 'M51.2', '骨关节系统', 'DEPT_011',
 '["腰痛放射至下肢","坐骨神经痛","下肢麻木无力","直腿抬高受限"]',
 '久坐、重体力劳动、肥胖、年龄退变、外伤',
 '腰椎MRI/CT、神经系统查体',
 '保守（卧床/理疗/牵引）、药物（NSAIDs/肌松剂）、微创/开放手术',
 '腰肌劳损、腰椎管狭窄、骶髂关节炎', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000030', '颈椎病', 'M47.9', '骨关节系统', 'DEPT_011',
 '["颈肩痛","上肢麻本","头痛头晕","手指灵活度下降","行走不稳"]',
 '长期低头工作、不良姿势、年龄退变、外伤',
 '颈椎MRI/CT+X线、神经电生理检查',
 '保守（牵引/理疗）、药物（NSAIDs/神经营养）、手术',
 '肩周炎、腕管综合征、肌紧张性头痛', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000031', '骨关节炎', 'M19.9', '骨关节系统', 'DEPT_011',
 '["关节疼痛（活动加重）","晨僵（＜30分钟）","关节肿胀","活动受限","骨摩擦音"]',
 '年龄、肥胖、关节创伤、遗传、过度使用',
 'X线（关节间隙狭窄/骨赘）、MRI',
 '减重运动、物理治疗、止痛（NSAIDs/关节腔注射）、关节置换',
 '类风湿性关节炎、痛风、强直性脊柱炎', 1);

-- ==================== 泌尿外科（DEPT_012）2条 ====================

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000032', '肾结石', 'N20.0', '泌尿系统', 'DEPT_012',
 '["腰部剧痛（绞痛）","血尿","尿频尿急","恶心呕吐","患侧叩痛"]',
 '饮水不足、高钙尿症、高尿酸血症、尿路感染',
 '泌尿系B超/CT、尿常规+尿沉渣、结石成分分析',
 '保守（多饮水/排石药）、体外冲击波碎石（ESWL）、手术取石',
 '急性阑尾炎、胆囊结石、肾盂肾炎', 1);

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, similarity_diseases, status) VALUES
('DK_000000000000033', '前列腺增生', 'N40.0', '泌尿系统', 'DEPT_012',
 '["尿频（夜尿增多）","排尿困难","尿流变细","尿不尽","急性尿潴留"]',
 '年龄、雄激素水平、肥胖、缺乏运动',
 '直肠指检+PSA、泌尿系B超（残余尿）、尿流率',
 'α受体阻滞剂（坦索罗辛）、5α还原酶抑制剂、经尿道前列腺电切术',
 '前列腺癌、膀胱颈挛缩、尿道狭窄', 1);
