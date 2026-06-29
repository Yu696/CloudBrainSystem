<template>
  <div class="compare-page">
    <!-- 页面标题 -->
    <div class="page-title">
      <el-button text @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <span>影像对比</span>
    </div>

    <!-- 选择器行 -->
    <div class="selector-row">
      <!-- 左侧选择器 -->
      <div class="selector-panel">
        <span class="selector-label">左侧影像</span>
        <div class="selector-input">
          <el-input
            v-model="leftImageId"
            placeholder="请输入 imageId"
            clearable
            @keyup.enter="confirmLeft"
          />
        </div>
      </div>

      <!-- 右侧选择器 -->
      <div class="selector-panel">
        <span class="selector-label">右侧影像</span>
        <div class="selector-input">
          <el-input
            v-model="rightImageId"
            placeholder="请输入 imageId"
            clearable
            @keyup.enter="confirmRight"
          />
        </div>
      </div>
    </div>

    <!-- AI 分析 -->
    <div class="action-row" v-if="leftConfirmedId">
      <el-button type="warning" :loading="aiLoading" @click="handleAiAnalysis" :disabled="!leftImageInfo?.patientId">
        <el-icon><DataAnalysis /></el-icon>
        AI 影像分析
      </el-button>
    </div>

    <!-- 主布局 -->
    <div class="compare-layout">
      <!-- 对比区域 -->
      <div class="compare-body">
        <!-- 左侧影像 -->
        <div class="compare-panel">
          <div v-if="leftConfirmedId" class="panel-header">
            <span class="panel-title">影像 ID：{{ leftConfirmedId }}</span>
            <span v-if="leftImageInfo" class="panel-meta">{{ leftImageInfo.modality }} | {{ leftImageInfo.patientName || leftImageInfo.patientId }}</span>
          </div>
          <div class="panel-content">
            <template v-if="leftConfirmedId">
              <img
                :key="leftConfirmedId"
                :src="imagePreviewUrl(leftConfirmedId)"
                alt="左侧影像"
                class="compare-image"
                @error="onLeftError"
                @load="onLeftLoad"
              />
              <div v-if="leftError" class="image-error-overlay">
                <el-icon :size="36"><WarningFilled /></el-icon>
                <span>影像加载失败</span>
              </div>
            </template>
            <div v-else class="empty-state">
              <el-icon :size="48"><Picture /></el-icon>
              <span>请选择影像</span>
            </div>
          </div>
        </div>

        <!-- 右侧影像 -->
        <div class="compare-panel">
          <div v-if="rightConfirmedId" class="panel-header">
            <span class="panel-title">影像 ID：{{ rightConfirmedId }}</span>
          </div>
          <div class="panel-content">
            <template v-if="rightConfirmedId">
              <img
                :key="rightConfirmedId"
                :src="imagePreviewUrl(rightConfirmedId)"
                alt="右侧影像"
                class="compare-image"
                @error="onRightError"
                @load="onRightLoad"
              />
              <div v-if="rightError" class="image-error-overlay">
                <el-icon :size="36"><WarningFilled /></el-icon>
                <span>影像加载失败</span>
              </div>
            </template>
            <div v-else class="empty-state">
              <el-icon :size="48"><Picture /></el-icon>
              <span>请选择影像</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧历史影像列表 -->
      <div v-if="leftConfirmedId && relatedImages.length > 0" class="history-panel">
        <div class="history-header">
          <span>同患者同类型历史影像</span>
          <el-tag size="small" type="info">{{ relatedImages.length }} 张</el-tag>
        </div>
        <div class="history-list">
          <div
            v-for="img in relatedImages"
            :key="img.imageId"
            class="history-item"
            :class="{ selected: img.imageId === rightConfirmedId }"
            @click="selectRelatedImage(img)"
          >
            <el-image
              :src="imagePreviewUrl(img.imageId)"
              fit="cover"
              class="history-thumb"
              :preview-src-list="[imagePreviewUrl(img.imageId)]"
            >
              <template #error>
                <div class="thumb-placeholder"><el-icon><Picture /></el-icon></div>
              </template>
            </el-image>
            <div class="history-info">
              <span class="history-name">{{ img.imageName || img.imageId?.slice(0, 20) }}</span>
              <span class="history-meta">{{ img.modality }} | {{ img.uploadTime?.slice(0, 10) || '' }}</span>
            </div>
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
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Picture, WarningFilled, DataAnalysis } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { imagePreviewUrl, imageInfoApi, imageListApi } from '@/api/image'
import { imageDiagnosisApi } from '@/api/ai'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()

function goBack() {
  if (leftConfirmedId.value) {
    router.push({ path: `/image/viewer/${leftConfirmedId.value}` })
  } else {
    router.back()
  }
}

const leftImageId = ref('')
const rightImageId = ref('')
const leftConfirmedId = ref('')
const rightConfirmedId = ref('')
const leftError = ref(false)
const rightError = ref(false)
const leftImageInfo = ref<any>(null)
const relatedImages = ref<any[]>([])
const loadingRelated = ref(false)

/* ==================== AI 分析 ==================== */

const aiLoading = ref(false)
const aiDialogVisible = ref(false)
const aiResult = ref<any>(null)

function confidenceFormat(percentage: number) {
  return percentage + '%'
}

async function handleAiAnalysis() {
  if (!leftImageInfo.value?.patientId) {
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
      imageId: leftConfirmedId.value,
      diagnosisType: 1,
      patientId: leftImageInfo.value.patientId,
      doctorId: userStore.userInfo.userId
    })
    aiResult.value = res.data
    aiDialogVisible.value = true
  } catch (err: any) {
    ElMessage.error('AI 分析失败：' + (err?.message || '未知错误'))
  } finally {
    aiLoading.value = false
  }
}

onMounted(() => {
  const left = route.query.left as string
  const right = route.query.right as string
  if (left) {
    leftImageId.value = left
    leftConfirmedId.value = left
    loadRelatedImages(left)
  }
  if (right) {
    rightImageId.value = right
    rightConfirmedId.value = right
  }
})

async function loadRelatedImages(imageId: string) {
  loadingRelated.value = true
  try {
    const infoRes = await imageInfoApi(imageId)
    leftImageInfo.value = infoRes.data
    const { patientId, modality } = infoRes.data || {}
    if (patientId && modality) {
      const listRes = await imageListApi({ patientId, modality, page: 1, pageSize: 50 })
      const all = listRes.data?.records || listRes.data?.list || []
      // 排除当前影像自身
      relatedImages.value = all.filter((r: any) => r.imageId !== imageId)
    }
  } catch {
    relatedImages.value = []
  } finally {
    loadingRelated.value = false
  }
}

function selectRelatedImage(img: any) {
  rightImageId.value = img.imageId
  rightConfirmedId.value = img.imageId
  rightError.value = false
}

function confirmLeft() {
  const id = leftImageId.value.trim()
  if (!id) return
  leftConfirmedId.value = id
  leftError.value = false
  loadRelatedImages(id)
}

function confirmRight() {
  const id = rightImageId.value.trim()
  if (!id) return
  rightConfirmedId.value = id
  rightError.value = false
}

function onLeftError() { leftError.value = true }
function onRightError() { rightError.value = true }
function onLeftLoad() { leftError.value = false }
function onRightLoad() { rightError.value = false }
</script>

<style scoped>
.compare-page {
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

/* ===== 选择器行 ===== */

.selector-row {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.selector-panel {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: var(--cb-white, #fff);
  border-radius: var(--cb-radius-lg, 12px);
  box-shadow: var(--cb-shadow-sm, 0 1px 2px rgba(0, 0, 0, 0.06));
}

.selector-label {
  font-size: var(--cb-font-sm, 13px);
  font-weight: 500;
  color: var(--cb-text-secondary, #606266);
  white-space: nowrap;
}

.selector-input {
  display: flex;
  gap: 8px;
  flex: 1;
}

/* ===== 对比区域 ===== */

.compare-body {
  display: flex;
  gap: 20px;
  flex: 1;
  min-height: 0;
}

.compare-panel {
  flex: 1;
  width: 50%;
  display: flex;
  flex-direction: column;
  background: var(--cb-white, #fff);
  border-radius: var(--cb-radius-lg, 12px);
  box-shadow: var(--cb-shadow-sm, 0 1px 2px rgba(0, 0, 0, 0.06));
  overflow: hidden;
}

.panel-header {
  padding: 10px 16px;
  border-bottom: 1px solid var(--cb-border, #ebeef5);
  flex-shrink: 0;
}

.panel-title {
  font-size: var(--cb-font-sm, 13px);
  font-weight: 500;
  color: var(--cb-text-primary, #1f2f3d);
}

.panel-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  position: relative;
  overflow: hidden;
  min-height: 400px;
}

.compare-image {
  display: block;
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
  border-radius: 4px;
  user-select: none;
  -webkit-user-drag: none;
}

/* ===== 空状态 ===== */

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: var(--cb-text-placeholder, #c0c4cc);
  font-size: var(--cb-font-base, 14px);
}

/* ===== 加载失败覆盖 ===== */

.image-error-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--cb-danger, #f5222d);
  font-size: var(--cb-font-base, 14px);
  background: var(--cb-background, #f0f2f5);
}

/* ===== 主布局 ===== */

.compare-layout {
  display: flex;
  gap: 20px;
  flex: 1;
  min-height: 0;
}
.compare-body {
  flex: 1;
  display: flex;
  gap: 20px;
  min-width: 0;
}

/* ===== 历史影像面板 ===== */

.history-panel {
  width: 220px;
  flex-shrink: 0;
  background: var(--cb-white, #fff);
  border-radius: var(--cb-radius-lg, 12px);
  box-shadow: var(--cb-shadow-sm, 0 1px 2px rgba(0, 0, 0, 0.06));
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.history-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--cb-border, #ebeef5);
  font-size: var(--cb-font-sm, 13px);
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}
.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.history-item {
  display: flex;
  gap: 8px;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  border: 2px solid transparent;
}
.history-item:hover {
  background: var(--cb-primary-lighter, #f0f7ff);
}
.history-item.selected {
  border-color: var(--cb-primary, #1a7fbf);
  background: var(--cb-primary-lighter, #f0f7ff);
}
.history-thumb {
  width: 56px;
  height: 56px;
  border-radius: 4px;
  flex-shrink: 0;
  background: var(--cb-background, #f0f2f5);
}
.history-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  flex: 1;
}
.history-name {
  font-size: var(--cb-font-xs, 12px);
  color: var(--cb-text-primary, #1f2f3d);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.history-meta {
  font-size: 11px;
  color: var(--cb-text-placeholder, #c0c4cc);
}
.thumb-placeholder {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--cb-text-placeholder, #c0c4cc);
  font-size: 20px;
}

.panel-meta {
  font-size: var(--cb-font-xs, 12px);
  color: var(--cb-text-placeholder, #c0c4cc);
  margin-left: 8px;
}

/* ===== 响应式 ===== */

@media (max-width: 900px) {
  .selector-row {
    flex-direction: column;
  }

  .compare-body {
    flex-direction: column;
  }

  .compare-panel {
    width: 100%;
  }

  .panel-content {
    min-height: 300px;
  }
}

/* ===== AI 分析按钮行 ===== */

.action-row {
  flex-shrink: 0;
  margin-bottom: 16px;
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
