package com.cloudbrain.controller.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.entity.*;
import com.cloudbrain.mapper.AiCallLogMapper;
import com.cloudbrain.mapper.DoctorMapper;
import com.cloudbrain.mapper.PatientMapper;
import com.cloudbrain.mapper.UserMapper;
import com.cloudbrain.service.ai.DiseaseKbService;
import com.cloudbrain.service.ai.PromptTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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
@PreAuthorize("hasRole('ADMIN')")
public class AiAdminController extends BaseController {

    private final PromptTemplateService promptTemplateService;
    private final DiseaseKbService diseaseKbService;
    private final AiCallLogMapper aiCallLogMapper;
    private final DoctorMapper doctorMapper;
    private final PatientMapper patientMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

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

        long totalCalls = countByType(null, start, end);
        long successCalls = countSuccess(null, start, end);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalCalls", totalCalls);
        stats.put("successCalls", successCalls);
        stats.put("failCalls", totalCalls - successCalls);
        stats.put("successRate", totalCalls > 0
                ? Math.round(successCalls * 10000.0 / totalCalls) / 100.0 : 0);

        Map<String, Object> byType = new LinkedHashMap<>();
        byType.put("triage", buildTypeStats(0, start, end));
        byType.put("diagnosis", buildTypeStats(1, start, end));
        byType.put("prescriptionCheck", buildTypeStats(2, start, end));
        byType.put("imageDiagnosis", buildTypeStats(3, start, end));
        stats.put("byType", byType);

        stats.put("dailyTrend", List.of());

        return success(stats);
    }

    @Operation(summary = "AI 调用明细列表")
    @GetMapping("/monitor/logs")
    public Result<Map<String, Object>> getMonitorLogs(
            @Parameter(description = "日志类型：0=分诊 1=诊断 2=处方审核 3=影像诊断") @RequestParam(required = false) Integer type,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "开始日期（yyyy-MM-dd）") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期（yyyy-MM-dd）") @RequestParam(required = false) String endDate) {

        LocalDateTime start = startDate != null ? LocalDate.parse(startDate).atStartOfDay() : null;
        LocalDateTime end = endDate != null ? LocalDate.parse(endDate).atTime(LocalTime.MAX) : null;

        Page<AiCallLog> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<AiCallLog> w = new LambdaQueryWrapper<>();
        if (type != null) w.eq(AiCallLog::getCallType, type);
        if (start != null) w.ge(AiCallLog::getCreateTime, start);
        if (end != null) w.le(AiCallLog::getCreateTime, end);
        w.orderByDesc(AiCallLog::getCreateTime);

        Page<AiCallLog> pageResult = aiCallLogMapper.selectPage(mpPage, w);

        // 批量查询医生姓名（通过 doctor 表关联 user 表）
        Set<String> doctorIds = pageResult.getRecords().stream()
                .map(AiCallLog::getDoctorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, String> doctorNameMap = new HashMap<>();
        if (!doctorIds.isEmpty()) {
            // doctor_id → user_id
            Map<String, String> doctorUserMap = new HashMap<>();
            doctorMapper.selectList(
                    new LambdaQueryWrapper<Doctor>().in(Doctor::getDoctorId, doctorIds)
            ).forEach(d -> doctorUserMap.put(d.getDoctorId(), d.getUserId()));
            // user_id → real_name
            Set<String> userIds = new HashSet<>(doctorUserMap.values());
            if (!userIds.isEmpty()) {
                userMapper.selectList(
                        new LambdaQueryWrapper<User>().in(User::getUserId, userIds)
                ).forEach(u -> {
                    // 反向映射：doctorId → userName
                    doctorUserMap.forEach((docId, uid) -> {
                        if (uid.equals(u.getUserId())) {
                            doctorNameMap.put(docId, u.getRealName() != null ? u.getRealName() : u.getUsername());
                        }
                    });
                });
            }
        }

        // 兜底：doctor_id 可能直接存的是 userId（如影像诊断），直接查 User 表
        Set<String> unmatchedDoctorIds = doctorIds.stream()
                .filter(id -> !doctorNameMap.containsKey(id))
                .collect(Collectors.toSet());
        if (!unmatchedDoctorIds.isEmpty()) {
            userMapper.selectList(
                    new LambdaQueryWrapper<User>().in(User::getUserId, unmatchedDoctorIds)
            ).forEach(u -> doctorNameMap.put(u.getUserId(),
                    u.getRealName() != null ? u.getRealName() : u.getUsername()));
        }

        // 批量查询患者姓名
        Set<String> patientIds = pageResult.getRecords().stream()
                .map(AiCallLog::getPatientId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, String> patientNameMap = new HashMap<>();
        if (!patientIds.isEmpty()) {
            patientMapper.selectList(
                    new LambdaQueryWrapper<Patient>().in(Patient::getPatientId, patientIds)
            ).forEach(p -> patientNameMap.put(p.getPatientId(),
                    p.getName() != null ? p.getName() : p.getPatientId()));
        }

        // 转换记录，添加调用人姓名和患者姓名
        List<Map<String, Object>> recordList = pageResult.getRecords().stream().map(log -> {
            Map<String, Object> record = objectMapper.convertValue(log, Map.class);
            record.put("callerName", log.getDoctorId() != null
                    ? doctorNameMap.getOrDefault(log.getDoctorId(), log.getDoctorId())
                    : null);
            record.put("patientName", log.getPatientId() != null
                    ? patientNameMap.getOrDefault(log.getPatientId(), log.getPatientId())
                    : null);
            return record;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("total", pageResult.getTotal());
        result.put("records", recordList);

        return success(result);
    }

    // ==================== 监控辅助方法 ====================

    private long countByType(Integer callType, LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<AiCallLog> w = new LambdaQueryWrapper<>();
        if (callType != null) w.eq(AiCallLog::getCallType, callType);
        if (start != null) w.ge(AiCallLog::getCreateTime, start);
        if (end != null) w.le(AiCallLog::getCreateTime, end);
        return aiCallLogMapper.selectCount(w);
    }

    private long countSuccess(Integer callType, LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<AiCallLog> w = new LambdaQueryWrapper<>();
        w.eq(AiCallLog::getStatus, 1);
        if (callType != null) w.eq(AiCallLog::getCallType, callType);
        if (start != null) w.ge(AiCallLog::getCreateTime, start);
        if (end != null) w.le(AiCallLog::getCreateTime, end);
        return aiCallLogMapper.selectCount(w);
    }

    private Map<String, Object> buildTypeStats(Integer callType, LocalDateTime start, LocalDateTime end) {
        long total = countByType(callType, start, end);

        // 计算该类型的平均响应时间
        LambdaQueryWrapper<AiCallLog> w = new LambdaQueryWrapper<>();
        w.eq(AiCallLog::getCallType, callType)
         .isNotNull(AiCallLog::getResponseTimeMs);
        if (start != null) w.ge(AiCallLog::getCreateTime, start);
        if (end != null) w.le(AiCallLog::getCreateTime, end);

        long avgMs = 0;
        List<AiCallLog> logs = aiCallLogMapper.selectList(w);
        if (!logs.isEmpty()) {
            avgMs = logs.stream()
                    .mapToLong(l -> l.getResponseTimeMs() != null ? l.getResponseTimeMs() : 0)
                    .sum() / logs.size();
        }

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("calls", total);
        m.put("avgMs", avgMs);
        return m;
    }
}
