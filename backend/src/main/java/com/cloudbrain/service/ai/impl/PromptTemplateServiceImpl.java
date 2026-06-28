package com.cloudbrain.service.ai.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.entity.PromptTemplate;
import com.cloudbrain.mapper.PromptTemplateMapper;
import com.cloudbrain.service.ai.PromptTemplateService;
import com.cloudbrain.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Prompt 模板服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptTemplateServiceImpl
        extends ServiceImpl<PromptTemplateMapper, PromptTemplate>
        implements PromptTemplateService {

    @Override
    public List<PromptTemplate> listByType(Integer templateType) {
        LambdaQueryWrapper<PromptTemplate> wrapper = new LambdaQueryWrapper<PromptTemplate>()
                .eq(templateType != null, PromptTemplate::getTemplateType, templateType)
                .orderByDesc(PromptTemplate::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public List<PromptTemplate> listAll() {
        return this.list(new LambdaQueryWrapper<PromptTemplate>()
                .orderByDesc(PromptTemplate::getCreateTime));
    }

    @Override
    public PromptTemplate getByTemplateId(String templateId) {
        PromptTemplate template = this.getOne(new LambdaQueryWrapper<PromptTemplate>()
                .eq(PromptTemplate::getTemplateId, templateId));
        if (template == null) {
            throw new BusinessException("Prompt 模板不存在");
        }
        return template;
    }

    @Override
    @Transactional
    public PromptTemplate create(PromptTemplate template) {
        if (template.getTemplateName() == null || template.getTemplateName().isBlank()) {
            throw new BusinessException("模板名称不能为空");
        }
        if (template.getTemplateType() == null) {
            throw new BusinessException("模板类型不能为空");
        }
        if (template.getContent() == null || template.getContent().isBlank()) {
            throw new BusinessException("模板内容不能为空");
        }

        template.setTemplateId(UUIDUtil.generateTemplateId());
        template.setVersion(1);
        template.setStatus(template.getStatus() != null ? template.getStatus() : 1);

        save(template);
        return template;
    }

    @Override
    @Transactional
    public PromptTemplate update(PromptTemplate template) {
        if (template.getTemplateId() == null) {
            throw new BusinessException("模板ID不能为空");
        }

        PromptTemplate existing = getByTemplateId(template.getTemplateId());

        if (template.getTemplateName() != null) existing.setTemplateName(template.getTemplateName());
        if (template.getContent() != null) existing.setContent(template.getContent());
        if (template.getVariables() != null) existing.setVariables(template.getVariables());
        if (template.getTemplateType() != null) existing.setTemplateType(template.getTemplateType());
        if (template.getDepartmentId() != null) existing.setDepartmentId(template.getDepartmentId());
        if (template.getStatus() != null) existing.setStatus(template.getStatus());

        // 内容变更时版本号+1
        if (template.getContent() != null) {
            existing.setVersion(existing.getVersion() + 1);
        }

        updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void delete(String templateId) {
        PromptTemplate existing = getByTemplateId(templateId);
        removeById(existing.getId());
    }

    @Override
    @Transactional
    public void updateStatus(String templateId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态值只能为0(禁用)或1(启用)");
        }
        PromptTemplate existing = getByTemplateId(templateId);
        existing.setStatus(status);
        updateById(existing);
    }

    @Override
    public String render(String templateContent, Map<String, String> variables) {
        String result = templateContent;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            // 对变量值进行安全转义（防止注入）
            String safeValue = entry.getValue()
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"");
            result = result.replace("{{" + entry.getKey() + "}}", safeValue);
        }
        // 检查是否有未替换的占位符
        if (result.contains("{{")) {
            List<String> unreplaced = new ArrayList<>();
            Matcher m = Pattern.compile("\\{\\{(.+?)}}").matcher(result);
            while (m.find()) {
                unreplaced.add(m.group(1));
            }
            log.warn("Prompt 模板存在未替换的变量占位符: {}", unreplaced);
        }
        return result;
    }
}
