package com.cloudbrain.service.ai;

import com.cloudbrain.entity.PromptTemplate;

import java.util.List;
import java.util.Map;

/**
 * Prompt 模板服务
 */
public interface PromptTemplateService {

    /** 按类型查询模板列表 */
    List<PromptTemplate> listByType(Integer templateType);

    /** 查询所有启用的模板 */
    List<PromptTemplate> listAll();

    /** 根据模板ID查询 */
    PromptTemplate getByTemplateId(String templateId);

    /** 新增模板 */
    PromptTemplate create(PromptTemplate template);

    /** 更新模板 */
    PromptTemplate update(PromptTemplate template);

    /** 删除模板 */
    void delete(String templateId);

    /** 启用/禁用模板 */
    void updateStatus(String templateId, Integer status);

    /**
     * 渲染模板 — 将 {{变量名}} 替换为实际值
     * @param templateContent 模板原文
     * @param variables 变量名 → 变量值的映射
     * @return 替换后的 Prompt 文本
     */
    String render(String templateContent, Map<String, String> variables);
}
