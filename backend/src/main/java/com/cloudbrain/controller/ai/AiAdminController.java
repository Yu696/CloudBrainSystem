package com.cloudbrain.controller.ai;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.entity.DiseaseKnowledge;
import com.cloudbrain.entity.PromptTemplate;
import com.cloudbrain.service.ai.DiseaseKbService;
import com.cloudbrain.service.ai.PromptTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 管理端 Controller
 * 负责 Prompt 模板管理 + 疾病知识库管理（P2 优先级）
 *
 * @author 刘鹏杰
 */
@Tag(name = "AI 管理 - Prompt模板 & 疾病知识库")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAdminController extends BaseController {

    private final PromptTemplateService promptTemplateService;
    private final DiseaseKbService diseaseKbService;

    // ==================== Prompt 模板管理（5 个 API） ====================

    @Operation(summary = "按类型查询 Prompt 模板列表")
    @GetMapping("/prompt-templates")
    public Result<List<PromptTemplate>> listPromptTemplates(
            @Parameter(description = "模板类型：0=分诊 1=病历生成 2=处方审核") @RequestParam(required = false) Integer type) {
        if (type != null) {
            return success(promptTemplateService.listByType(type));
        }
        return success(promptTemplateService.listAll());
    }

    @Operation(summary = "新增 Prompt 模板")
    @PostMapping("/prompt-template")
    public Result<PromptTemplate> createPromptTemplate(@RequestBody PromptTemplate template) {
        return success(promptTemplateService.create(template));
    }

    @Operation(summary = "更新 Prompt 模板")
    @PutMapping("/prompt-template")
    public Result<PromptTemplate> updatePromptTemplate(@RequestBody PromptTemplate template) {
        return success(promptTemplateService.update(template));
    }

    @Operation(summary = "删除 Prompt 模板")
    @DeleteMapping("/prompt-template/{templateId}")
    public Result<String> deletePromptTemplate(@PathVariable String templateId) {
        promptTemplateService.delete(templateId);
        return success("删除成功");
    }

    @Operation(summary = "启用/禁用 Prompt 模板")
    @PutMapping("/prompt-template/{templateId}/status")
    public Result<String> updateTemplateStatus(
            @PathVariable String templateId,
            @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        promptTemplateService.updateStatus(templateId, status);
        return success(status == 1 ? "已启用" : "已禁用");
    }

    // ==================== 疾病知识库管理（4 个 API） ====================

    @Operation(summary = "搜索/列表疾病知识库")
    @GetMapping("/disease-kb")
    public Result<List<DiseaseKnowledge>> searchDiseaseKb(
            @Parameter(description = "搜索关键词（按疾病名称模糊匹配）") @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return success(diseaseKbService.search(keyword));
        }
        return success(diseaseKbService.listAll());
    }

    @Operation(summary = "新增疾病知识库条目")
    @PostMapping("/disease-kb")
    public Result<DiseaseKnowledge> createDiseaseKb(@RequestBody DiseaseKnowledge disease) {
        return success(diseaseKbService.create(disease));
    }

    @Operation(summary = "更新疾病知识库条目")
    @PutMapping("/disease-kb")
    public Result<DiseaseKnowledge> updateDiseaseKb(@RequestBody DiseaseKnowledge disease) {
        return success(diseaseKbService.update(disease));
    }

    @Operation(summary = "删除疾病知识库条目")
    @DeleteMapping("/disease-kb/{diseaseId}")
    public Result<String> deleteDiseaseKb(@PathVariable String diseaseId) {
        diseaseKbService.delete(diseaseId);
        return success("删除成功");
    }

    // ==================== AI 调用监控（预留，依赖余润东的 AiService） ====================

    // TODO: GET /api/ai/monitor/stats — AI 调用统计概览（待 AiService 完成后对接）
    // TODO: GET /api/ai/monitor/logs  — AI 调用明细列表（待 AiService 完成后对接）
}
