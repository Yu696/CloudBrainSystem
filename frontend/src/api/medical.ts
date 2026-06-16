import request from '@/utils/request'

/** 创建病历 */
export function createMedicalRecordApi(data: {
  patientId: string
  doctorId: string
  appointmentId: string
  chiefComplaint: string
  presentIllness?: string
  diagnosis?: string
  treatmentPlan?: string
}) {
  return request.post<{ recordId: string }>('/medical-record/create', data)
}

/** 病历列表 */
export function listMedicalRecordsApi(patientId?: string, doctorId?: string) {
  return request.get<any[]>('/medical-record/list', { params: { patientId, doctorId } })
}

/** 病历详情 */
export function getMedicalRecordDetailApi(recordId: string) {
  return request.get<any>('/medical-record/detail', { params: { recordId } })
}

/** 更新病历 */
export function updateMedicalRecordApi(data: { recordId: string; [key: string]: any }) {
  return request.put<string>('/medical-record/update', data)
}

/** 完成病历 */
export function completeMedicalRecordApi(recordId: string) {
  return request.put<string>('/medical-record/complete', { recordId })
}

/** 开具处方 */
export function createPrescriptionApi(data: { recordId: string; items: { drugId: string; dosage: string; frequency: string; days: number }[] }) {
  return request.post<{ prescriptionId: string }>('/prescription/create', data)
}

/** 处方列表 */
export function listPrescriptionsApi(recordId: string) {
  return request.get<any[]>('/prescription/list', { params: { recordId } })
}

/** 处方详情 */
export function getPrescriptionDetailApi(prescriptionId: string) {
  return request.get<any>('/prescription/detail', { params: { prescriptionId } })
}

/** 开检查单 */
export function createExaminationOrderApi(data: { recordId: string; examName: string; category: string }) {
  return request.post<{ orderId: string }>('/examination/create', data)
}

/** 检查单列表 */
export function listExaminationOrdersApi(recordId: string) {
  return request.get<any[]>('/examination/list', { params: { recordId } })
}

/** 检查结果 */
export function getExaminationResultApi(orderId: string) {
  return request.get<any>('/examination/result', { params: { orderId } })
}
