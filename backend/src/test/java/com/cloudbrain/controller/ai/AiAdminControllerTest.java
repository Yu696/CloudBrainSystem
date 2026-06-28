package com.cloudbrain.controller.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.entity.*;
import com.cloudbrain.mapper.DiagnosisResultMapper;
import com.cloudbrain.mapper.GenerationLogMapper;
import com.cloudbrain.mapper.TriageLogMapper;
import com.cloudbrain.service.ai.AiService;
import com.cloudbrain.service.ai.DiseaseKbService;
import com.cloudbrain.service.ai.PromptTemplateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AI 管理端接口单元测试
 * 覆盖 AiAdminController 的全部端点：
 * Prompte 模板管理（5 个 API）+ 疾病知识库管理（4 个 API）+ AI 调用监控（2 个 API）
 */
@SpringBootTest(classes = AiAdminControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AiAdminController 单元测试")
class AiAdminControllerTest {

    @Configuration
    @EnableAutoConfiguration(exclude = {
            SecurityAutoConfiguration.class,
            SecurityFilterAutoConfiguration.class,
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class
    })
    @ComponentScan(
            basePackages = {"com.cloudbrain.controller.ai", "com.cloudbrain.common"},
            useDefaultFilters = false,
            includeFilters = @ComponentScan.Filter({Controller.class, RestControllerAdvice.class}))
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiService aiService;

    @MockBean
    private PromptTemplateService promptTemplateService;

    @MockBean
    private DiseaseKbService diseaseKbService;

    @MockBean
    private TriageLogMapper triageLogMapper;

    @MockBean
    private DiagnosisResultMapper diagnosisResultMapper;

    @MockBean
    private GenerationLogMapper generationLogMapper;

    private static final String TEMPLATE_ID = "PTMP_0000000000000001";

    // ==================== Mock 工厂方法 ====================

    private PromptTemplate mockTemplate() {
        PromptTemplate t = new PromptTemplate();
        t.setId(1L);
        t.setTemplateId(TEMPLATE_ID);
        t.setTemplateName("分诊默认模板");
        t.setTemplateType(0);
        t.setContent("你是一位资深全科医生…请返回 JSON…");
        t.setVariables("[\"chief_complaint\",\"age\",\"gender\"]");
        t.setVersion(1);
        t.setStatus(1);
        t.setCreateTime(LocalDateTime.now());
        t.setUpdateTime(LocalDateTime.now());
        return t;
    }

    private DiseaseKnowledge mockDisease() {
        DiseaseKnowledge d = new DiseaseKnowledge();
        d.setId(1L);
        d.setDiseaseId("DK_001");
        d.setDiseaseName("高血压");
        d.setIcdCode("I10");
        d.setCategory("心血管疾病");
        d.setRelatedDepartmentId("DEPT_001");
        d.setSymptoms("[\"头晕\",\"头痛\",\"心悸\"]");
        d.setDiagnosisBasis("血压持续≥140/90mmHg");
        d.setTreatmentPlan("生活方式干预+药物治疗");
        d.setStatus(1);
        d.setCreateTime(LocalDateTime.now());
        d.setUpdateTime(LocalDateTime.now());
        return d;
    }

    // ======================== Prompt 模板管理 ========================

    @Nested
    @DisplayName("Prompt 模板管理 - 正常操作")
    class PromptTemplateHappyPath {

        @Test
        @DisplayName("按类型查询模板列表 — 返回模板数组")
        void listByType() throws Exception {
            when(promptTemplateService.listByType(0)).thenReturn(List.of(mockTemplate()));

            mockMvc.perform(get("/api/ai/prompt-templates")
                            .param("type", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].templateId").value(TEMPLATE_ID))
                    .andExpect(jsonPath("$.data[0].templateName").value("分诊默认模板"))
                    .andExpect(jsonPath("$.data[0].templateType").value(0));
        }

        @Test
        @DisplayName("查询全部模板（不传 type）— 返回全部")
        void listAll() throws Exception {
            when(promptTemplateService.listAll()).thenReturn(List.of(mockTemplate()));

            mockMvc.perform(get("/api/ai/prompt-templates"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("新增模板 — 返回模板对象")
        void create() throws Exception {
            when(promptTemplateService.create(any(PromptTemplate.class))).thenReturn(mockTemplate());

            mockMvc.perform(post("/api/ai/prompt-template")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"templateName":"分诊默认模板","templateType":0,"content":"你是一位资深全科医生…","variables":["chief_complaint","age","gender"]}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.templateId").value(TEMPLATE_ID))
                    .andExpect(jsonPath("$.data.status").value(1));
        }

        @Test
        @DisplayName("更新模板 — 返回更新后的模板")
        void update() throws Exception {
            when(promptTemplateService.update(any(PromptTemplate.class))).thenReturn(mockTemplate());

            mockMvc.perform(put("/api/ai/prompt-template")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"templateId":"%s","templateName":"分诊模板v2","templateType":0,"content":"更新后的内容","variables":["chief_complaint","age"]}
                                    """.formatted(TEMPLATE_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.templateId").value(TEMPLATE_ID));
        }

        @Test
        @DisplayName("删除模板 — 返回操作成功")
        void deleteTemplate() throws Exception {
            doNothing().when(promptTemplateService).delete(TEMPLATE_ID);

            mockMvc.perform(delete("/api/ai/prompt-template/{templateId}", TEMPLATE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value("删除成功"));
        }

        @Test
        @DisplayName("启用模板 — 返回已启用")
        void enable() throws Exception {
            doNothing().when(promptTemplateService).updateStatus(TEMPLATE_ID, 1);

            mockMvc.perform(put("/api/ai/prompt-template/{templateId}/status", TEMPLATE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":1}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value("已启用"));
        }

        @Test
        @DisplayName("禁用模板 — 返回已禁用")
        void disable() throws Exception {
            doNothing().when(promptTemplateService).updateStatus(TEMPLATE_ID, 0);

            mockMvc.perform(put("/api/ai/prompt-template/{templateId}/status", TEMPLATE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":0}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value("已禁用"));
        }
    }

    // ======================== 疾病知识库管理 ========================

    @Nested
    @DisplayName("疾病知识库管理 - 正常操作")
    class DiseaseKbHappyPath {

        @Test
        @DisplayName("关键词搜索 — 返回匹配条目")
        void search() throws Exception {
            when(diseaseKbService.search("高血压")).thenReturn(List.of(mockDisease()));

            mockMvc.perform(get("/api/ai/disease-kb")
                            .param("keyword", "高血压"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].diseaseName").value("高血压"))
                    .andExpect(jsonPath("$.data[0].icdCode").value("I10"));
        }

        @Test
        @DisplayName("全量列表（不传 keyword）— 返回全部条目")
        void listAll() throws Exception {
            when(diseaseKbService.listAll()).thenReturn(List.of(mockDisease()));

            mockMvc.perform(get("/api/ai/disease-kb"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("新增疾病条目 — 返回条目对象")
        void create() throws Exception {
            when(diseaseKbService.create(any(DiseaseKnowledge.class))).thenReturn(mockDisease());

            mockMvc.perform(post("/api/ai/disease-kb")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"diseaseName":"高血压","icdCode":"I10","category":"心血管疾病","symptoms":["头晕","头痛","心悸"]}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.diseaseId").value("DK_001"))
                    .andExpect(jsonPath("$.data.diseaseName").value("高血压"));
        }

        @Test
        @DisplayName("更新疾病条目 — 返回更新后的条目")
        void update() throws Exception {
            when(diseaseKbService.update(any(DiseaseKnowledge.class))).thenReturn(mockDisease());

            mockMvc.perform(put("/api/ai/disease-kb")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"diseaseId":"DK_001","diseaseName":"原发性高血压","icdCode":"I10","category":"心血管疾病","symptoms":["头晕","头痛"]}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.diseaseId").value("DK_001"));
        }

        @Test
        @DisplayName("删除疾病条目 — 返回操作成功")
        void deleteDisease() throws Exception {
            doNothing().when(diseaseKbService).delete("DK_001");

            mockMvc.perform(delete("/api/ai/disease-kb/{diseaseId}", "DK_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value("删除成功"));
        }
    }

    // ======================== AI 调用监控 ========================

    @Nested
    @DisplayName("AI 调用监控 - 正常操作")
    class MonitorHappyPath {

        @Test
        @DisplayName("统计概览 — 返回汇总数据")
        void stats() throws Exception {
            when(triageLogMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);
            when(diagnosisResultMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);
            when(generationLogMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

            mockMvc.perform(get("/api/ai/monitor/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.totalCalls").value(18))
                    .andExpect(jsonPath("$.data.byType.triage.total").value(10))
                    .andExpect(jsonPath("$.data.byType.diagnosis.total").value(5))
                    .andExpect(jsonPath("$.data.byType.generation.total").value(3));
        }

        @Test
        @DisplayName("统计概览 — 带日期范围筛选")
        void stats_withDateRange() throws Exception {
            when(triageLogMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);
            when(diagnosisResultMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
            when(generationLogMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            mockMvc.perform(get("/api/ai/monitor/stats")
                            .param("startDate", "2024-01-01")
                            .param("endDate", "2024-01-31"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.totalCalls").value(4))
                    .andExpect(jsonPath("$.data.successRate").value(100.0));
        }

        @Test
        @DisplayName("调用明细 — type=0 分诊日志分页")
        void logs_triage() throws Exception {
            Page<TriageLog> mpPage = new Page<>(1, 10);
            mpPage.setTotal(1);
            TriageLog log = new TriageLog();
            log.setTriageId("TRIAGE_001");
            log.setPatientId("P001");
            log.setStatus(1);
            log.setCreateTime(LocalDateTime.now());
            mpPage.setRecords(List.of(log));
            when(triageLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mpPage);

            mockMvc.perform(get("/api/ai/monitor/logs")
                            .param("type", "0")
                            .param("page", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(1))
                    .andExpect(jsonPath("$.data.records[0].triageId").value("TRIAGE_001"));
        }

        @Test
        @DisplayName("调用明细 — type=1 诊断日志分页")
        void logs_diagnosis() throws Exception {
            Page<DiagnosisResult> mpPage = new Page<>(1, 10);
            mpPage.setTotal(0);
            mpPage.setRecords(List.of());
            when(diagnosisResultMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mpPage);

            mockMvc.perform(get("/api/ai/monitor/logs")
                            .param("type", "1")
                            .param("page", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(0));
        }

        @Test
        @DisplayName("调用明细 — type=3 生成日志分页（处方审核+病历生成）")
        void logs_generation() throws Exception {
            Page<GenerationLog> mpPage = new Page<>(1, 10);
            mpPage.setTotal(2);
            GenerationLog log = new GenerationLog();
            log.setGenerationId("GEN_001");
            log.setTargetType(0);
            log.setStatus(1);
            log.setCreateTime(LocalDateTime.now());
            mpPage.setRecords(List.of(log));
            when(generationLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mpPage);

            mockMvc.perform(get("/api/ai/monitor/logs")
                            .param("type", "3")
                            .param("page", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(2))
                    .andExpect(jsonPath("$.data.records[0].generationId").value("GEN_001"));
        }

        @Test
        @DisplayName("调用明细 — 不传 type 返回空")
        void logs_noType() throws Exception {
            mockMvc.perform(get("/api/ai/monitor/logs")
                            .param("page", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(0));
        }
    }

    // ======================== 业务异常 ========================

    @Nested
    @DisplayName("业务异常场景")
    class ServiceException {

        @Test
        @DisplayName("删除模板 — 有关联数据无法删除")
        void deletePrompt_cannotDelete() throws Exception {
            doThrow(new BusinessException(400, "该模板为系统默认模板，无法删除"))
                    .when(promptTemplateService).delete("PTMP_SYSTEM");

            mockMvc.perform(delete("/api/ai/prompt-template/{templateId}", "PTMP_SYSTEM"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("该模板为系统默认模板，无法删除"));
        }

        @Test
        @DisplayName("删除疾病条目 — 条目不存在")
        void deleteDisease_notFound() throws Exception {
            doThrow(new BusinessException(404, "疾病条目不存在"))
                    .when(diseaseKbService).delete("NOT_EXIST");

            mockMvc.perform(delete("/api/ai/disease-kb/{diseaseId}", "NOT_EXIST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value("疾病条目不存在"));
        }
    }
}
