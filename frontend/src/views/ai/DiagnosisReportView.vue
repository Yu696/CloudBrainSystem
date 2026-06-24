<template>
  <div class="report-page">
    <div class="page-title">
      <el-button text @click="router.back()" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <span>诊断报告</span>
    </div>

    <div v-loading="loading" class="report-stack">
      <div v-if="report" class="report-stack-inner">
        <!-- 报告头 -->
        <div class="doctor-card report-head">
          <div class="card-body">
            <div class="head-top">
              <h2>AI 辅助诊断报告</h2>
              <el-tag :type="report.needsManualReview ? 'warning' : 'success'" size="large" effect="dark">
                {{ report.needsManualReview ? '建议人工复核' : '诊断完成' }}
              </el-tag>
            </div>
            <div class="head-meta">
              <span>编号：{{ report.diagnosisId }}</span>
              <span>类型：{{ report.diagnosisTypeName || '症状分析' }}</span>
              <span>模型：{{ report.aiModel }}</span>
              <span>时间：{{ report.createTime }}</span>
            </div>
          </div>
        </div>

        <!-- 疾病匹配 -->
        <div class="doctor-card" v-if="report.diseaseMatches?.length">
          <div class="card-title">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <polyline points="14 2 14 8 20 8" />
            </svg>
            <span>疾病匹配</span>
            <span class="card-badge">{{ report.diseaseMatches.length }} 项</span>
          </div>
          <div class="card-body">
            <div v-for="(d, idx) in report.diseaseMatches" :key="idx" class="match-block">
              <div class="match-header">
                <span class="match-name">{{ d.diseaseName }}</span>
                <el-tag size="small" type="info" effect="plain">{{ d.icdCode }}</el-tag>
                <span class="match-bar-wrap">
                  <span class="match-bar-fill" :style="{ width: (d.confidence * 100) + '%' }"></span>
                </span>
                <span class="match-pct">{{ (d.confidence * 100).toFixed(0) }}%</span>
              </div>
              <div v-if="d.diagnosisBasis" class="match-detail">依据：{{ d.diagnosisBasis }}</div>
              <div v-if="d.differentialDiagnosis?.length" class="match-detail diff">鉴别诊断：{{ d.differentialDiagnosis.join('、') }}</div>
            </div>
          </div>
        </div>

        <!-- 科室推荐 -->
        <div class="doctor-card" v-if="report.departmentRecommendations?.length">
          <div class="card-title">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
              <polyline points="9 22 9 12 15 12 15 22" />
            </svg>
            <span>科室推荐</span>
          </div>
          <div class="card-body">
            <div v-for="(dept, idx) in report.departmentRecommendations" :key="idx" class="dept-rec-row">
              <span class="rec-name">{{ dept.departmentName }}</span>
              <span class="rec-conf">{{ (dept.confidence * 100).toFixed(0) }}%</span>
            </div>
          </div>
        </div>

        <!-- 分析结论 -->
        <div class="doctor-card">
          <div class="card-title">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="16" x2="12" y2="12" />
              <line x1="12" y1="8" x2="12.01" y2="8" />
            </svg>
            <span>分析结论</span>
          </div>
          <div class="card-body">
            <div class="conclusion-body">{{ report.analysisResult }}</div>
          </div>
        </div>

        <!-- 报告信息 -->
        <div class="doctor-card meta-card">
          <div class="card-title">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="16" x2="12" y2="12" />
              <line x1="12" y1="8" x2="12.01" y2="8" />
            </svg>
            <span>报告信息</span>
          </div>
          <div class="card-body">
            <div class="info-grid-2">
              <div><span class="info-label">报告状态</span><span class="info-val">{{ report.statusName || (report.status === 2 ? '已完成' : '处理中') }}</span></div>
              <div><span class="info-label">置信度</span><span class="info-val" :class="{ 'low': report.needsManualReview }">{{ (report.confidenceScore * 100).toFixed(0) }}%</span></div>
              <div><span class="info-label">AI 模型</span><span class="info-val">{{ report.aiModel }}</span></div>
              <div><span class="info-label">创建时间</span><span class="info-val">{{ report.createTime }}</span></div>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-else-if="!loading" description="未找到诊断报告" :image-size="80" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getDiagnosisReportApi } from '@/api/ai'

const router = useRouter()
const route = useRoute()
const diagnosisId = route.params.diagnosisId as string

const loading = ref(false)
const report = ref<any>(null)

onMounted(async () => {
  if (!diagnosisId) {
    ElMessage.error('缺少诊断报告 ID')
    return
  }
  loading.value = true
  try {
    const res = await getDiagnosisReportApi(diagnosisId)
    report.value = res.data
  } catch {
    ElMessage.error('获取诊断报告失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.report-page {
  max-width: 860px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1C1C1E;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.page-title::before {
  content: '';
  display: inline-block;
  width: 3px;
  height: 20px;
  background: #007AFF;
  border-radius: 2px;
}
.back-btn {
  color: #8E8E93;
  padding: 4px;
}
.back-btn:hover {
  color: #007AFF;
}

.report-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.report-stack-inner {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ===== 医生端卡片 ===== */
.doctor-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}
.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 15px;
  font-weight: 600;
  color: #1C1C1E;
}
.card-title svg {
  color: #007AFF;
  flex-shrink: 0;
}
.card-badge {
  margin-left: auto;
  font-size: 12px;
  color: #8E8E93;
  background: #f5f5f7;
  padding: 1px 10px;
  border-radius: 10px;
}
.card-body {
  padding: 20px;
}

/* 报告头 */
.report-head {
  border-left: 4px solid #007AFF;
}
.head-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.head-top h2 {
  margin: 0;
  font-size: 22px;
  color: #1C1C1E;
}
.head-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  font-size: 13px;
  color: #8E8E93;
}

/* 疾病匹配 */
.match-block {
  padding: 14px;
  background: #f8f9fc;
  border-radius: 10px;
  margin-bottom: 8px;
}
.match-block:last-child { margin-bottom: 0; }
.match-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.match-name {
  font-weight: 600;
  font-size: 15px;
}
.match-bar-wrap {
  width: 80px;
  height: 6px;
  background: #e5e5ea;
  border-radius: 3px;
  overflow: hidden;
  margin-left: auto;
}
.match-bar-fill {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, #007AFF, #4da6ff);
  border-radius: 3px;
}
.match-pct {
  font-weight: 600;
  color: #007AFF;
  font-size: 14px;
  min-width: 36px;
  text-align: right;
}
.match-detail {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}
.match-detail.diff {
  margin-top: 4px;
  padding-top: 4px;
  border-top: 1px dashed #e5e5ea;
}

/* 科室 */
.dept-rec-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f7;
}
.dept-rec-row:last-child { border-bottom: none; }
.rec-name { font-weight: 500; }
.rec-conf { color: #007AFF; font-weight: 600; }

/* 结论 */
.conclusion-body {
  line-height: 1.8;
  color: #3C3C43;
  font-size: 14px;
  white-space: pre-wrap;
}

/* 报告信息 */
.info-grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.info-grid-2 div {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.info-label {
  font-size: 13px;
  color: #8E8E93;
}
.info-val {
  font-weight: 500;
  color: #1C1C1E;
}
.info-val.low { color: #FF9500; }

.meta-card {
  margin-bottom: 32px;
}
</style>
