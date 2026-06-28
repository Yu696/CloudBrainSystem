package com.cloudbrain.controller.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.entity.*;
import com.cloudbrain.mapper.DiagnosisResultMapper;
import com.cloudbrain.mapper.GenerationLogMapper;
import com.cloudbrain.mapper.TriageLogMapper;
import com.cloudbrain.service.ai.DiseaseKbService;
import com.cloudbrain.service.ai.PromptTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 管理端 Controller
 * 负责 Prompt 模板管理 + 疾病知识库管理（P2 优先级）+ AI 调用监控
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
    private final TriageLogMapper triageLogMapper;
    private final DiagnosisResultMapper diagnosisResultMapper;
    private final GenerationLogMapper generationLogMapper;

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

    // ==================== AI 调用监控（2 个 API） ====================

    @Operation(summary = "AI 调用统计概览")
    @GetMapping("/monitor/stats")
    public Result<Map<String, Object>> getMonitorStats(
            @Parameter(description = "开始日期（yyyy-MM-dd）") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期（yyyy-MM-dd）") @RequestParam(required = false) String endDate) {

        LocalDateTime start = startDate != null ? LocalDate.parse(startDate).atStartOfDay() : null;
        LocalDateTime end = endDate != null ? LocalDate.parse(endDate).atTime(LocalTime.MAX) : null;

        long triageTotal = countTriage(start, end);
        long diagnosisTotal = countDiagnosis(start, end);
        long generationTotal = countGeneration(start, end);

        long triageSuccess = countTriageSuccess(start, end);
        long diagnosisSuccess = countDiagnosisSuccess(start, end);
        long generationSuccess = countGenerationSuccess(start, end);

        long totalCalls = triageTotal + diagnosisTotal + generationTotal;
        long successCalls = triageSuccess + diagnosisSuccess + generationSuccess;

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalCalls", totalCalls);
        stats.put("successCalls", successCalls);
        stats.put("failCalls", totalCalls - successCalls);
        stats.put("successRate", totalCalls > 0
                ? Math.round(successCalls * 10000.0 / totalCalls) / 100.0 : 0);

        Map<String, Object> byType = new LinkedHashMap<>();
        byType.put("triage", Map.of("total", triageTotal, "success", triageSuccess));
        byType.put("diagnosis", Map.of("total", diagnosisTotal, "success", diagnosisSuccess));
        byType.put("generation", Map.of("total", generationTotal, "success", generationSuccess));
        stats.put("byType", byType);

        return success(stats);
    }

    @Operation(summary = "AI 调用明细列表")
    @GetMapping("/monitor/logs")
    public Result<Map<String, Object>> getMonitorLogs(
            @Parameter(description = "日志类型：0=分诊 1=诊断 2=处方审核 3=病历生成") @RequestParam(required = false) Integer type,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "开始日期（yyyy-MM-dd）") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期（yyyy-MM-dd）") @RequestParam(required = false) String endDate) {

        LocalDateTime start = startDate != null ? LocalDate.parse(startDate).atStartOfDay() : null;
        LocalDateTime end = endDate != null ? LocalDate.parse(endDate).atTime(LocalTime.MAX) : null;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("page", page);
        result.put("pageSize", pageSize);

        if (type == null) {
            result.put("total", 0);
            result.put("records", List.of());
            return success(result);
        }

        switch (type) {
            case 0 -> {
                Page<TriageLog> mpPage = new Page<>(page, pageSize);
                LambdaQueryWrapper<TriageLog> w = new LambdaQueryWrapper<>();
                if (start != null) w.ge(TriageLog::getCreateTime, start);
                if (end != null) w.le(TriageLog::getCreateTime, end);
                w.orderByDesc(TriageLog::getCreateTime);
                Page<TriageLog> pageResult = triageLogMapper.selectPage(mpPage, w);
                result.put("total", pageResult.getTotal());
                result.put("records", pageResult.getRecords());
            }
            case 1 -> {
                Page<DiagnosisResult> mpPage = new Page<>(page, pageSize);
                LambdaQueryWrapper<DiagnosisResult> w = new LambdaQueryWrapper<>();
                if (start != null) w.ge(DiagnosisResult::getCreateTime, start);
                if (end != null) w.le(DiagnosisResult::getCreateTime, end);
                w.orderByDesc(DiagnosisResult::getCreateTime);
                Page<DiagnosisResult> pageResult = diagnosisResultMapper.selectPage(mpPage, w);
                result.put("total", pageResult.getTotal());
                result.put("records", pageResult.getRecords());
            }
            case 2, 3 -> {
                Page<GenerationLog> mpPage = new Page<>(page, pageSize);
                LambdaQueryWrapper<GenerationLog> w = new LambdaQueryWrapper<>();
                if (start != null) w.ge(GenerationLog::getCreateTime, start);
                if (end != null) w.le(GenerationLog::getCreateTime, end);
                w.orderByDesc(GenerationLog::getCreateTime);
                Page<GenerationLog> pageResult = generationLogMapper.selectPage(mpPage, w);
                result.put("total", pageResult.getTotal());
                result.put("records", pageResult.getRecords());
            }
            default -> {
                result.put("total", 0);
                result.put("records", List.of());
            }
        }

        return success(result);
    }

    // ==================== 监控辅助方法 ====================

    private long countTriage(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<TriageLog> w = new LambdaQueryWrapper<>();
        if (start != null) w.ge(TriageLog::getCreateTime, start);
        if (end != null) w.le(TriageLog::getCreateTime, end);
        return triageLogMapper.selectCount(w);
    }

    private long countDiagnosis(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<DiagnosisResult> w = new LambdaQueryWrapper<>();
        if (start != null) w.ge(DiagnosisResult::getCreateTime, start);
        if (end != null) w.le(DiagnosisResult::getCreateTime, end);
        return diagnosisResultMapper.selectCount(w);
    }

    private long countGeneration(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<GenerationLog> w = new LambdaQueryWrapper<>();
        if (start != null) w.ge(GenerationLog::getCreateTime, start);
        if (end != null) w.le(GenerationLog::getCreateTime, end);
        return generationLogMapper.selectCount(w);
    }

    private long countTriageSuccess(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<TriageLog> w = new LambdaQueryWrapper<>();
        w.eq(TriageLog::getStatus, 1);
        if (start != null) w.ge(TriageLog::getCreateTime, start);
        if (end != null) w.le(TriageLog::getCreateTime, end);
        return triageLogMapper.selectCount(w);
    }

    private long countDiagnosisSuccess(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<DiagnosisResult> w = new LambdaQueryWrapper<>();
        w.eq(DiagnosisResult::getStatus, 1);
        if (start != null) w.ge(DiagnosisResult::getCreateTime, start);
        if (end != null) w.le(DiagnosisResult::getCreateTime, end);
        return diagnosisResultMapper.selectCount(w);
    }

    private long countGenerationSuccess(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<GenerationLog> w = new LambdaQueryWrapper<>();
        w.eq(GenerationLog::getStatus, 1);
        if (start != null) w.ge(GenerationLog::getCreateTime, start);
        if (end != null) w.le(GenerationLog::getCreateTime, end);
        return generationLogMapper.selectCount(w);
    }
}
