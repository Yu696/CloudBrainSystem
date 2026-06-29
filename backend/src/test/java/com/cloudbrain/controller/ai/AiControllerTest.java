package com.cloudbrain.controller.ai;

import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.config.TestDataSourceConfig;
import com.cloudbrain.dto.PageResult;
import com.cloudbrain.dto.request.ai.*;
import com.cloudbrain.dto.response.ai.*;
import com.cloudbrain.entity.TriageLog;
import com.cloudbrain.mapper.AiCallLogMapper;
import com.cloudbrain.mapper.DiagnosisResultMapper;
import com.cloudbrain.mapper.GenerationLogMapper;
import com.cloudbrain.mapper.PatientMapper;
import com.cloudbrain.mapper.TriageLogMapper;
import com.cloudbrain.service.ai.AiService;
import com.cloudbrain.service.ai.DiseaseKbService;
import com.cloudbrain.service.ai.PromptTemplateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AI 核心接口单元测试（患者端 + 医生端）
 * 覆盖 AiController 的全部 8 个端点
 */
@SpringBootTest(classes = AiControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AiController 单元测试")
class AiControllerTest {

    @Configuration
    @Import(TestDataSourceConfig.class)
    @EnableAutoConfiguration(exclude = {
            SecurityAutoConfiguration.class,
            SecurityFilterAutoConfiguration.class
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

    // AiAdminController's dependencies (also scanned by component scan)
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
    @MockBean
    private AiCallLogMapper aiCallLogMapper;
    @MockBean
    private PatientMapper patientMapper;

    private static final String PATIENT_ID = "P2024010100001";
    private static final String DOCTOR_ID = "D2024010100001";
    private static final String DIAGNOSIS_ID = "DIAG_001";
    private static final String GENERATION_ID = "GEN_001";

    // ==================== Mock VO 工厂方法 ====================

    private TriageVO mockTriageVO() {
        return TriageVO.builder()
                .triageId("TRIAGE_001")
                .chiefComplaint("头痛、发热三天")
                .recommendedDepartment(TriageVO.DepartmentInfo.builder()
                        .departmentId("DEPT_001")
                        .departmentName("呼吸内科")
                        .confidence(new BigDecimal("0.85"))
                        .build())
                .diseaseMatches(List.of(TriageVO.DiseaseMatch.builder()
                        .diseaseName("上呼吸道感染")
                        .icdCode("J06.9")
                        .confidence(new BigDecimal("0.85"))
                        .matchedSymptoms(List.of("头痛", "发热"))
                        .build()))
                .confidenceScore(new BigDecimal("0.85"))
                .needsManualReview(false)
                .analysisDetail("根据症状分析，推荐前往呼吸内科就诊")
                .aiModel("deepseek-chat")
                .responseTimeMs(1200)
                .build();
    }

    private DiagnosisVO mockDiagnosisVO() {
        return DiagnosisVO.builder()
                .diagnosisId(DIAGNOSIS_ID)
                .symptomData(Map.of("chiefComplaint", "胸痛、心悸"))
                .diseaseMatches(List.of(DiagnosisVO.DiseaseMatch.builder()
                        .diseaseName("冠心病")
                        .icdCode("I25.1")
                        .confidence(new BigDecimal("0.82"))
                        .diagnosisBasis("心电图ST段异常，结合症状判断")
                        .differentialDiagnosis(List.of("心肌炎", "心包炎"))
                        .build()))
                .confidenceScore(new BigDecimal("0.82"))
                .needsManualReview(false)
                .analysisResult("综合考虑，初步诊断冠状动脉疾病")
                .status(DiagnosisVO.STATUS_COMPLETE)
                .aiModel("deepseek-chat")
                .build();
    }

    private PrescriptionAuditVO mockPrescriptionAuditVO() {
        return PrescriptionAuditVO.builder()
                .auditId("AUDIT_001")
                .overallResult(PrescriptionAuditVO.PASS)
                .overallResultName("通过")
                .items(List.of(PrescriptionAuditVO.AuditItem.builder()
                        .drugName("阿莫西林")
                        .result(PrescriptionAuditVO.PASS)
                        .resultName("通过")
                        .checks(List.of(
                                PrescriptionAuditVO.AuditCheck.builder()
                                        .checkType("过敏检测").result("PASS").detail("未发现过敏记录").build(),
                                PrescriptionAuditVO.AuditCheck.builder()
                                        .checkType("剂量检查").result("PASS").detail("剂量在常规范围内").build()
                        ))
                        .build()))
                .summary("处方审核通过")
                .confidenceScore(new BigDecimal("0.95"))
                .aiModel("deepseek-chat")
                .build();
    }

    private RecordGenerateVO mockRecordGenerateVO() {
        return RecordGenerateVO.builder()
                .generationId(GENERATION_ID)
                .recordPreview(RecordGenerateVO.RecordPreview.builder()
                        .chiefComplaint("头痛三天")
                        .presentIllness("患者三天前出现阵发性头痛")
                        .pastHistory("既往体健")
                        .physicalExam("体温36.5℃，血压120/80mmHg")
                        .preliminaryDiagnosis("紧张性头痛")
                        .treatmentPlan("建议休息，必要时服用止痛药")
                        .build())
                .isConfirmed(false)
                .aiModel("deepseek-chat")
                .responseTimeMs(1500)
                .build();
    }

    private ImageDiagnosisVO mockImageDiagnosisVO() {
        return ImageDiagnosisVO.builder()
                .diagnosisId("IMG_DIAG_001")
                .imageId("IMG_001")
                .diagnosisType(1)
                .diagnosisTypeName("影像诊断")
                .findings(ImageDiagnosisVO.Findings.builder()
                        .abnormalRegions(List.of(ImageDiagnosisVO.AbnormalRegion.builder()
                                .location("右上肺")
                                .size("2.3cm×1.8cm")
                                .description("结节状高密度影")
                                .confidence(new BigDecimal("0.78"))
                                .build()))
                        .summaryDetail("右上肺见结节状高密度影，建议进一步检查")
                        .build())
                .confidenceScore(new BigDecimal("0.78"))
                .status(2)
                .aiModel("deepseek-chat")
                .build();
    }

    // ======================== 正案例 ========================

    @Nested
    @DisplayName("正常操作")
    class HappyPath {

        @Test
        @DisplayName("AI-01 智能分诊 — 返回分诊结果")
        void triage_success() throws Exception {
            when(aiService.triage(any(TriageRequest.class))).thenReturn(mockTriageVO());

            mockMvc.perform(post("/api/ai/triage")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"patientId":"%s","chiefComplaint":"头痛、发热三天"}
                                    """.formatted(PATIENT_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.triageId").value("TRIAGE_001"))
                    .andExpect(jsonPath("$.data.recommendedDepartment.departmentName").value("呼吸内科"))
                    .andExpect(jsonPath("$.data.confidenceScore").value(0.85))
                    .andExpect(jsonPath("$.data.aiModel").value("deepseek-chat"));
        }

        @Test
        @DisplayName("AI-04 分诊历史 — 返回分页数据")
        void triageHistory_success() throws Exception {
            PageResult<TriageLog> pageResult = PageResult.of(1, 1, 10, List.of(new TriageLog()));
            when(aiService.getTriageHistory(eq(PATIENT_ID), eq(1), eq(10))).thenReturn(pageResult);

            mockMvc.perform(get("/api/ai/triage-history")
                            .param("patientId", PATIENT_ID)
                            .param("page", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(1))
                    .andExpect(jsonPath("$.data.page").value(1));
        }

        @Test
        @DisplayName("AI-05 AI 诊断分析 — 返回诊断结果")
        void diagnosis_success() throws Exception {
            when(aiService.diagnosis(any(DiagnosisRequest.class))).thenReturn(mockDiagnosisVO());

            mockMvc.perform(post("/api/ai/diagnosis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"patientId":"%s","doctorId":"%s","diagnosisType":0,"symptomData":{"chiefComplaint":"胸痛、心悸"}}
                                    """.formatted(PATIENT_ID, DOCTOR_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.diagnosisId").value(DIAGNOSIS_ID))
                    .andExpect(jsonPath("$.data.status").value(2))
                    .andExpect(jsonPath("$.data.diseaseMatches[0].diseaseName").value("冠心病"));
        }

        @Test
        @DisplayName("AI-07 获取诊断报告 — 返回报告详情")
        void diagnosisReport_success() throws Exception {
            when(aiService.getDiagnosisReport(DIAGNOSIS_ID)).thenReturn(mockDiagnosisVO());

            mockMvc.perform(get("/api/ai/diagnosis-report")
                            .param("diagnosisId", DIAGNOSIS_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.diagnosisId").value(DIAGNOSIS_ID))
                    .andExpect(jsonPath("$.data.analysisResult").value("综合考虑，初步诊断冠状动脉疾病"));
        }

        @Test
        @DisplayName("AI-08 处方审核 — 返回审核结果")
        void prescriptionCheck_success() throws Exception {
            when(aiService.prescriptionCheck(any(PrescriptionCheckRequest.class)))
                    .thenReturn(mockPrescriptionAuditVO());

            mockMvc.perform(post("/api/ai/prescription-check")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"prescriptionId":"PRES_001","recordId":"REC_001","patientId":"%s","doctorId":"%s","items":[{"drugId":"DRUG_001","drugName":"阿莫西林","dosage":"0.5g","frequency":"tid","days":7}]}
                                    """.formatted(PATIENT_ID, DOCTOR_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.auditId").value("AUDIT_001"))
                    .andExpect(jsonPath("$.data.overallResult").value("PASS"))
                    .andExpect(jsonPath("$.data.items[0].drugName").value("阿莫西林"));
        }

        @Test
        @DisplayName("AI-11 病历生成 — 返回结构化病历预览")
        void recordGenerate_success() throws Exception {
            when(aiService.recordGenerate(any(RecordGenerateRequest.class)))
                    .thenReturn(mockRecordGenerateVO());

            mockMvc.perform(post("/api/ai/record-generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"patientId":"%s","doctorId":"%s","appointmentId":"APT_001","dialogueText":"医生：你哪里不舒服？\\n患者：头痛三天了"}
                                    """.formatted(PATIENT_ID, DOCTOR_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.generationId").value(GENERATION_ID))
                    .andExpect(jsonPath("$.data.recordPreview.chiefComplaint").value("头痛三天"))
                    .andExpect(jsonPath("$.data.isConfirmed").value(false));
        }

        @Test
        @DisplayName("AI-13 病历确认归档 — 返回操作成功")
        void confirmRecord_success() throws Exception {
            doNothing().when(aiService).confirmRecord(eq(GENERATION_ID), any(RecordGenerateRequest.ConfirmBody.class));

            mockMvc.perform(put("/api/ai/record-generate/{generationId}/confirm", GENERATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"recordPreview":{"chiefComplaint":"头痛三天"},"recordId":"REC_001"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value("归档成功"));
        }

        @Test
        @DisplayName("AI 影像诊断（预留） — 返回模拟结果")
        void imageDiagnosis_success() throws Exception {
            when(aiService.imageDiagnosis(any(ImageDiagnosisRequest.class)))
                    .thenReturn(mockImageDiagnosisVO());

            mockMvc.perform(post("/api/ai/image-diagnosis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"imageId":"IMG_001","diagnosisType":1,"patientId":"%s","doctorId":"%s"}
                                    """.formatted(PATIENT_ID, DOCTOR_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.diagnosisId").value("IMG_DIAG_001"))
                    .andExpect(jsonPath("$.data.findings.summaryDetail").value("右上肺见结节状高密度影，建议进一步检查"));
        }
    }

    // ======================== 参数校验失败 ========================

    @Nested
    @DisplayName("参数校验失败")
    class ValidationError {

        @Test
        @DisplayName("分诊 — 缺少 patientId 返回 400")
        void triage_missingPatientId() throws Exception {
            mockMvc.perform(post("/api/ai/triage")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"chiefComplaint":"头痛"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("分诊 — 缺少 chiefComplaint 返回 400")
        void triage_missingComplaint() throws Exception {
            mockMvc.perform(post("/api/ai/triage")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"patientId":"%s"}
                                    """.formatted(PATIENT_ID)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("分诊 — 空请求体返回 400")
        void triage_emptyBody() throws Exception {
            mockMvc.perform(post("/api/ai/triage")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("诊断 — 缺少 doctorId 返回 400")
        void diagnosis_missingDoctorId() throws Exception {
            mockMvc.perform(post("/api/ai/diagnosis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"patientId":"%s","diagnosisType":0}
                                    """.formatted(PATIENT_ID)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("诊断 — 缺少 diagnosisType 返回 400")
        void diagnosis_missingType() throws Exception {
            mockMvc.perform(post("/api/ai/diagnosis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"patientId":"%s","doctorId":"%s"}
                                    """.formatted(PATIENT_ID, DOCTOR_ID)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("处方审核 — 缺少 items 返回 400")
        void prescriptionCheck_emptyItems() throws Exception {
            mockMvc.perform(post("/api/ai/prescription-check")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"recordId":"REC_001","patientId":"%s","doctorId":"%s","items":[]}
                                    """.formatted(PATIENT_ID, DOCTOR_ID)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("处方审核 — 缺少必填字段返回 400")
        void prescriptionCheck_missingFields() throws Exception {
            mockMvc.perform(post("/api/ai/prescription-check")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("病历生成 — 缺少 dialogueText 返回 400")
        void recordGenerate_missingDialogue() throws Exception {
            mockMvc.perform(post("/api/ai/record-generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"patientId":"%s","doctorId":"%s"}
                                    """.formatted(PATIENT_ID, DOCTOR_ID)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("影像诊断 — 缺少 imageId 返回 400")
        void imageDiagnosis_missingImageId() throws Exception {
            mockMvc.perform(post("/api/ai/image-diagnosis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"diagnosisType":1,"patientId":"%s","doctorId":"%s"}
                                    """.formatted(PATIENT_ID, DOCTOR_ID)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }
    }

    // ======================== Service 异常 ========================

    @Nested
    @DisplayName("Service 抛出业务异常")
    class ServiceException {

        @Test
        @DisplayName("分诊 — AI 服务超时执行降级")
        void triage_aiTimeout() throws Exception {
            when(aiService.triage(any(TriageRequest.class)))
                    .thenThrow(new BusinessException(503, "AI 服务响应超时，已为您提供备用方案"));

            mockMvc.perform(post("/api/ai/triage")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"patientId":"%s","chiefComplaint":"头痛"}
                                    """.formatted(PATIENT_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(503))
                    .andExpect(jsonPath("$.message").value("AI 服务响应超时，已为您提供备用方案"));
        }

        @Test
        @DisplayName("诊断报告 — 诊断ID不存在")
        void diagnosisReport_notFound() throws Exception {
            when(aiService.getDiagnosisReport("NOT_EXIST"))
                    .thenThrow(new BusinessException(404, "诊断报告不存在"));

            mockMvc.perform(get("/api/ai/diagnosis-report")
                            .param("diagnosisId", "NOT_EXIST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value("诊断报告不存在"));
        }

        @Test
        @DisplayName("病历确认归档 — 生成记录不存在")
        void confirmRecord_notFound() throws Exception {
            doThrow(new BusinessException(404, "生成记录不存在"))
                    .when(aiService).confirmRecord(eq("NOT_EXIST"), any(RecordGenerateRequest.ConfirmBody.class));

            mockMvc.perform(put("/api/ai/record-generate/{generationId}/confirm", "NOT_EXIST")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"recordPreview":{"chiefComplaint":"头痛"},"recordId":"REC_001"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value("生成记录不存在"));
        }

        @Test
        @DisplayName("分诊 — 调用频率超限")
        void triage_rateLimited() throws Exception {
            when(aiService.triage(any(TriageRequest.class)))
                    .thenThrow(new BusinessException(429, "调用过于频繁，请稍后再试"));

            mockMvc.perform(post("/api/ai/triage")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"patientId":"%s","chiefComplaint":"头痛"}
                                    """.formatted(PATIENT_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(429))
                    .andExpect(jsonPath("$.message").value("调用过于频繁，请稍后再试"));
        }
    }
}
