package com.cloudbrain.service.ai;

import com.cloudbrain.dto.request.ai.*;
import com.cloudbrain.dto.response.ai.*;
import com.cloudbrain.dto.PageResult;
import com.cloudbrain.entity.TriageLog;

/** AI 核心服务接口 */
public interface AiService {

    /** AI-01/02/03 智能分诊 */
    TriageVO triage(TriageRequest request);

    /** AI-04 分诊历史（分页） */
    PageResult<TriageLog> getTriageHistory(String patientId, int page, int pageSize);

    /** AI-05/06 AI 诊断分析 */
    DiagnosisVO diagnosis(DiagnosisRequest request);

    /** AI-07 获取诊断报告 */
    DiagnosisVO getDiagnosisReport(String diagnosisId);

    /** AI-08/09/10 处方审核 */
    PrescriptionAuditVO prescriptionCheck(PrescriptionCheckRequest request);

    /** AI-11 病历生成 */
    RecordGenerateVO recordGenerate(RecordGenerateRequest request);

    /** AI-13 病历确认归档 */
    void confirmRecord(String generationId, RecordGenerateRequest.ConfirmBody body);

    /** AI 影像诊断（预留） */
    ImageDiagnosisVO imageDiagnosis(ImageDiagnosisRequest request);
}
