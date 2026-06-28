package com.cloudbrain.controller.ai;

import com.cloudbrain.common.BaseController;
import com.cloudbrain.common.Result;
import com.cloudbrain.dto.PageResult;
import com.cloudbrain.dto.request.ai.*;
import com.cloudbrain.dto.response.ai.*;
import com.cloudbrain.entity.TriageLog;
import com.cloudbrain.service.ai.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AI 辅助诊断 Controller
 * 严格按阶段二 API 规范实现 8 个核心接口
 *
 * @author 余润东
 */
@Tag(name = "AI 辅助诊断")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController extends BaseController {

    private final AiService aiService;

    @Operation(summary = "AI-01/02/03 智能分诊")
    @PostMapping("/triage")
    public Result<TriageVO> triage(@Valid @RequestBody TriageRequest request) {
        return success(aiService.triage(request));
    }

    @Operation(summary = "AI-04 分诊历史（分页）")
    @GetMapping("/triage-history")
    public Result<PageResult<TriageLog>> getTriageHistory(
            @Parameter(description = "患者ID") @RequestParam String patientId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        return success(aiService.getTriageHistory(patientId, page, pageSize));
    }

    @Operation(summary = "AI-05/06 AI 诊断分析")
    @PostMapping("/diagnosis")
    public Result<DiagnosisVO> diagnosis(@Valid @RequestBody DiagnosisRequest request) {
        return success(aiService.diagnosis(request));
    }

    @Operation(summary = "AI-07 获取诊断报告")
    @GetMapping("/diagnosis-report")
    public Result<DiagnosisVO> getDiagnosisReport(
            @Parameter(description = "诊断ID") @RequestParam String diagnosisId) {
        return success(aiService.getDiagnosisReport(diagnosisId));
    }

    @Operation(summary = "AI-08/09/10 处方审核")
    @PostMapping("/prescription-check")
    public Result<PrescriptionAuditVO> prescriptionCheck(@Valid @RequestBody PrescriptionCheckRequest request) {
        return success(aiService.prescriptionCheck(request));
    }

    @Operation(summary = "AI-11 病历生成")
    @PostMapping("/record-generate")
    public Result<RecordGenerateVO> recordGenerate(@Valid @RequestBody RecordGenerateRequest request) {
        return success(aiService.recordGenerate(request));
    }

    @Operation(summary = "AI-13 病历确认归档")
    @PutMapping("/record-generate/{generationId}/confirm")
    public Result<String> confirmRecord(
            @Parameter(description = "生成ID") @PathVariable String generationId,
            @Valid @RequestBody RecordGenerateRequest.ConfirmBody body) {
        aiService.confirmRecord(generationId, body);
        return success("归档成功");
    }

    @Operation(summary = "AI 影像诊断（预留）")
    @PostMapping("/image-diagnosis")
    public Result<ImageDiagnosisVO> imageDiagnosis(@Valid @RequestBody ImageDiagnosisRequest request) {
        return success(aiService.imageDiagnosis(request));
    }
}
