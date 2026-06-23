import request from '@/utils/request'

/** 科室列表 */
export function listDepartmentsApi(category?: string) {
  return request.get<any[]>('/department/list', { params: { category } })
}

/** 新增科室 */
export function createDepartmentApi(data: { name: string; parentId?: string; category?: string; description?: string; location?: string; phone?: string; sortOrder?: number }) {
  return request.post<any>('/department/create', data)
}

/** 更新科室 */
export function updateDepartmentApi(departmentId: string, data: any) {
  return request.put<string>('/department/update', data, { params: { departmentId } })
}

/** 删除科室 */
export function deleteDepartmentApi(departmentId: string) {
  return request.delete<string>('/department/delete', { params: { departmentId } })
}

/** 医生列表 */
export function listDoctorsApi(departmentId?: string) {
  return request.get<any[]>('/doctor/list', { params: { departmentId } })
}

/** 医生详情 */
export function getDoctorDetailApi(doctorId: string) {
  return request.get<any>('/doctor/detail', { params: { doctorId } })
}

/** 当前登录医生信息 */
export function getMyDoctorInfoApi() {
  return request.get<any>('/doctor/me')
}

/** 管理员医生列表（含不可用） */
export function adminListDoctorsApi(departmentId?: string) {
  return request.get<any[]>('/doctor/admin/list', { params: { departmentId } })
}

/** 更新医生信息 */
export function updateDoctorApi(doctorId: string, data: any) {
  return request.put<string>('/doctor/update', data, { params: { doctorId } })
}

/** 切换医生接诊状态 */
export function toggleDoctorAvailableApi(doctorId: string) {
  return request.put<string>('/doctor/toggle', null, { params: { doctorId } })
}

/** 设置排班 */
export function setScheduleApi(data: {
  doctorId: string
  departmentId: string
  scheduleDate: string
  workShift: number
  startTime: string
  endTime: string
  slotDuration?: number
  maxPatients?: number
}) {
  return request.post<any>('/schedule/set', data)
}

/** 查询排班 */
export function queryScheduleApi(doctorId: string, startDate?: string, endDate?: string) {
  return request.get<any[]>('/schedule/query', { params: { doctorId, startDate, endDate } })
}

/** 可用时段 */
export function getAvailableSlotsApi(doctorId: string, date: string) {
  return request.get<any[]>('/schedule/available', { params: { doctorId, date } })
}

/** 预约挂号 */
export function bookAppointmentApi(data: {
  patientId: string
  doctorId: string
  departmentId: string
  slotId: string
  symptoms?: string
  appointmentType?: number
}) {
  return request.post<any>('/appointment/book', data)
}

/** 取消预约 */
export function cancelAppointmentApi(data: { appointmentId: string; reason?: string }) {
  return request.post<string>('/appointment/cancel', data)
}

/** 预约详情 */
export function getAppointmentDetailApi(appointmentId: string) {
  return request.get<any>('/appointment/detail', { params: { appointmentId } })
}

/** 预约列表 */
export function listAppointmentsApi(patientId?: string, doctorId?: string) {
  return request.get<any[]>('/appointment/list', { params: { patientId, doctorId } })
}

/** 创建支付 */
export function createPaymentApi(data: { appointmentId: string; patientId: string; paymentMethod: number }) {
  return request.post<any>('/payment/create', data)
}

/** 支付状态 */
export function getPaymentStatusApi(paymentId: string) {
  return request.get<any>('/payment/status', { params: { paymentId } })
}

/** 管理员预约列表（支持多条件筛选） */
export function adminListAppointmentsApi(params?: { patientId?: string; doctorId?: string; date?: string; status?: number }) {
  return request.get<any[]>('/appointment/admin/list', { params })
}

/** 删除预约 */
export function deleteAppointmentApi(appointmentId: string) {
  return request.delete<string>('/appointment/delete', { params: { appointmentId } })
}

/** 仪表盘统计数据 */
export function getDashboardStatsApi() {
  return request.get<{ todayAppointments: number; waitingCount: number; monthNewPatients: number; todayCompleted: number }>('/dashboard/stats')
}
