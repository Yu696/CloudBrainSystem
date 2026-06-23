import request from '@/utils/request'

/** 医生列表 */
export function listDoctorsApi(departmentId?: string) {
  return request.get<any[]>('/doctor/list', { params: { departmentId } })
}

/** 医生详情 */
export function getDoctorDetailApi(doctorId: string) {
  return request.get<any>('/doctor/detail', { params: { doctorId } })
}

/** 根据用户ID查询医生信息 */
export function getDoctorByUserIdApi(userId: string) {
  return request.get<any>('/doctor/get-by-user', { params: { userId } })
}

/** 更新医生信息（专长、简介等） */
export function updateDoctorApi(data: {
  doctorId?: string
  userId?: string
  specialty?: string
  introduction?: string
}) {
  return request.put<string>('/doctor/update', data)
}
