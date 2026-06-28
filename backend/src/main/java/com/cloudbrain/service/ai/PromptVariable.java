package com.cloudbrain.service.ai;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Prompt 变量注册表
 * <p>
 * 每个枚举值定义了一个模板变量的元数据：
 * 变量名(key)、中文标签(label)、适用的模板类型(applicableTypes)
 */
public enum PromptVariable {

    // ========== 通用变量（PatientContext 自动注入）==========
    AGE("age", "年龄", Set.of(0, 1)),
    GENDER("gender", "性别", Set.of(0, 1)),
    ALLERGY_HISTORY("allergy_history", "过敏史", Set.of(0, 1, 2)),
    MEDICAL_HISTORY("medical_history", "既往病史", Set.of(0, 1, 2)),
    CURRENT_MEDICATIONS("current_medications", "当前用药", Set.of(0, 1, 2)),

    // ========== 分诊(type=0) 专用 ==========
    CHIEF_COMPLAINT("chief_complaint", "主诉", Set.of(0, 1)),
    DEPT_HINT("dept_hint", "可选科室列表", Set.of(0)),

    // ========== 诊断(type=1) 专用 ==========
    PRESENT_ILLNESS("present_illness", "现病史", Set.of(1)),
    PHYSICAL_EXAM("physical_exam", "体格检查", Set.of(1)),
    AUXILIARY_EXAM("auxiliary_exam", "辅助检查", Set.of(1)),
    DISEASE_KNOWLEDGE_REF("disease_knowledge_ref", "疾病知识库参考", Set.of(1)),

    // ========== 处方审核(type=2) 专用 ==========
    PRESCRIPTION_ITEMS("prescription_items", "待审核处方药品", Set.of(2)),

    // ========== 病历生成(type=1) 专用 ==========
    DIALOGUE("dialogue", "医患对话内容", Set.of(1)),
    PATIENT_INFO("patient_info", "患者综合信息", Set.of(1, 2));

    private final String key;
    private final String label;
    private final Set<Integer> applicableTypes;

    PromptVariable(String key, String label, Set<Integer> applicableTypes) {
        this.key = key;
        this.label = label;
        this.applicableTypes = applicableTypes;
    }

    public String getKey() { return key; }
    public String getLabel() { return label; }
    public Set<Integer> getApplicableTypes() { return applicableTypes; }

    /**
     * 获取指定模板类型下所有可用变量
     */
    public static List<PromptVariable> forType(int templateType) {
        return Arrays.stream(values())
                .filter(v -> v.applicableTypes.contains(templateType))
                .collect(Collectors.toList());
    }
}