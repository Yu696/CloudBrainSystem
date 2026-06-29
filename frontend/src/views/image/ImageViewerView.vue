<template>
  <div class="viewer-page">
    <!-- 页面标题 -->
    <div class="page-title">
      <el-button text @click="router.back()" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <span>影像预览</span>
      <el-tag v-if="imageInfo" type="info" effect="plain" class="image-tag">
        {{ imageInfo.imageName || imageInfo.imageId || '' }}
      </el-tag>
    </div>

    <!-- 标注工具栏 -->
    <div class="toolbar">
      <el-button-group>
        <el-button
          :type="currentTool === 'rectangle' ? 'primary' : 'default'"
          @click="currentTool = 'rectangle'"
        >
          矩形标注
        </el-button>
        <el-button
          :type="currentTool === 'circle' ? 'primary' : 'default'"
          @click="currentTool = 'circle'"
        >
          圆形标注
        </el-button>
        <el-button
          :type="currentTool === 'text' ? 'primary' : 'default'"
          @click="currentTool = 'text'"
        >
          文字标注
        </el-button>
      </el-button-group>
      <span class="tool-hint">
        当前工具：<strong>{{ currentToolLabel }}</strong> — {{ currentTool === 'text' ? '点击影像添加标注' : '按住拖动画框' }}
      </span>
      <span class="ann-count">标注 {{ annotations.length }} 个</span>
      <el-button
        size="small"
        type="warning"
        :loading="aiLoading"
        @click="handleAiAnalysis"
        v-if="imageInfo"
      >
        <el-icon><DataAnalysis /></el-icon>
        AI 分析
      </el-button>
    </div>

    <!-- 主布局 -->
    <div class="viewer-layout">
      <!-- 影像区域 -->
      <div class="cb-card viewer-card">
        <div class="cb-card-body viewer-body">
          <div v-loading="loading" class="image-wrapper">
            <!-- 未加载时的占位 -->
            <div v-if="!imageLoaded && !imageError && !loading" class="image-placeholder">
              <el-icon :size="48"><Picture /></el-icon>
              <span>正在加载影像...</span>
            </div>

            <!-- 加载失败 -->
            <div v-if="imageError" class="image-placeholder error">
              <el-icon :size="48"><WarningFilled /></el-icon>
              <span>影像加载失败或不存在</span>
              <el-button size="small" type="primary" @click="reloadImage">重新加载</el-button>
            </div>

            <!-- 影像 + 标注层 -->
            <div
              v-show="imageLoaded && !imageError"
              class="image-container"
              ref="imageContainerRef"
            >
              <img
                ref="imageRef"
                :src="imgSrc"
                alt="影像预览"
                class="viewer-image"
                @load="onImageLoad"
                @error="onImageError"
                draggable="false"
              />
              <div
                v-if="imageLoaded"
                class="annotation-overlay"
                @mousedown.self="onOverlayMouseDown"
                @mousemove="onOverlayMouseMove"
                @mouseup="onOverlayMouseUp"
                @mouseleave="onOverlayMouseUp"
              >
                <!-- 拖拽绘制中的预览框 -->
                <div
                  v-if="isDrawing"
                  class="annotation-item draw-preview"
                  :style="drawPreviewStyle"
                />
                <div
                  v-for="ann in annotations"
                  :key="ann.id"
                  class="annotation-item"
                  :class="[
                    `annotation-${ann.type}`,
                    { 'annotation-selected': selectedAnnotation?.id === ann.id }
                  ]"
                  :style="getAnnotationStyle(ann)"
                  @mousedown.stop.prevent="onAnnotationMouseDown($event, ann)"
                  @click.stop="selectAnnotation(ann)"
                >
                  <span class="ann-marker">{{ ann.type === 'rectangle' ? '&#9633;' : ann.type === 'circle' ? '&#9675;' : '&#84;' }}</span>
                  <span v-if="ann.label" class="ann-label">{{ ann.label }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧面板 -->
      <div class="panel-area">
        <!-- 标注组件 -->
        <div v-if="selectedAnnotation" class="cb-card panel-card">
          <div class="cb-card-header">
            <span>标注信息</span>
            <el-button text @click="clearSelection">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
          <div class="cb-card-body">
            <el-form label-position="top" size="small">
              <el-form-item label="类型">
                <el-tag size="small" :type="typeTagType" effect="plain">
                  {{ typeLabel }}
                </el-tag>
              </el-form-item>
              <el-form-item label="位置">
                <span class="coord-text">
                  ({{ Math.round(selectedAnnotation.x) }}, {{ Math.round(selectedAnnotation.y) }})
                </span>
              </el-form-item>
              <el-form-item label="标注标签">
                <el-input v-model="editForm.label" placeholder="如：结节、阴影" />
              </el-form-item>
              <el-form-item label="测量值">
                <el-input v-model="editForm.measurement" placeholder="如：1.2cm&#215;0.8cm" />
              </el-form-item>
              <el-form-item label="描述">
                <el-input
                  v-model="editForm.description"
                  type="textarea"
                  :rows="3"
                  placeholder="标注描述..."
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
              <el-divider style="margin: 8px 0" />
              <el-button
                type="primary"
                :loading="saving"
                style="width: 100%"
                @click="saveAnnotation"
              >
                保存标注
              </el-button>
              <el-button
                style="width: 100%; margin-top: 8px"
                @click="deleteSelectedAnnotation"
              >
                删除标注
              </el-button>
            </el-form>
          </div>
        </div>

        <!-- 未选中标注时的提示 -->
        <div v-else class="cb-card hint-card">
          <div class="cb-card-body hint-body">
            <div class="hint-icon">
              <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="#c0c4cc" stroke-width="1.5">
                <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                <circle cx="8.5" cy="7" r="4" />
                <line x1="20" y1="8" x2="20" y2="14" />
                <line x1="17" y1="11" x2="23" y2="11" />
              </svg>
            </div>
            <p>按住拖动画框创建标注</p>
            <p class="hint-sub">文字工具点击创建，拖拽已有标注可移动</p>
          </div>
        </div>

        <!-- 影像对比按钮 -->
        <el-button
          type="primary"
          style="width: 100%"
          @click="goCompare"
        >
          <el-icon><Rank /></el-icon>
          影像对比
        </el-button>

        <!-- 检查结果表单 -->
        <div v-if="imageInfo" class="cb-card exam-result-card">
          <div class="cb-card-header">
            <span>检查结果</span>
            <el-tag v-if="examResultExists" size="small" type="success" effect="plain">已填写</el-tag>
          </div>
          <div class="cb-card-body">
            <el-form label-position="top" size="small">
              <el-form-item label="检查所见">
                <el-input
                  v-model="examForm.resultData"
                  type="textarea"
                  :rows="3"
                  placeholder="描述检查所见..."
                  maxlength="1000"
                  show-word-limit
                />
              </el-form-item>
              <el-form-item label="参考范围">
                <el-input
                  v-model="examForm.referenceRange"
                  placeholder="如：3.5-5.5 mmol/L"
                />
              </el-form-item>
              <el-form-item label="是否异常">
                <el-switch v-model="examForm.isAbnormal" />
              </el-form-item>
              <el-form-item label="医生意见">
                <el-input
                  v-model="examForm.doctorOpinion"
                  type="textarea"
                  :rows="2"
                  placeholder="诊断意见..."
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
              <el-button
                type="primary"
                :loading="examResultSaving"
                style="width: 100%"
                @click="handleSaveExamResult"
              >
                {{ examResultExists ? '更新结果' : '保存结果' }}
              </el-button>
            </el-form>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- AI 分析结果对话框 -->
  <el-dialog v-model="aiDialogVisible" title="AI 影像分析" width="620px" top="8vh" destroy-on-close>
    <div v-loading="aiLoading" class="ai-dialog-body">
      <template v-if="aiResult">
        <div class="ai-section">
          <h4 class="ai-section-title">诊断总结</h4>
          <p class="ai-summary-text">{{ aiResult.findings?.summaryDetail || '暂无总结' }}</p>
        </div>
        <div class="ai-section" v-if="aiResult.confidenceScore !== null && aiResult.confidenceScore !== undefined">
          <h4 class="ai-section-title">置信度</h4>
          <el-progress :percentage="Math.round(Number(aiResult.confidenceScore) * 100)" :format="confidenceFormat" />
        </div>
        <div class="ai-section" v-if="aiResult.findings?.abnormalRegions?.length">
          <h4 class="ai-section-title">异常区域（{{ aiResult.findings.abnormalRegions.length }}）</h4>
          <div v-for="(region, i) in aiResult.findings.abnormalRegions" :key="i" class="ai-region-card">
            <div class="ai-region-header">区域 {{ Number(i) + 1 }}</div>
            <div class="ai-region-body">
              <p><span class="ai-label">位置：</span>{{ region.location || '-' }}</p>
              <p><span class="ai-label">大小：</span>{{ region.size || '-' }}</p>
              <p><span class="ai-label">描述：</span>{{ region.description || '-' }}</p>
              <p v-if="region.confidence !== null && region.confidence !== undefined">
                <span class="ai-label">置信度：</span>{{ (Number(region.confidence) * 100).toFixed(1) }}%
              </p>
            </div>
          </div>
        </div>
        <div class="ai-meta">
          <el-tag size="small" effect="plain">{{ aiResult.aiModel || '-' }}</el-tag>
          <el-tag size="small" :type="aiResult.status === 1 ? 'success' : aiResult.status === 3 ? 'danger' : 'warning'">
            {{ aiResult.status === 1 ? '分析完成' : aiResult.status === 3 ? '服务不可用' : '处理中' }}
          </el-tag>
        </div>
      </template>
      <el-empty v-else-if="!aiLoading" description="暂无分析结果" />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Close, Picture, WarningFilled, Rank, DataAnalysis } from '@element-plus/icons-vue'
import { imagePreviewUrl, imageInfoApi, annotateImageApi, listAnnotationsApi, deleteAnnotationApi } from '@/api/image'
import { imageDiagnosisApi } from '@/api/ai'
import { getExaminationResultApi, saveExaminationResultApi } from '@/api/medical'
import { useUserStore } from '@/stores/user'

/* ==================== 类型定义 ==================== */

interface Annotation {
  id: string
  type: 'rectangle' | 'circle' | 'text'
  /** 左上角 X 坐标（相对于影像渲染尺寸） */
  x: number
  /** 左上角 Y 坐标（相对于影像渲染尺寸） */
  y: number
  width: number
  height: number
  label: string
  measurement: string
  description: string
  /** 是否已保存到后端 */
  saved: boolean
}

/* ==================== 路由 & 引用 ==================== */

const router = useRouter()
const route = useRoute()
const imageId = route.params.imageId as string

const imageRef = ref<HTMLImageElement | null>(null)
const imageContainerRef = ref<HTMLDivElement | null>(null)

/* ==================== 影像状态 ==================== */

const loading = ref(false)
const imageInfo = ref<any>(null)
const imageLoaded = ref(false)
const imageError = ref(false)

const imgSrc = computed(() => imagePreviewUrl(imageId))

/* ==================== AI 分析状态 ==================== */

const aiLoading = ref(false)
const aiDialogVisible = ref(false)
const aiResult = ref<any>(null)

/* ==================== 检查结果状态 ==================== */

const examResultSaving = ref(false)
const examResultExists = ref(false)

const examForm = reactive({
  resultData: '',
  referenceRange: '',
  isAbnormal: false,
  doctorOpinion: '',
  aiAnalysis: ''
})

function confidenceFormat(percentage: number) {
  return percentage + '%'
}

async function handleAiAnalysis() {
  if (!imageInfo.value?.patientId) {
    ElMessage.warning('影像缺少患者信息，无法进行 AI 分析')
    return
  }
  const userStore = useUserStore()
  if (!userStore.userInfo?.userId) {
    ElMessage.warning('无法获取医生信息')
    return
  }

  aiLoading.value = true
  aiResult.value = null
  try {
    const res = await imageDiagnosisApi({
      imageId,
      diagnosisType: 1,
      patientId: imageInfo.value.patientId,
      doctorId: userStore.userInfo.userId
    })
    aiResult.value = res.data
    aiDialogVisible.value = true

    // 将 AI 诊断总结自动保存到检查结果的 aiAnalysis 字段
    const summary = aiResult.value?.findings?.summaryDetail
    if (summary) {
      try {
        await saveExaminationResultApi({
          imageId,
          orderId: imageInfo.value?.examinationId || undefined,
          resultData: examForm.resultData,
          referenceRange: examForm.referenceRange,
          isAbnormal: examForm.isAbnormal ? 1 : 0,
          doctorOpinion: examForm.doctorOpinion,
          aiAnalysis: summary
        })
        examResultExists.value = true
      } catch {
        // 静默保存失败不影响主流程
      }
    }
  } catch (err: any) {
    ElMessage.error('AI 分析失败：' + (err?.message || '未知错误'))
  } finally {
    aiLoading.value = false
  }
}

/* ==================== 检查结果表单 ==================== */

async function loadExamResult() {
  const orderId = imageInfo.value?.examinationId
  if (!orderId) return
  try {
    const res = await getExaminationResultApi(orderId)
    if (res.data) {
      examResultExists.value = true
      examForm.resultData = res.data.resultData || ''
      examForm.referenceRange = res.data.referenceRange || ''
      examForm.isAbnormal = res.data.isAbnormal === 1
      examForm.doctorOpinion = res.data.doctorOpinion || ''
      examForm.aiAnalysis = res.data.aiAnalysis || ''
    }
  } catch {
    // 结果不存在时不清空表单，保留已填内容
  }
}

async function handleSaveExamResult() {
  examResultSaving.value = true
  try {
    await saveExaminationResultApi({
      orderId: imageInfo.value?.examinationId || undefined,
      imageId,
      resultData: examForm.resultData,
      referenceRange: examForm.referenceRange,
      isAbnormal: examForm.isAbnormal ? 1 : 0,
      doctorOpinion: examForm.doctorOpinion,
      aiAnalysis: examForm.aiAnalysis
    })
    examResultExists.value = true
    ElMessage.success('检查结果保存成功')
  } catch (err: any) {
    ElMessage.error('保存失败：' + (err?.message || '未知错误'))
  } finally {
    examResultSaving.value = false
  }
}

/* ==================== 标注工具状态 ==================== */

const currentTool = ref<'rectangle' | 'circle' | 'text'>('rectangle')
const annotations = ref<Annotation[]>([])
const selectedAnnotation = ref<Annotation | null>(null)
const saving = ref(false)

/* ==================== 拖拽绘制状态 ==================== */

const isDrawing = ref(false)
const drawStartX = ref(0)
const drawStartY = ref(0)
const drawCurrentX = ref(0)
const drawCurrentY = ref(0)

const drawPreviewStyle = computed(() => {
  const x = Math.min(drawStartX.value, drawCurrentX.value)
  const y = Math.min(drawStartY.value, drawCurrentY.value)
  const w = Math.abs(drawCurrentX.value - drawStartX.value)
  const h = Math.abs(drawCurrentY.value - drawStartY.value)
  return {
    left: x + 'px',
    top: y + 'px',
    width: Math.max(w, 2) + 'px',
    height: Math.max(h, 2) + 'px'
  }
})

/* ==================== 拖拽移动状态 ==================== */

const isDragging = ref(false)
const draggingAnnId = ref<string | null>(null)
const dragOffsetX = ref(0)
const dragOffsetY = ref(0)

const editForm = reactive({
  label: '',
  measurement: '',
  description: ''
})

const currentToolLabel = computed(() => {
  const m: Record<string, string> = { rectangle: '矩形', circle: '圆形', text: '文字' }
  return m[currentTool.value] || '矩形'
})

const typeLabel = computed(() => {
  if (!selectedAnnotation.value) return ''
  const m: Record<string, string> = { rectangle: '矩形标注', circle: '圆形标注', text: '文字标注' }
  return m[selectedAnnotation.value.type] || ''
})

const typeTagType = computed(() => {
  if (!selectedAnnotation.value) return 'info'
  const m: Record<string, string> = { rectangle: 'primary', circle: 'success', text: 'warning' }
  return m[selectedAnnotation.value.type] || 'info'
})

/* ==================== 生命周期 ==================== */

onMounted(async () => {
  if (!imageId) {
    ElMessage.error('缺少影像 ID')
    return
  }

  loading.value = true
  try {
    const res = await imageInfoApi(imageId)
    imageInfo.value = res.data

    // 加载已保存的标注列表
    try {
      const annRes = await listAnnotationsApi(imageId)
      const list = annRes.data || []
      if (Array.isArray(list) && list.length > 0) {
        annotations.value = list.map((a: any, i: number) => ({
          id: a.annotationId || a.id || `remote_${i}`,
          type: a.annotationType || a.type || 'rectangle',
          x: a.coordinates ? (typeof a.coordinates === 'string' ? JSON.parse(a.coordinates).x : a.coordinates.x) : a.x || 0,
          y: a.coordinates ? (typeof a.coordinates === 'string' ? JSON.parse(a.coordinates).y : a.coordinates.y) : a.y || 0,
          width: a.coordinates ? (typeof a.coordinates === 'string' ? JSON.parse(a.coordinates).width : a.coordinates.width) : a.width || 80,
          height: a.coordinates ? (typeof a.coordinates === 'string' ? JSON.parse(a.coordinates).height : a.coordinates.height) : a.height || 60,
          label: a.label || '',
          measurement: a.measurement || '',
          description: a.description || '',
          saved: true
        }))
      }
    } catch {
      // 无标注或加载失败不影响主流程
    }
    // 加载已有检查结果
    loadExamResult()
  } catch {
    ElMessage.error('获取影像信息失败')
  } finally {
    loading.value = false
  }
})

/* ==================== 影像加载 ==================== */

function onImageLoad() {
  imageLoaded.value = true
  imageError.value = false
}

function onImageError() {
  imageLoaded.value = false
  imageError.value = true
}

function reloadImage() {
  imageError.value = false
  imageLoaded.value = false
  // 强制重新加载图片
  const img = imageRef.value
  if (img) {
    img.src = imgSrc.value
  }
}

function goCompare() {
  router.push({
    path: '/image/compare',
    query: { left: imageId }
  })
}

/* ==================== 标注交互 ==================== */

/** 根据鼠标事件获取在 imageContainer 内的坐标 */
function getContainerXY(event: MouseEvent): { x: number; y: number } {
  if (!imageContainerRef.value) return { x: 0, y: 0 }
  const rect = imageContainerRef.value.getBoundingClientRect()
  return {
    x: event.clientX - rect.left,
    y: event.clientY - rect.top
  }
}

/** 鼠标按下：开始拖拽绘制（矩形/圆形），文字则直接创建 */
function onOverlayMouseDown(event: MouseEvent) {
  if (isDragging.value) return

  const { x, y } = getContainerXY(event)

  if (currentTool.value === 'text') {
    // 文字标注：点击直接创建，不需要拖拽
    const newAnn: Annotation = {
      id: `ann_${Date.now()}_${annotations.value.length}`,
      type: 'text',
      x: Math.max(0, x - 50),
      y: Math.max(0, y - 18),
      width: 100,
      height: 36,
      label: '',
      measurement: '',
      description: '',
      saved: false
    }
    annotations.value.push(newAnn)
    selectAnnotation(newAnn)
    return
  }

  // 矩形/圆形：开始拖拽绘制
  isDrawing.value = true
  drawStartX.value = x
  drawStartY.value = y
  drawCurrentX.value = x
  drawCurrentY.value = y
}

/** 鼠标移动：更新绘制预览 / 拖拽移动已有标注 */
function onOverlayMouseMove(event: MouseEvent) {
  const { x, y } = getContainerXY(event)

  if (isDrawing.value) {
    drawCurrentX.value = x
    drawCurrentY.value = y
    return
  }

  if (isDragging.value && draggingAnnId.value) {
    const ann = annotations.value.find((a) => a.id === draggingAnnId.value)
    if (ann) {
      ann.x = Math.max(0, x - dragOffsetX.value)
      ann.y = Math.max(0, y - dragOffsetY.value)
      ann.saved = false
    }
  }
}

/** 鼠标松开：完成绘制或停止拖拽 */
function onOverlayMouseUp(_event: MouseEvent) {
  if (isDrawing.value) {
    isDrawing.value = false
    const w = Math.abs(drawCurrentX.value - drawStartX.value)
    const h = Math.abs(drawCurrentY.value - drawStartY.value)

    // 拖拽距离太小则忽略（防误触）
    if (w < 4 && h < 4) return

    const newAnn: Annotation = {
      id: `ann_${Date.now()}_${annotations.value.length}`,
      type: currentTool.value,
      x: Math.min(drawStartX.value, drawCurrentX.value),
      y: Math.min(drawStartY.value, drawCurrentY.value),
      width: w,
      height: h,
      label: '',
      measurement: '',
      description: '',
      saved: false
    }
    annotations.value.push(newAnn)
    selectAnnotation(newAnn)
  }

  if (isDragging.value) {
    isDragging.value = false
    draggingAnnId.value = null
  }
}

/** 在已有标注上按下鼠标：开始拖拽移动 */
function onAnnotationMouseDown(event: MouseEvent, ann: Annotation) {
  // 只允许左键拖拽
  if (event.button !== 0) return

  const { x, y } = getContainerXY(event)
  isDragging.value = true
  draggingAnnId.value = ann.id
  dragOffsetX.value = x - ann.x
  dragOffsetY.value = y - ann.y
}

function selectAnnotation(ann: Annotation) {
  selectedAnnotation.value = ann
  editForm.label = ann.label
  editForm.measurement = ann.measurement
  editForm.description = ann.description
}

function clearSelection() {
  selectedAnnotation.value = null
  editForm.label = ''
  editForm.measurement = ''
  editForm.description = ''
}

function getAnnotationStyle(ann: Annotation): Record<string, string> {
  const base: Record<string, string> = {
    left: ann.x + 'px',
    top: ann.y + 'px',
    width: ann.width + 'px',
    height: ann.height + 'px'
  }

  switch (ann.type) {
    case 'rectangle':
      return {
        ...base,
        border: '2px solid var(--cb-primary, #1a7fbf)',
        backgroundColor: 'rgba(26, 127, 191, 0.08)'
      }
    case 'circle':
      return {
        ...base,
        border: '2px solid var(--cb-success, #52c41a)',
        backgroundColor: 'rgba(82, 196, 26, 0.08)',
        borderRadius: '50%'
      }
    case 'text':
      return {
        ...base,
        border: '2px dashed var(--cb-warning, #faad14)',
        backgroundColor: 'rgba(250, 173, 20, 0.08)'
      }
  }
}

/* ==================== 保存 & 删除 ==================== */

async function saveAnnotation() {
  if (!selectedAnnotation.value) return

  saving.value = true
  try {
    const ann = selectedAnnotation.value
    ann.label = editForm.label
    ann.measurement = editForm.measurement
    ann.description = editForm.description

    const res = await annotateImageApi({
      imageId,
      annotationType: ann.type,
      coordinates: JSON.stringify({
        x: Math.round(ann.x),
        y: Math.round(ann.y),
        width: Math.round(ann.width),
        height: Math.round(ann.height)
      }),
      label: ann.label,
      measurement: ann.measurement,
      description: ann.description
    })

    // 用后端返回的 annotationId 替换本地临时 ID
    if (res.data?.annotationId) {
      ann.id = res.data.annotationId
    }
    ann.saved = true
    ElMessage.success('标注保存成功')
  } catch (err: any) {
    ElMessage.error('标注保存失败：' + (err?.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

async function deleteSelectedAnnotation() {
  if (!selectedAnnotation.value) return

  const ann = selectedAnnotation.value
  const idx = annotations.value.findIndex((a) => a.id === ann.id)
  if (idx !== -1) {
    annotations.value.splice(idx, 1)
  }
  clearSelection()

  // 已保存的标注同步删除后端
  if (ann.saved) {
    try {
      await deleteAnnotationApi(ann.id)
    } catch {
      // 后端删除失败不影响本地
    }
  }
  ElMessage.success('标注已删除')
}
</script>

<style scoped>
.viewer-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* ===== 页面标题 ===== */

.page-title {
  font-size: var(--cb-font-xl, 20px);
  font-weight: 600;
  color: var(--cb-text-primary, #1f2f3d);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.page-title::before {
  content: '';
  display: inline-block;
  width: 3px;
  height: 20px;
  background: var(--cb-primary, #1a7fbf);
  border-radius: 2px;
}
.back-btn {
  color: var(--cb-text-placeholder, #c0c4cc);
  padding: 4px;
}
.back-btn:hover {
  color: var(--cb-primary, #1a7fbf);
}
.image-tag {
  font-size: var(--cb-font-sm, 13px);
  border-radius: 20px;
  padding: 0 12px;
  max-width: 280px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== 工具栏 ===== */

.toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  padding: 12px 20px;
  background: var(--cb-white, #fff);
  border-radius: var(--cb-radius-lg, 12px);
  box-shadow: var(--cb-shadow-sm, 0 1px 2px rgba(0, 0, 0, 0.06));
  flex-shrink: 0;
  flex-wrap: wrap;
}
.tool-hint {
  font-size: var(--cb-font-sm, 13px);
  color: var(--cb-text-secondary, #606266);
}
.tool-hint strong {
  color: var(--cb-primary, #1a7fbf);
}
.ann-count {
  margin-left: auto;
  font-size: var(--cb-font-sm, 13px);
  color: var(--cb-text-placeholder, #c0c4cc);
  background: var(--cb-background, #f0f2f5);
  padding: 2px 10px;
  border-radius: 10px;
}

/* ===== 主布局 ===== */

.viewer-layout {
  display: flex;
  gap: 20px;
  flex: 1;
  min-height: 0;
}

/* ===== 影像卡片 ===== */

.viewer-card {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.viewer-body {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  overflow: auto;
  min-height: 0;
}

/* ===== 影像容器 ===== */

.image-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  max-width: 100%;
  max-height: 100%;
}
.image-container {
  position: relative;
  display: inline-block;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}
.viewer-image {
  display: block;
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 4px;
  user-select: none;
  -webkit-user-drag: none;
}

/* ===== 占位 / 错误 ===== */

.image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 60px 24px;
  color: var(--cb-text-placeholder, #c0c4cc);
  font-size: var(--cb-font-base, 14px);
}
.image-placeholder.error {
  color: var(--cb-danger, #f5222d);
}

/* ===== 拖拽绘制预览 ===== */

.draw-preview {
  border: 2px dashed #409eff !important;
  background-color: rgba(64, 158, 255, 0.1) !important;
  pointer-events: none;
  z-index: 99;
}

/* ===== 标注覆盖层 ===== */

.annotation-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  cursor: crosshair;
  user-select: none;
}
.annotation-overlay:empty {
  /* 无标注时依然需要捕获点击事件 */;
}

/* ===== 单个标注 ===== */

.annotation-item {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: grab;
  transition: box-shadow 0.2s ease, border-color 0.2s ease;
  box-sizing: border-box;
  font-size: var(--cb-font-xs, 12px);
  overflow: hidden;
}
.annotation-item:active {
  cursor: grabbing;
}
.annotation-item:hover {
  box-shadow: 0 0 0 3px rgba(26, 127, 191, 0.2);
}
.annotation-selected {
  box-shadow: 0 0 0 3px rgba(26, 127, 191, 0.35) !important;
  z-index: 10;
}

.ann-marker {
  flex-shrink: 0;
  font-weight: 700;
  font-size: 13px;
  line-height: 1;
  opacity: 0.8;
}
.annotation-rectangle .ann-marker {
  color: var(--cb-primary, #1a7fbf);
}
.annotation-circle .ann-marker {
  color: var(--cb-success, #52c41a);
}
.annotation-text .ann-marker {
  color: var(--cb-warning, #faad14);
}

.ann-label {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 80px;
  color: var(--cb-text-primary, #1f2f3d);
  font-weight: 500;
}

/* ===== 标注信息面板 ===== */

.panel-area {
  width: 340px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel-card {
  position: sticky;
  top: 0;
}

.panel-body {
  padding: 16px 20px;
}

.coord-text {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: var(--cb-font-sm, 13px);
  color: var(--cb-text-secondary, #606266);
}

/* ===== 提示卡片 ===== */

.hint-card {
  display: flex;
  align-items: center;
  justify-content: center;
}
.hint-body {
  text-align: center;
  padding: 48px 24px;
  color: var(--cb-text-placeholder, #c0c4cc);
}
.hint-body p {
  margin: 8px 0 0;
  font-size: var(--cb-font-base, 14px);
}
.hint-sub {
  font-size: var(--cb-font-sm, 13px) !important;
}
.hint-icon {
  margin-bottom: 8px;
}

/* ===== 响应式 ===== */

@media (max-width: 900px) {
  .viewer-layout {
    flex-direction: column;
  }
  .panel-area {
    width: 100%;
  }
}

/* ===== 检查结果卡片 ===== */

.exam-result-card {
  margin-top: 16px;
}
.exam-result-card .cb-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* ===== AI 分析对话框 ===== */

.ai-dialog-body {
  min-height: 120px;
}
.ai-section {
  margin-bottom: 20px;
}
.ai-section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--cb-text-primary, #1f2f3d);
  margin: 0 0 10px;
  padding-bottom: 6px;
  border-bottom: 1px solid var(--cb-border, #ebeef5);
}
.ai-summary-text {
  font-size: 14px;
  line-height: 1.7;
  color: var(--cb-text-secondary, #606266);
  margin: 0;
  white-space: pre-wrap;
}
.ai-region-card {
  border: 1px solid var(--cb-border, #ebeef5);
  border-radius: 8px;
  margin-bottom: 10px;
  overflow: hidden;
}
.ai-region-header {
  background: var(--cb-background, #f0f2f5);
  padding: 6px 12px;
  font-size: 13px;
  font-weight: 500;
  color: var(--cb-text-primary, #1f2f3d);
}
.ai-region-body {
  padding: 10px 12px;
}
.ai-region-body p {
  margin: 4px 0;
  font-size: 13px;
  color: var(--cb-text-secondary, #606266);
}
.ai-label {
  color: var(--cb-text-primary, #1f2f3d);
  font-weight: 500;
}
.ai-meta {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--cb-border, #ebeef5);
  display: flex;
  gap: 8px;
}
</style>
