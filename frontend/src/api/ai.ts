import request from '@/utils/request'

// ==================== 患者端 ====================

/** 智能分诊（AI-01/02/03） */
export function triageApi(data: {
  patientId: string
  chiefComplaint: string
  additionalInfo?: Record<string, any>
}) {
  return request.post<any>('/ai/triage', data)
}

/** 分诊历史（AI-04） */
export function triageHistoryApi(params: {
  patientId: string
  page?: number
  pageSize?: number
}) {
  return request.get<any>('/ai/triage-history', { params })
}

// ==================== 医生端 ====================

/** AI 诊断分析（AI-05/06） */
export function diagnosisApi(data: {
  patientId: string
  doctorId: string
  diagnosisType: number
  symptomData: Record<string, any>
}) {
  return request.post<any>('/ai/diagnosis', data)
}

/** 获取诊断报告（AI-07） */
export function getDiagnosisReportApi(diagnosisId: string) {
  return request.get<any>('/ai/diagnosis-report', { params: { diagnosisId } })
}

/** AI 处方审核（AI-08/09/10） */
export function prescriptionCheckApi(data: {
  prescriptionId: string
  recordId: string
  patientId: string
  doctorId: string
  items: { drugId: string; drugName: string; dosage: string; frequency: string; days: number }[]
}) {
  return request.post<any>('/ai/prescription-check', data)
}

/** AI 病历生成（AI-11） */
export function recordGenerateApi(data: {
  patientId: string
  doctorId: string
  appointmentId: string
  dialogueText: string
}) {
  return request.post<any>('/ai/record-generate', data)
}

/** 病历确认归档（AI-13） */
export function confirmRecordApi(generationId: string, data: {
  recordPreview: Record<string, any>
  recordId?: string
}) {
  return request.put<any>(`/ai/record-generate/${generationId}/confirm`, data)
}

/** AI 影像诊断（预留） */
export function imageDiagnosisApi(data: {
  imageId: string
  diagnosisType: number
  patientId: string
  doctorId: string
}) {
  return request.post<any>('/ai/image-diagnosis', data)
}

// ==================== 管理端 ====================

// ---- Prompt 模板管理（AI-14） ----

/** 按类型查询模板列表 */
export function listPromptTemplatesApi(type?: number) {
  return request.get<any>('/ai/prompt-templates', { params: { type } })
}

/** 新增 Prompt 模板 */
export function createPromptTemplateApi(data: {
  templateName: string
  templateType: number
  content: string
  variables: string[]
}) {
  return request.post<any>('/ai/prompt-template', data)
}

/** 更新 Prompt 模板 */
export function updatePromptTemplateApi(data: any) {
  return request.put<any>('/ai/prompt-template', data)
}

/** 删除模板 */
export function deletePromptTemplateApi(templateId: string) {
  return request.delete<any>(`/ai/prompt-template/${templateId}`)
}

/** 启用/禁用模板 */
export function togglePromptTemplateStatusApi(templateId: string, status: number) {
  return request.put<any>(`/ai/prompt-template/${templateId}/status`, { status })
}

// ---- 疾病知识库管理（AI-15） ----

/** 搜索疾病知识库 */
export function searchDiseaseKbApi(keyword?: string) {
  return request.get<any>('/ai/disease-kb', { params: { keyword } })
}

/** 新增疾病条目 */
export function createDiseaseKbApi(data: {
  diseaseName: string
  icdCode: string
  category: string
  relatedDepartmentId?: string
  symptoms: string[]
  diagnosisBasis: string
  treatmentPlan: string
}) {
  return request.post<any>('/ai/disease-kb', data)
}

/** 更新疾病条目 */
export function updateDiseaseKbApi(data: any) {
  return request.put<any>('/ai/disease-kb', data)
}

/** 删除疾病条目 */
export function deleteDiseaseKbApi(diseaseId: string) {
  return request.delete<any>(`/ai/disease-kb/${diseaseId}`)
}

// ---- AI 调用监控（AI-16） ----

/** AI 调用统计概览 */
export function aiMonitorStatsApi(params: { startDate: string; endDate: string }) {
  return request.get<any>('/ai/monitor/stats', { params })
}

/** AI 调用明细列表 */
export function aiMonitorLogsApi(params: {
  type?: number
  page?: number
  pageSize?: number
  startDate?: string
  endDate?: string
}) {
  return request.get<any>('/ai/monitor/logs', { params })
}
