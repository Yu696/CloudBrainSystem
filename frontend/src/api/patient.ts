import request from '@/utils/request'

/** 创建患者档案 */
export function createPatientApi(data: {
  name: string
  idCard: string
  gender: number
  phone: string
  birthDate: string
  address?: string
  bloodType?: string
  allergyHistory?: string
  medicalHistory?: string
}) {
  return request.post<{ patientId: string; medicalRecordNo: string }>('/patient/create', data)
}

/** 查询患者信息 */
export function getPatientInfoApi(patientId: string) {
  return request.get<any>('/patient/info', { params: { patientId } })
}

/** 患者列表（旧接口） */
export function listPatientsApi(params?: { name?: string; phone?: string; medicalRecordNo?: string }) {
  return request.get<any[]>('/patient/list', { params })
}

/** 管理员端患者列表 */
export function adminListPatientsApi(params?: { name?: string; phone?: string; medicalRecordNo?: string }) {
  return request.get<any[]>('/admin/patients', { params })
}

/** 医生端患者列表（仅挂过该医生号的患者） */
export function doctorListPatientsApi(params?: { name?: string; phone?: string; medicalRecordNo?: string }) {
  return request.get<any[]>('/doctor/patients', { params })
}

/** 更新患者档案 */
export function updatePatientApi(data: { patientId: string; [key: string]: any }) {
  return request.put<string>('/patient/update', data)
}

/** 校验身份证 */
export function checkIdCardApi(idCard: string) {
  return request.get<{ exists: boolean }>('/patient/check-idcard', { params: { idCard } })
}

/** 根据登录用户ID查找患者档案 */
export function findPatientByUserIdApi(userId: string) {
  return request.get<any>('/patient/find-by-user', { params: { userId } })
}
