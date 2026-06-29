import request from '@/utils/request'

/** 新增药品（DR-01） */
export function addDrugApi(data: {
  drugCode?: string
  drugName: string
  genericName?: string
  ingredients?: string
  spec?: string
  dosageForm?: string
  manufacturer?: string
  unit: string
  unitPrice: number
  purchasePrice?: number
  usageMethod?: string
  cautiousCrowd?: string
  sideEffects?: string
  drugCategory?: string
  prescriptionType: number
}) {
  return request.post<any>('/drug/add', data)
}

/** 修改药品（DR-02） */
export function updateDrugApi(data: Record<string, any>) {
  return request.put<any>('/drug/update', data)
}

/** 删除药品（DR-03） */
export function deleteDrugApi(drugId: string) {
  return request.delete<any>('/drug/delete', { params: { drugId } })
}

/** 搜索药品（DR-04） */
export function searchDrugApi(params: {
  keyword?: string
  category?: string
  prescriptionType?: number
  page?: number
  pageSize?: number
}) {
  return request.get<any>('/drug/search', { params })
}

/** 查询库存（DR-05） */
export function drugStockApi(drugId: string) {
  return request.get<any>('/drug/stock', { params: { drugId } })
}

/** 库存列表（分页） */
export function drugStockListApi(params: {
  warehouseId?: string
  keyword?: string
  page?: number
  pageSize?: number
}) {
  return request.get<any>('/drug/stock/list', { params: { ...params, _t: Date.now() } })
}

/** 库存预警列表（DR-06） */
export function lowStockAlertApi(type?: number) {
  return request.get<any>('/drug/low-stock', { params: { type, _t: Date.now() } })
}

/** 发药出库（DR-07） */
export function dispenseDrugApi(data: {
  prescriptionId: string
  patientId: string
  items: { drugId: string; quantity: number }[]
}) {
  return request.post<any>('/drug/dispense', data)
}

/** 取药单打印（DR-08） */
export function printShipRecordApi(recordId: string) {
  return request.get<any>(`/drug/print/${recordId}`)
}

/** 仓库列表（DR-09） */
export function warehouseListApi() {
  return request.get<any>('/drug/warehouse')
}

/** 新增仓库（DR-09） */
export function addWarehouseApi(data: {
  name: string
  location?: string
  adminId?: string
  type: number
}) {
  return request.post<any>('/drug/warehouse', data)
}

/** 标记预警已处理 */
export function handleAlertApi(alertId: number) {
  return request.put<any>('/drug/alert/handle', null, { params: { alertId } })
}

/** 删除预警记录 */
export function deleteAlertApi(alertId: number) {
  return request.delete<any>(`/drug/alert/${alertId}`)
}

/** 调整库存（正数入库，负数出库） */
export function adjustStockApi(data: {
  drugId: string
  quantity: number
  remark?: string
  warehouseId?: string
  batchNo?: string
  productionDate?: string
  expiryDate?: string
  minStock?: number
  maxStock?: number
}) {
  return request.put<any>('/drug/stock/adjust', data)
}

/** 销毁过期药品 */
export function destroyExpiredApi(drugId: string) {
  return request.put<any>('/drug/stock/destroy-expired', { drugId })
}

/** 库存转移 */
export function transferStockApi(data: {
  drugId: string
  fromWarehouseId: string
  toWarehouseId: string
  quantity: number
}) {
  return request.put<any>('/drug/stock/transfer', data)
}
