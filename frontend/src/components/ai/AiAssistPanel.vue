<template>
  <div class="ai-assist-panel">
    <div class="panel-head">
      <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5" class="head-icon">
        <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
      </svg>
      <span>AI 辅助</span>
      <el-tag v-if="processing" type="warning" effect="dark" size="small" style="border-radius:20px">生成中...</el-tag>
    </div>

    <div class="panel-body">
      <!-- 对话输入 -->
      <div class="panel-section" v-if="!generatedPreview">
        <div class="sec-label">医患对话</div>
        <el-input
          v-model="dialogueText"
          type="textarea"
          :rows="5"
          placeholder="输入医患对话内容，AI 将自动生成结构化病历..."
          maxlength="5000"
          show-word-limit
          class="panel-input"
        />
        <el-button
          type="primary"
          size="small"
          style="width:100%;margin-top:10px;border-radius:10px"
          @click="handleGenerate"
          :loading="processing"
          :disabled="!dialogueText.trim()"
        >
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:4px">
            <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
          </svg>
          AI 生成病历
        </el-button>
      </div>

      <!-- 生成预览 -->
      <div v-if="generatedPreview" class="preview-area">
        <div class="pv-row"><span class="pv-lbl">主诉</span><span class="pv-val">{{ generatedPreview.chiefComplaint }}</span></div>
        <div class="pv-row"><span class="pv-lbl">现病史</span><span class="pv-val">{{ generatedPreview.presentIllness }}</span></div>
        <div class="pv-row" v-if="generatedPreview.pastHistory"><span class="pv-lbl">既往史</span><span class="pv-val">{{ generatedPreview.pastHistory }}</span></div>
        <div class="pv-row" v-if="generatedPreview.physicalExam"><span class="pv-lbl">体格检查</span><span class="pv-val">{{ generatedPreview.physicalExam }}</span></div>
        <div class="pv-row" v-if="generatedPreview.preliminaryDiagnosis"><span class="pv-lbl">初步诊断</span><span class="pv-val diagnosis">{{ generatedPreview.preliminaryDiagnosis }}</span></div>
        <div class="pv-row" v-if="generatedPreview.treatmentPlan"><span class="pv-lbl">治疗计划</span><span class="pv-val">{{ generatedPreview.treatmentPlan }}</span></div>

        <div class="pv-actions">
          <el-button type="primary" size="small" class="pv-btn" @click="$emit('apply', generatedPreview)">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:4px">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
              <polyline points="22 4 12 14.01 9 11.01" />
            </svg>
            应用到病历
          </el-button>
          <el-button size="small" class="pv-btn pv-cancel" @click="generatedPreview = null">
            放弃
          </el-button>
        </div>
      </div>

      <!-- 降级 -->
      <div v-if="fallbackMode" class="fallback-note">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#FF9500" stroke-width="1.5">
          <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
          <line x1="12" y1="9" x2="12" y2="13" />
        </svg>
        <span>AI 服务暂不可用，请手动填写</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { recordGenerateApi } from '@/api/ai'

const props = defineProps<{
  patientId: string
  doctorId: string
  appointmentId: string
}>()

const emit = defineEmits<{
  apply: [preview: any]
}>()

const dialogueText = ref('')
const processing = ref(false)
const generatedPreview = ref<any>(null)
const fallbackMode = ref(false)

async function handleGenerate() {
  if (!dialogueText.value.trim()) return
  processing.value = true
  fallbackMode.value = false
  try {
    const res = await recordGenerateApi({
      patientId: props.patientId,
      doctorId: props.doctorId,
      appointmentId: props.appointmentId,
      dialogueText: dialogueText.value
    })
    const data = res.data as any
    generatedPreview.value = data.recordPreview
    if (data.generationId) ElMessage.success('病历生成成功，请核对后应用')
  } catch (err: any) {
    if (err.response?.status === 500 || err.message?.includes('AI 服务')) {
      fallbackMode.value = true
    } else ElMessage.error('生成失败，请稍后重试')
  } finally {
    processing.value = false
  }
}
</script>

<style scoped>
.ai-assist-panel {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.panel-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #007AFF, #0056b3);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}
.head-icon { flex-shrink: 0; }

.panel-body { padding: 16px; }

.panel-section { }
.sec-label {
  font-size: 13px;
  font-weight: 600;
  color: #1C1C1E;
  margin-bottom: 8px;
}
.panel-input :deep(.el-textarea__inner) {
  border-radius: 10px;
  border-color: #e5e5ea;
  font-size: 13px;
}
.panel-input :deep(.el-textarea__inner:focus) {
  border-color: #007AFF;
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.08);
}

/* 预览区 */
.preview-area {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.pv-row { }
.pv-lbl {
  display: block;
  font-size: 11px;
  color: #8E8E93;
  font-weight: 500;
  margin-bottom: 2px;
}
.pv-val {
  display: block;
  font-size: 13px;
  color: #3C3C43;
  line-height: 1.5;
  white-space: pre-wrap;
}
.pv-val.diagnosis {
  color: #007AFF;
  font-weight: 500;
}
.pv-actions {
  display: flex;
  gap: 8px;
  margin-top: 6px;
}
.pv-btn {
  border-radius: 8px !important;
  flex: 1;
}
.pv-cancel {
  color: #8E8E93;
}

/* 降级 */
.fallback-note {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f5f5f7;
  border-radius: 10px;
  font-size: 13px;
  color: #8E8E93;
}
</style>
