<template>
  <div class="image-upload-page">
    <div class="page-title">
      <el-button text @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      影像上传
    </div>

    <!-- 检查单上下文卡片（从检查单入口进入时显示） -->
    <div v-if="examOrder" class="exam-context-card">
      <div class="context-header">
        <el-icon class="context-icon"><Notebook /></el-icon>
        <span>关联检查单</span>
      </div>
      <div class="context-body">
        <div class="context-item">
          <span class="context-label">检查项目：</span>
          <el-tag size="small" type="warning">{{ examOrder.examName }}</el-tag>
        </div>
        <div class="context-item">
          <span class="context-label">患者 ID：</span>
          <span class="context-value">{{ examOrder.patientId }}</span>
        </div>
        <div class="context-item">
          <span class="context-label">检查单号：</span>
          <span class="context-value">{{ examOrder.orderId }}</span>
        </div>
        <div class="context-item">
          <span class="context-label">状态：</span>
          <el-tag size="small" :type="examStatusTag">{{ examStatusText }}</el-tag>
        </div>
      </div>
      <el-alert
        v-if="examOrder.status !== 1"
        :title="examOrder.status === 2 ? '该检查单已有影像上传，如确需补充请联系管理员' : '该检查单尚未缴费，请先完成缴费后再上传影像'"
        :type="examOrder.status === 2 ? 'info' : 'error'"
        :closable="false"
        show-icon
        style="margin-top: 12px;"
      />
    </div>

    <div class="upload-content">
      <!-- 拖拽上传区域 -->
      <div class="cb-card">
        <div class="cb-card-header">
          <el-icon class="header-icon"><Upload /></el-icon>
          <span>选择影像文件</span>
        </div>
        <div class="cb-card-body">
          <el-upload
            ref="uploadRef"
            v-model:file-list="fileList"
            drag
            multiple
            :auto-upload="false"
            :accept="acceptTypes"
            :before-upload="beforeUpload"
            :on-exceed="handleExceed"
            :limit="10"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将文件拖拽到此处，或 <em>点击选择文件</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 DICOM（.dcm）、JPG（.jpg/.jpeg）、PNG（.png）格式，单文件最大 50MB
              </div>
            </template>
          </el-upload>
        </div>
      </div>

      <!-- 元信息表单 -->
      <div class="cb-card">
        <div class="cb-card-header">
          <el-icon class="header-icon"><Document /></el-icon>
          <span>影像元信息</span>
        </div>
        <div class="cb-card-body">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
            <el-form-item label="患者 ID" prop="patientId">
              <el-input v-model="form.patientId" :disabled="!!orderIdFromRoute" placeholder="请输入患者 ID" />
            </el-form-item>
            <el-form-item label="检查 ID" prop="examinationId">
              <el-input v-model="form.examinationId" :disabled="!!orderIdFromRoute" placeholder="请输入检查 ID（可选）" />
            </el-form-item>
            <el-form-item label="影像模态" prop="modality">
              <el-select v-model="form.modality" placeholder="请选择影像模态" clearable style="width: 100%">
                <el-option label="CT" value="CT" />
                <el-option label="MRI" value="MRI" />
                <el-option label="X光" value="X光" />
                <el-option label="超声" value="超声" />
              </el-select>
            </el-form-item>
            <el-form-item label="身体部位" prop="bodyPart">
              <el-input v-model="form.bodyPart" placeholder="请输入身体部位（可选）" />
            </el-form-item>
          </el-form>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="upload-actions">
        <el-button
          type="primary"
          :loading="uploading"
          :disabled="fileList.length !== 1 || uploadDisabled"
          @click="handleSingleUpload"
        >
          <el-icon><Upload /></el-icon>
          上传单文件
        </el-button>
        <el-button
          type="success"
          :loading="batchUploading"
          :disabled="fileList.length < 2 || uploadDisabled"
          @click="handleBatchUpload"
        >
          <el-icon><Upload /></el-icon>
          批量上传（{{ fileList.length }} 个文件）
        </el-button>
        <el-button
          :disabled="fileList.length === 0"
          @click="handleClear"
        >
          清空列表
        </el-button>
      </div>

      <!-- 上传结果 -->
      <div v-if="uploadResults.length > 0" class="cb-card">
        <div class="cb-card-header">
          <el-icon class="header-icon"><SuccessFilled /></el-icon>
          <span>上传结果（成功 {{ successCount }}/{{ uploadResults.length }}）</span>
        </div>
        <div class="cb-card-body">
          <el-table :data="uploadResults" border stripe style="width: 100%">
            <el-table-column label="缩略图" width="100" align="center">
              <template #default="{ row }">
                <el-image
                  v-if="row.success && row.imageId"
                  :src="imagePreviewUrl(row.imageId)"
                  :preview-src-list="[imagePreviewUrl(row.imageId)]"
                  fit="cover"
                  style="width: 72px; height: 72px; border-radius: 4px;"
                >
                  <template #error>
                    <div class="thumb-placeholder">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                  <template #placeholder>
                    <div class="thumb-placeholder">
                      <el-icon><Loading /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div v-else class="thumb-placeholder">
                  <el-icon><WarningFilled /></el-icon>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="imageName" label="文件名" min-width="160" show-overflow-tooltip />
            <el-table-column prop="imageId" label="影像 ID" min-width="140" show-overflow-tooltip />
            <el-table-column prop="imageType" label="类型" width="70" />
            <el-table-column label="文件大小" width="100">
              <template #default="{ row }">
                {{ formatFileSize(row.fileSize) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="70" align="center">
              <template #default="{ row }">
                <el-tag :type="row.success ? 'success' : 'danger'" size="small" effect="plain">
                  {{ row.success ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="message" label="备注" min-width="120" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Upload,
  Document,
  SuccessFilled,
  UploadFilled,
  Notebook,
  Picture,
  Loading,
  WarningFilled
} from '@element-plus/icons-vue'
import type { UploadInstance, UploadFile } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { uploadImageApi, batchUploadImageApi, imagePreviewUrl } from '@/api/image'

const router = useRouter()
const route = useRoute()

/* ==================== 检查单上下文 ==================== */

const orderIdFromRoute = computed(() => route.params.orderId as string | undefined)
const examOrder = ref<any>(null)

const examStatusMap: Record<number, string> = {
  0: '已开单', 1: '已缴费', 2: '检查中', 3: '已完成', 4: '已取消'
}
const examStatusTagMap: Record<number, '' | 'warning' | 'success' | 'info' | 'danger'> = {
  0: 'warning', 1: '', 2: 'info', 3: 'success', 4: 'info'
}
const examStatusText = computed(() => examStatusMap[examOrder.value?.status] || '')
const examStatusTag = computed(() => examStatusTagMap[examOrder.value?.status] || 'info')
const uploadDisabled = computed(() => examOrder.value != null && examOrder.value.status !== 1)

onMounted(() => {
  if (orderIdFromRoute.value) {
    const patientId = (route.query.patientId as string) || ''
    const examName = (route.query.examName as string) || ''
    const status = parseInt((route.query.status as string) || '0')

    form.patientId = patientId
    form.examinationId = orderIdFromRoute.value

    examOrder.value = {
      orderId: orderIdFromRoute.value,
      patientId,
      examName,
      status
    }
  }
})

/* ==================== 文件上传 ==================== */

const uploadRef = ref<UploadInstance>()
const fileList = ref<UploadFile[]>([])
const acceptTypes = '.dcm,.jpg,.jpeg,.png'
const maxSize = 50 * 1024 * 1024 // 50MB

/** 文件添加到列表前的校验（自动上传关闭时也会触发） */
function beforeUpload(file: File): boolean {
  if (file.size > maxSize) {
    ElMessage.error(`文件 "${file.name}" 超过 50MB 限制`)
    return false
  }
  const ext = extractExtension(file.name)
  if (!ext || !['dcm', 'jpg', 'jpeg', 'png'].includes(ext)) {
    ElMessage.error(`文件 "${file.name}" 类型不支持，仅支持 .dcm/.jpg/.jpeg/.png`)
    return false
  }
  return true
}

function handleExceed() {
  ElMessage.warning('最多同时选择 10 个文件')
}

function handleClear() {
  fileList.value = []
  uploadRef.value?.clearFiles()
}

/* ==================== 元信息表单 ==================== */

const formRef = ref<FormInstance>()
const form = reactive({
  patientId: '',
  examinationId: '',
  modality: '',
  bodyPart: ''
})
const rules = {
  patientId: [{ required: true, message: '请输入患者 ID', trigger: 'blur' }]
}

/** 将元数据追加到 FormData */
function appendMetadata(fd: FormData) {
  fd.append('patientId', form.patientId)
  if (form.examinationId) fd.append('examinationId', form.examinationId)
  if (form.modality) fd.append('modality', form.modality)
  if (form.bodyPart) fd.append('bodyPart', form.bodyPart)
}

/** 校验表单，返回是否通过 */
async function validateForm(): Promise<boolean> {
  if (!formRef.value) return false
  try {
    await formRef.value.validate()
    return true
  } catch {
    ElMessage.warning('请完善影像元信息（患者 ID 为必填项）')
    return false
  }
}

/* ==================== 上传操作 ==================== */

const uploading = ref(false)
const batchUploading = ref(false)

interface UploadResultItem {
  imageName: string
  imageId?: string
  imageType?: string
  fileSize?: number
  success: boolean
  message: string
}

const uploadResults = ref<UploadResultItem[]>([])
const successCount = computed(() => uploadResults.value.filter((r) => r.success).length)

/** 单文件上传 */
async function handleSingleUpload() {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先选择要上传的文件')
    return
  }
  if (fileList.value.length > 1) {
    ElMessage.warning('单文件上传仅支持一个文件，请使用批量上传')
    return
  }

  const formValid = await validateForm()
  if (!formValid) return

  const uploadFile = fileList.value[0]
  const file = uploadFile.raw
  if (!file) {
    ElMessage.error('文件对象无效，请重新选择')
    return
  }

  // 二次校验
  if (!validateFileOrWarn(file)) return

  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    appendMetadata(fd)

    const res = await uploadImageApi(fd)
    const data = res.data

    uploadResults.value.unshift({
      imageName: data.imageName || file.name,
      imageId: data.imageId,
      imageType: data.imageType,
      fileSize: data.fileSize,
      success: true,
      message: '上传成功'
    })
    ElMessage.success(`文件 "${file.name}" 上传成功`)

    // 上传成功后更新检查单状态为"检查中"
    if (examOrder.value && examOrder.value.status === 1) {
      examOrder.value.status = 2
    }

    // 从列表中移除已上传的文件
    removeFileFromList(uploadFile.uid)
  } catch (err: any) {
    uploadResults.value.unshift({
      imageName: file.name,
      success: false,
      message: err?.message || '上传失败'
    })
    ElMessage.error(`文件 "${file.name}" 上传失败: ${err?.message || '未知错误'}`)
  } finally {
    uploading.value = false
  }
}

/** 批量上传 */
async function handleBatchUpload() {
  if (fileList.value.length < 2) {
    ElMessage.warning('批量上传至少需要 2 个文件')
    return
  }

  const formValid = await validateForm()
  if (!formValid) return

  // 逐一校验所有文件
  for (const f of fileList.value) {
    const file = f.raw
    if (!file) {
      ElMessage.error('存在无效文件对象，请重新选择')
      return
    }
    if (!validateFileOrWarn(file)) return
  }

  batchUploading.value = true
  try {
    const fd = new FormData()
    fileList.value.forEach((f) => {
      if (f.raw) fd.append('files', f.raw)
    })
    appendMetadata(fd)

    const res = await batchUploadImageApi(fd)
    const resultData = res.data

    // 兼容后端返回单对象或数组
    const resultArray = Array.isArray(resultData) ? resultData : [resultData]

    resultArray.forEach((item: any) => {
      uploadResults.value.unshift({
        imageName: item.imageName || '未知',
        imageId: item.imageId,
        imageType: item.imageType,
        fileSize: item.fileSize,
        success: true,
        message: '上传成功'
      })
    })
    ElMessage.success(`批量上传成功，共 ${resultArray.length} 个文件`)

    // 上传成功后更新检查单状态为"检查中"
    if (examOrder.value && examOrder.value.status === 1) {
      examOrder.value.status = 2
    }

    // 清空文件列表
    fileList.value = []
    uploadRef.value?.clearFiles()
  } catch (err: any) {
    // 整个批量请求失败，逐个记录
    fileList.value.forEach((f) => {
      uploadResults.value.unshift({
        imageName: f.name || '未知文件',
        success: false,
        message: err?.message || '批量上传失败'
      })
    })
    ElMessage.error(`批量上传失败: ${err?.message || '未知错误'}`)
  } finally {
    batchUploading.value = false
  }
}

/* ==================== 工具函数 ==================== */

function extractExtension(name: string): string {
  return name.toLowerCase().split('.').pop() || ''
}

function validateFileOrWarn(file: File): boolean {
  if (file.size > maxSize) {
    ElMessage.error(`文件 "${file.name}" 超过 50MB 限制`)
    return false
  }
  const ext = extractExtension(file.name)
  if (!ext || !['dcm', 'jpg', 'jpeg', 'png'].includes(ext)) {
    ElMessage.error(`文件 "${file.name}" 类型不支持，仅支持 .dcm/.jpg/.jpeg/.png`)
    return false
  }
  return true
}

function removeFileFromList(uid: number) {
  const idx = fileList.value.findIndex((f) => f.uid === uid)
  if (idx !== -1) {
    fileList.value.splice(idx, 1)
  }
}

function formatFileSize(bytes?: number): string {
  if (bytes === undefined || bytes === null) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}
</script>

<style scoped>
.image-upload-page {
  max-width: 960px;
}

/* ===== 检查单上下文卡片 ===== */
.exam-context-card {
  background: var(--cb-primary-lighter, #f0f7ff);
  border: 1px solid var(--cb-primary-light, #a0d2f0);
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 20px;
}

.context-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 12px;
  color: var(--cb-primary);
}

.context-icon {
  font-size: 18px;
}

.context-body {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 32px;
}

.context-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.context-label {
  color: var(--cb-text-secondary);
}

.context-value {
  color: var(--cb-text-primary);
  font-weight: 500;
}

/* ===== 缩略图 ===== */
.thumb-placeholder {
  width: 72px;
  height: 72px;
  border-radius: 4px;
  background: var(--cb-background, #f5f5f5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--cb-text-placeholder, #c0c4cc);
  margin: 0 auto;
}

.header-icon {
  color: var(--cb-primary);
  font-size: 18px;
}

.upload-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.upload-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.upload-actions .el-button {
  min-width: 140px;
}

/* 覆盖 el-upload 拖拽区域的手型光标 */
.el-upload-dragger {
  cursor: pointer;
}
</style>
