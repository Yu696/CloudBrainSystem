-- =============================================================
-- M04 AI 辅助诊断 — 种子数据
-- Prompt 模板 + 疾病知识库初始数据
-- =============================================================

USE cloudbrain;

-- ==================== Prompt 模板种子数据 ====================

INSERT INTO prompt_template (template_id, template_name, template_type, department_id, content, version, variables, status) VALUES
('PTMP_0000000000000001', '通用分诊模板', 0, NULL,
 '你是一位资深全科医生。请根据以下患者主诉进行智能分诊分析。\n\n患者主诉：{{chief_complaint}}\n患者年龄：{{age}}\n患者性别：{{gender}}\n\n请返回JSON格式：\n{\n  "recommendedDepartment": "科室名",\n  "diseaseMatches": [{"name": "", "icdCode": "", "confidence": 0.0}],\n  "analysisDetail": "分析详情"\n}',
 1,
 '["chief_complaint","age","gender"]', 1),
('PTMP_0000000000000002', '通用病历生成模板', 1, NULL,
 '你是一位医疗文书专家。请将以下医患对话转换为结构化病历。\n\n对话内容：\n{{dialogue}}\n\n患者基本信息：{{patient_info}}\n\n请生成包含以下内容的结构化病历：主诉、现病史、既往史、体格检查、初步诊断、治疗计划。以JSON格式返回。',
 1,
 '["dialogue","patient_info"]', 1),
('PTMP_0000000000000003', '处方审核模板', 2, NULL,
 '你是一位临床药学专家。请审核以下处方。\n\n患者信息：{{patient_info}}\n处方药品：{{prescription_items}}\n\n请逐项检查：药物过敏、禁忌人群、药物相互作用、剂量合理性。以JSON格式返回审核结果。',
 1,
 '["patient_info","prescription_items"]', 1);

-- ==================== 疾病知识库种子数据 ====================

INSERT INTO disease_knowledge (disease_id, disease_name, icd_code, category, related_department_id, symptoms, risk_factors, diagnosis_basis, treatment_plan, status) VALUES
('DK_000000000000001', '上呼吸道感染', 'J06.9', '呼吸系统', NULL,
 '["发热","咳嗽","咽痛","鼻塞","流涕","头痛"]',
 '免疫力低下、季节变化、接触感染者',
 '临床表现 + 血常规',
 '对症支持治疗、多饮水、休息', 1),
('DK_000000000000002', '原发性高血压', 'I10', '心血管系统', NULL,
 '["头痛","头晕","心悸","耳鸣","视力模糊"]',
 '高盐饮食、肥胖、家族史、精神紧张',
 '非同日三次血压测量 ≥140/90mmHg',
 '低盐饮食、运动、降压药物（ACEI/ARB/CCB）', 1),
('DK_000000000000003', '2型糖尿病', 'E11.9', '内分泌系统', NULL,
 '["多饮","多尿","多食","体重下降","乏力","视力模糊"]',
 '肥胖、家族史、缺乏运动、不健康饮食',
 '空腹血糖≥7.0mmol/L 或 OGTT 2h≥11.1mmol/L 或 HbA1c≥6.5%',
 '饮食控制、运动、口服降糖药/胰岛素', 1);
