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
        当前工具：<strong>{{ currentToolLabel }}</strong> — 点击影像添加标注
      </span>
      <span class="ann-count">标注 {{ annotations.length }} 个</span>
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
                @click.self="onOverlayClick"
              >
                <div
                  v-for="ann in annotations"
                  :key="ann.id"
                  class="annotation-item"
                  :class="[
                    `annotation-${ann.type}`,
                    { 'annotation-selected': selectedAnnotation?.id === ann.id }
                  ]"
                  :style="getAnnotationStyle(ann)"
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

      <!-- 标注信息面板 -->
      <div class="panel-area">
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
            <p>在影像上点击创建新标注</p>
            <p class="hint-sub">或点击已有标注进行编辑</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Close, Picture, WarningFilled } from '@element-plus/icons-vue'
import { imagePreviewUrl, imageInfoApi, annotateImageApi } from '@/api/image'

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

/* ==================== 标注工具状态 ==================== */

const currentTool = ref<'rectangle' | 'circle' | 'text'>('rectangle')
const annotations = ref<Annotation[]>([])
const selectedAnnotation = ref<Annotation | null>(null)
const saving = ref(false)

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

    // 如果后端返回了已有标注数据，加载到本地
    const existAnnotations = res.data?.annotations || res.data?.imageAnnotations
    if (Array.isArray(existAnnotations) && existAnnotations.length > 0) {
      annotations.value = existAnnotations.map((a: any, i: number) => ({
        id: a.id || `remote_${i}`,
        type: a.annotationType || a.type || 'rectangle',
        x: a.coordinates?.x || a.x || 0,
        y: a.coordinates?.y || a.y || 0,
        width: a.coordinates?.width || a.width || 80,
        height: a.coordinates?.height || a.height || 60,
        label: a.label || '',
        measurement: a.measurement || '',
        description: a.description || '',
        saved: true
      }))
    }
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

/* ==================== 标注交互 ==================== */

function onOverlayClick(event: MouseEvent) {
  if (!imageContainerRef.value) return

  const rect = imageContainerRef.value.getBoundingClientRect()
  const x = event.clientX - rect.left
  const y = event.clientY - rect.top

  let annWidth: number
  let annHeight: number
  switch (currentTool.value) {
    case 'rectangle':
      annWidth = 80
      annHeight = 60
      break
    case 'circle':
      annWidth = 60
      annHeight = 60
      break
    case 'text':
      annWidth = 100
      annHeight = 36
      break
  }

  const newAnn: Annotation = {
    id: `ann_${Date.now()}_${annotations.value.length}`,
    type: currentTool.value,
    x: Math.max(0, x - annWidth / 2),
    y: Math.max(0, y - annHeight / 2),
    width: annWidth,
    height: annHeight,
    label: '',
    measurement: '',
    description: '',
    saved: false
  }

  annotations.value.push(newAnn)
  selectAnnotation(newAnn)
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

    await annotateImageApi({
      imageId,
      annotationType: ann.type,
      coordinates: {
        x: Math.round(ann.x),
        y: Math.round(ann.y),
        width: Math.round(ann.width),
        height: Math.round(ann.height)
      },
      label: ann.label,
      measurement: ann.measurement,
      description: ann.description
    })

    ann.saved = true
    ElMessage.success('标注保存成功')
  } catch (err: any) {
    ElMessage.error('标注保存失败：' + (err?.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

function deleteSelectedAnnotation() {
  if (!selectedAnnotation.value) return

  const idx = annotations.value.findIndex((a) => a.id === selectedAnnotation.value!.id)
  if (idx !== -1) {
    annotations.value.splice(idx, 1)
  }
  clearSelection()
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
}

/* ===== 影像容器 ===== */

.image-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  width: 100%;
}
.image-container {
  position: relative;
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
}
.viewer-image {
  display: block;
  max-width: 100%;
  max-height: 75vh;
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

/* ===== 标注覆盖层 ===== */

.annotation-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  cursor: crosshair;
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
  cursor: pointer;
  transition: box-shadow 0.2s ease, border-color 0.2s ease;
  box-sizing: border-box;
  font-size: var(--cb-font-xs, 12px);
  overflow: hidden;
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
</style>
