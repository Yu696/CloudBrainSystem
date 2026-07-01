import request from '@/utils/request'

/** 查询钱包余额 */
export function walletBalanceApi(patientId: string) {
  return request.get<any>('/wallet/balance', { params: { patientId } })
}

/** 充值 */
export function walletRechargeApi(patientId: string, amount: number) {
  return request.post<any>('/wallet/recharge', { patientId, amount })
}

/** 交易记录 */
export function walletTransactionsApi(patientId: string) {
  return request.get<any>('/wallet/transactions', { params: { patientId } })
}

/** 检查费支付 */
export function payExamApi(orderId: string) {
  return request.post<any>('/examination/pay', null, { params: { orderId } })
}

/** 待支付订单列表 */
export function pendingOrdersApi(patientId: string) {
  return request.get<any>('/wallet/pending-orders', { params: { patientId } })
}

/** 处方药费支付 */
export function payPrescriptionApi(prescriptionId: string) {
  return request.post<string>('/prescription/pay', null, { params: { prescriptionId } })
}
