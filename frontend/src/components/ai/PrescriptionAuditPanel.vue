<template>
  <div class="audit-panel">
    <!-- 面板头 -->
    <div class="panel-head" :class="headClass">
      <svg v-if="overallResult === 'PASS'" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
        <polyline points="22 4 12 14.01 9 11.01" />
      </svg>
      <svg v-else-if="overallResult === 'WARNING'" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
        <line x1="12" y1="9" x2="12" y2="13" />
        <line x1="12" y1="17" x2="12.01" y2="17" />
      </svg>
      <svg v-else-if="overallResult === 'REJECT'" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10" />
        <line x1="15" y1="9" x2="9" y2="15" />
        <line x1="9" y1="9" x2="15" y2="15" />
      </svg>
      <svg v-else viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10" />
        <line x1="12" y1="16" x2="12" y2="12" />
        <line x1="12" y1="8" x2="12.01" y2="8" />
      </svg>
      <span>{{ resultTitle }}</span>
      <el-tag v-if="auditResult" :color="tagBg" effect="dark" size="small" style="border:0;border-radius:20px;margin-left:auto">
        {{ auditResult.overallResultName || overallResult }}
      </el-tag>
    </div>

    <div class="panel-body">
      <!-- 未审核 -->
      <div v-if="!auditResult && !loading && !fallbackMode" class="state-empty">
        <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="#c7c7cc" stroke-width="1.5">
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="16" x2="12" y2="12" />
          <line x1="12" y1="8" x2="12.01" y2="8" />
        </svg>
        <div>提交处方后将自动进行 AI 审核</div>
      </div>

      <!-- 审核中 -->
      <div v-if="loading" class="state-loading">
        <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="#007AFF" stroke-width="2" class="spin-svg">
          <circle cx="12" cy="12" r="10" stroke-dasharray="31.4 31.4" stroke-linecap="round" />
        </svg>
        <span>AI 处方审核中...</span>
      </div>

      <!-- 降级 -->
      <div v-if="fallbackMode" class="state-fallback">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#FF9500" stroke-width="1.5">
          <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
          <line x1="12" y1="9" x2="12" y2="13" />
        </svg>
        <span>AI 审核暂不可用，请人工审核</span>
      </div>

      <!-- 审核结果 -->
      <div v-if="auditResult && !fallbackMode" class="audit-content">
        <!-- 患者关键信息 -->
        <div v-if="auditResult.patientContext" class="audit-section patient-bg">
          <div class="sec-head">患者关键信息</div>
          <div v-if="auditResult.patientContext.allergyHistory" class="bg-row danger">
            <span class="bg-lbl">过敏史</span>
            <span class="bg-val">{{ auditResult.patientContext.allergyHistory }}</span>
          </div>
          <div v-if="auditResult.patientContext.medicalHistory" class="bg-row">
            <span class="bg-lbl">既往病史</span>
            <span class="bg-val">{{ auditResult.patientContext.medicalHistory }}</span>
          </div>
          <div v-if="auditResult.patientContext.currentMedications?.length" class="bg-row">
            <span class="bg-lbl">当前用药</span>
            <span class="bg-val">{{ auditResult.patientContext.currentMedications.join('；') }}</span>
          </div>
        </div>

        <!-- 药品审核明细 -->
        <div class="audit-section">
          <div class="sec-head">药品审核明细</div>
          <div v-for="(item, idx) in auditResult.items" :key="idx" class="drug-audit-item">
            <div class="drug-audit-head">
              <span class="drug-audit-name">{{ item.drugName }}</span>
              <span class="drug-audit-badge" :class="item.result.toLowerCase()">
                {{ item.resultName || item.result }}
              </span>
            </div>
            <div v-for="(check, ci) in item.checks" :key="ci" class="check-line" :class="check.result.toLowerCase()">
              <span class="check-type">{{ check.checkType }}</span>
              <span class="check-badge" :class="check.result.toLowerCase()">{{ check.result }}</span>
              <span class="check-detail">{{ check.detail }}</span>
            </div>
          </div>
        </div>

        <!-- 摘要 -->
        <div v-if="auditResult.summary" class="audit-summary" :class="overallResult.toLowerCase()">
          <svg v-if="overallResult === 'PASS'" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
            <polyline points="22 4 12 14.01 9 11.01" />
          </svg>
          <svg v-else viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
            <line x1="12" y1="9" x2="12" y2="13" />
          </svg>
          <span>{{ auditResult.summary }}</span>
        </div>

        <!-- 元信息 -->
        <div class="audit-meta" v-if="auditResult.confidenceScore">
          <span>置信度 {{ (auditResult.confidenceScore * 100).toFixed(0) }}%</span>
          <span>模型 {{ auditResult.aiModel }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  auditResult: any
  loading: boolean
  fallbackMode: boolean
}>()

const overallResult = computed(() => props.auditResult?.overallResult || '')

const headClass = computed(() => {
  if (!props.auditResult) return ''
  switch (overallResult.value) {
    case 'PASS': return 'head-pass'
    case 'WARNING': return 'head-warning'
    case 'REJECT': return 'head-reject'
    default: return 'head-manual'
  }
})

const resultTitle = computed(() => {
  if (!props.auditResult) return '处方审核'
  switch (overallResult.value) {
    case 'PASS': return '审核通过'
    case 'WARNING': return '存在警告'
    case 'REJECT': return '存在禁忌'
    default: return '处方审核'
  }
})

const tagBg = computed(() => {
  switch (overallResult.value) {
    case 'PASS': return '#34C759'
    case 'WARNING': return '#FF9500'
    case 'REJECT': return '#FF3B30'
    default: return '#8E8E93'
  }
})
</script>

<style scoped>
.audit-panel {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  margin-top: 16px;
}

/* 面板头 */
.panel-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  transition: background 0.3s;
}
.head-pass { background: linear-gradient(135deg, #34C759, #28a745); }
.head-warning { background: linear-gradient(135deg, #FF9500, #e68a00); }
.head-reject { background: linear-gradient(135deg, #FF3B30, #d63031); }
.head-manual { background: linear-gradient(135deg, #8E8E93, #636366); }

.panel-body { padding: 16px; }

/* 状态 */
.state-empty, .state-loading {
  text-align: center;
  padding: 24px 0;
  color: #8E8E93;
  font-size: 13px;
}
.state-empty svg { margin-bottom: 8px; }
.state-loading { display: flex; align-items: center; justify-content: center; gap: 8px; }
.spin-svg { animation: spin 1.2s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.state-fallback {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f5f5f7;
  border-radius: 10px;
  font-size: 13px;
  color: #8E8E93;
}

/* 审核内容 */
.audit-content { }
.audit-section { margin-bottom: 14px; }
.sec-head {
  font-size: 13px;
  font-weight: 600;
  color: #1C1C1E;
  margin-bottom: 8px;
}

/* 患者背景 */
.patient-bg {
  background: #f8f9fc;
  border-radius: 10px;
  padding: 12px;
}
.bg-row {
  display: flex;
  gap: 8px;
  font-size: 13px;
  padding: 4px 0;
}
.bg-lbl { color: #8E8E93; flex-shrink: 0; min-width: 56px; }
.bg-val { color: #3C3C43; }
.bg-row.danger .bg-val { color: #FF3B30; font-weight: 600; }

/* 药品审核 */
.drug-audit-item {
  background: #f8f9fc;
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 8px;
}
.drug-audit-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 6px;
  border-bottom: 1px solid #e5e5ea;
}
.drug-audit-name { font-weight: 600; font-size: 14px; color: #1C1C1E; }
.drug-audit-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 20px;
}
.drug-audit-badge.pass { background: #e8f8e8; color: #34C759; }
.drug-audit-badge.warning { background: #fff3e0; color: #FF9500; }
.drug-audit-badge.reject { background: #ffe8e8; color: #FF3B30; }

.check-line {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 5px 0;
  font-size: 13px;
}
.check-type { color: #8E8E93; min-width: 60px; flex-shrink: 0; font-size: 12px; }
.check-badge {
  font-size: 10px;
  font-weight: 600;
  padding: 1px 8px;
  border-radius: 20px;
  flex-shrink: 0;
}
.check-badge.pass { background: #e8f8e8; color: #34C759; }
.check-badge.warning { background: #fff3e0; color: #FF9500; }
.check-badge.reject { background: #ffe8e8; color: #FF3B30; }
.check-detail { color: #3C3C43; line-height: 1.4; flex: 1; }

/* 摘要 */
.audit-summary {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.5;
}
.audit-summary.pass { background: #e8f8e8; color: #28a745; }
.audit-summary.warning { background: #fff3e0; color: #e68a00; }
.audit-summary.reject { background: #ffe8e8; color: #d63031; }
.audit-summary svg { flex-shrink: 0; margin-top: 1px; }

/* 元信息 */
.audit-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #c7c7cc;
  padding-top: 8px;
  margin-top: 8px;
  border-top: 1px solid #f0f0f0;
}
</style>
