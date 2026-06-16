import request from '@/utils/request'

/** 科室列表 */
export function listDepartmentsApi(category?: string) {
  return request.get<any[]>('/department/list', { params: { category } })
}

/** 医生列表 */
export function listDoctorsApi(departmentId?: string) {
  return request.get<any[]>('/doctor/list', { params: { departmentId } })
}

/** 医生详情 */
export function getDoctorDetailApi(doctorId: string) {
  return request.get<any>('/doctor/detail', { params: { doctorId } })
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
