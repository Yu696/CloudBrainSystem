import request from '@/utils/request'

/** 上传影像（IM-01） */
export function uploadImageApi(formData: FormData) {
  return request.post<any>('/image/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 批量上传影像（IM-02） */
export function batchUploadImageApi(formData: FormData) {
  return request.post<any>('/image/batch-upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 影像列表分页查询（IM-03） */
export function imageListApi(params: {
  patientId?: string
  examinationId?: string
  modality?: string
  myExams?: boolean
  page?: number
  pageSize?: number
}) {
  return request.get<any>('/image/list', { params })
}

/** 影像详情查询（IM-03） */
export function imageInfoApi(imageId: string) {
  return request.get<any>('/image/info', { params: { imageId } })
}

/** 获取影像预览 URL（IM-04） */
export function imagePreviewUrl(imageId: string) {
  return `/api/image/preview?imageId=${imageId}`
}

/** 删除影像（IM-05） */
export function deleteImageApi(imageId: string) {
  return request.delete<any>('/image/delete', { params: { imageId } })
}

/** 影像标注（IM-06） */
export function annotateImageApi(data: {
  imageId: string
  annotationType: string
  coordinates: Record<string, any>
  label?: string
  measurement?: string
  description?: string
}) {
  return request.post<any>('/image/annotate', data)
}

/** 格式转换（IM-09） */
export function convertImageApi(imageId: string, targetFormat: string) {
  return request.post<any>('/image/convert', { imageId, targetFormat })
}

/** 获取存储配置 */
export function getStorageConfigApi() {
  return request.get<any>('/image/storage-config')
}
