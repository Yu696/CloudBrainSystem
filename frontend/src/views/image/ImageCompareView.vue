<template>
  <div class="compare-page">
    <!-- 页面标题 -->
    <div class="page-title">
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
          <el-button type="primary" @click="confirmLeft" :disabled="!leftImageId.trim()">
            确认
          </el-button>
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
          <el-button type="primary" @click="confirmRight" :disabled="!rightImageId.trim()">
            确认
          </el-button>
        </div>
      </div>
    </div>

    <!-- 对比区域 -->
    <div class="compare-body">
      <!-- 左侧影像 -->
      <div class="compare-panel">
        <div v-if="leftConfirmedId" class="panel-header">
          <span class="panel-title">影像 ID：{{ leftConfirmedId }}</span>
        </div>
        <div class="panel-content">
          <!-- 已确认 -->
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
          <!-- 空状态 -->
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
          <!-- 已确认 -->
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
          <!-- 空状态 -->
          <div v-else class="empty-state">
            <el-icon :size="48"><Picture /></el-icon>
            <span>请选择影像</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Picture, WarningFilled } from '@element-plus/icons-vue'
import { imagePreviewUrl } from '@/api/image'

const leftImageId = ref('')
const rightImageId = ref('')
const leftConfirmedId = ref('')
const rightConfirmedId = ref('')
const leftError = ref(false)
const rightError = ref(false)

function confirmLeft() {
  const id = leftImageId.value.trim()
  if (!id) return
  leftConfirmedId.value = id
  leftError.value = false
}

function confirmRight() {
  const id = rightImageId.value.trim()
  if (!id) return
  rightConfirmedId.value = id
  rightError.value = false
}

function onLeftError() {
  leftError.value = true
}

function onRightError() {
  rightError.value = true
}

function onLeftLoad() {
  leftError.value = false
}

function onRightLoad() {
  rightError.value = false
}
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
</style>
