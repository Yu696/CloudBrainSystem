<template>
  <div class="triage-page">
    <!-- 页头 -->
    <div class="page-header">
      <div class="page-icon">
        <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="12" r="10" />
          <path d="M12 8v8M8 12h8" />
        </svg>
      </div>
      <div class="page-text">
        <h1>智能分诊</h1>
        <p class="page-subtitle">描述您的症状，AI 将为您推荐合适的科室和医生</p>
      </div>
    </div>

    <!-- 症状输入卡片 -->
    <div class="patient-card input-card">
      <div class="card-header">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5" class="header-icon">
          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
        </svg>
        <span>症状描述</span>
      </div>
      <div class="card-body">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
          <div class="input-tip">请详细描述您的主要症状，帮助我们更准确地分析</div>

          <el-form-item prop="chiefComplaint">
            <el-input
              v-model="form.chiefComplaint"
              type="textarea"
              :rows="4"
              placeholder="例如：头痛发热三天，伴有咳嗽、咽痛..."
              maxlength="500"
              show-word-limit
              class="symptom-input"
            />
          </el-form-item>

          <el-row :gutter="16" class="symptom-options">
            <el-col :span="12">
              <div class="option-label">持续时间</div>
              <el-select v-model="form.duration" placeholder="选择持续时间" class="option-select" :teleported="false">
                <el-option label="1天以内" value="1天内" />
                <el-option label="2-3天" value="2-3天" />
                <el-option label="3-7天" value="3-7天" />
                <el-option label="1-2周" value="1-2周" />
                <el-option label="2周以上" value="2周以上" />
              </el-select>
            </el-col>
            <el-col :span="12">
              <div class="option-label">伴随症状</div>
              <el-select
                v-model="form.otherSymptoms"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="选择或输入"
                class="option-select"
                :teleported="false"
              >
                <el-option label="咳嗽" value="咳嗽" />
                <el-option label="乏力" value="乏力" />
                <el-option label="恶心" value="恶心" />
                <el-option label="呕吐" value="呕吐" />
                <el-option label="腹泻" value="腹泻" />
                <el-option label="胸闷" value="胸闷" />
                <el-option label="心悸" value="心悸" />
                <el-option label="腹痛" value="腹痛" />
                <el-option label="头晕" value="头晕" />
                <el-option label="咽痛" value="咽痛" />
                <el-option label="鼻塞" value="鼻塞" />
                <el-option label="肌肉酸痛" value="肌肉酸痛" />
              </el-select>
            </el-col>
          </el-row>

          <div class="input-action">
            <el-button type="primary" size="large" class="cta-button" @click="handleTriage" :loading="analyzing">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:6px">
                <circle cx="11" cy="11" r="8" />
                <path d="M21 21l-4.35-4.35" />
              </svg>
              <span>开始分析</span>
            </el-button>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 分析中动画 -->
    <div v-if="analyzing" class="analyzing-section">
      <div class="patient-card">
        <div class="card-body" style="text-align:center;padding:48px 24px">
          <div class="analyzing-animation">
            <div class="pulse-circle"></div>
            <div class="pulse-circle delay-1"></div>
            <div class="pulse-circle delay-2"></div>
          </div>
          <div class="analyzing-text">AI 正在分析您的症状</div>
          <div class="analyzing-sub">正在结合医学知识库进行智能匹配，请稍候...</div>
        </div>
      </div>
    </div>

    <!-- 降级模式 -->
    <div v-if="fallbackMode" class="patient-card fallback-card">
      <div class="card-body" style="text-align:center;padding:40px 24px">
        <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#faad14" stroke-width="1.5" style="margin-bottom:16px">
          <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
          <line x1="12" y1="9" x2="12" y2="13" />
          <line x1="12" y1="17" x2="12.01" y2="17" />
        </svg>
        <div class="fallback-title">AI 服务暂不可用</div>
        <div class="fallback-desc">您可以直接选择科室进行挂号，或稍后再试</div>
        <el-button type="primary" size="large" class="cta-button" @click="goManualDept" style="margin-top:20px">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:6px">
            <rect x="3" y="3" width="18" height="18" rx="3" />
            <path d="M12 8v8M8 12h8" />
          </svg>
          选择科室挂号
        </el-button>
      </div>
    </div>

    <!-- 结果展示 -->
    <div v-if="triageResult && !fallbackMode" class="result-section">
      <!-- 置信度横幅 -->
      <div class="confidence-banner" :class="triageResult.needsManualReview ? 'warning' : 'success'">
        <svg v-if="triageResult.needsManualReview" viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
          <line x1="12" y1="9" x2="12" y2="13" />
          <line x1="12" y1="17" x2="12.01" y2="17" />
        </svg>
        <svg v-else viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
          <polyline points="22 4 12 14.01 9 11.01" />
        </svg>
        <div class="banner-text">
          <div class="banner-title">{{ triageResult.needsManualReview ? '置信度较低，建议就医' : '分诊分析完成' }}</div>
          <div class="banner-desc">{{ triageResult.needsManualReview ? '当前分析结果置信度偏低，建议您前往医院由医生进行专业诊断。' : '基于您的症状，AI 已为您推荐合适的科室和医生。' }}</div>
        </div>
        <div class="banner-score">
          <div class="score-circle" :class="triageResult.needsManualReview ? 'low' : 'high'">
            {{ (triageResult.confidenceScore * 100).toFixed(0) }}%
          </div>
          <div class="score-label">置信度</div>
        </div>
      </div>

      <!-- 推荐科室 -->
      <div class="patient-card">
        <div class="card-header">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5" class="header-icon">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
            <polyline points="9 22 9 12 15 12 15 22" />
          </svg>
          <span>推荐科室</span>
        </div>
        <div class="card-body">
          <div v-for="(dept, idx) in allDepartments" :key="dept.departmentId || idx" class="dept-card-item" :class="{ first: idx === 0 }">
            <div class="dept-card-left">
              <span v-if="idx === 0" class="dept-badge">首选</span>
              <span class="dept-name">{{ dept.departmentName }}</span>
              <span class="dept-confidence">{{ (dept.confidence * 100).toFixed(0) }}% 匹配</span>
            </div>
            <el-button type="primary" size="default" class="dept-book-btn" @click="goBookDept(dept)">
              挂号
            </el-button>
          </div>
          <div v-if="allDepartments.length === 0" style="text-align:center;color:#8E8E93;padding:16px">
            暂无可挂号科室，建议重新描述症状或直接选择科室
          </div>
        </div>
      </div>

      <!-- 疑似疾病 -->
      <div class="patient-card">
        <div class="card-header">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5" class="header-icon">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" />
            <line x1="16" y1="13" x2="8" y2="13" />
            <line x1="16" y1="17" x2="8" y2="17" />
          </svg>
          <span>疑似疾病</span>
        </div>
        <div class="card-body">
          <div v-for="(disease, idx) in triageResult.diseaseMatches" :key="idx" class="disease-item" :class="{ last: idx === triageResult.diseaseMatches.length - 1 }">
            <div class="disease-top">
              <div class="disease-name">
                {{ disease.diseaseName }}
                <span class="disease-icd">{{ disease.icdCode }}</span>
              </div>
              <div class="disease-bar">
                <div class="bar-track">
                  <div class="bar-fill" :style="{ width: (disease.confidence * 100) + '%' }"></div>
                </div>
                <span class="bar-label">{{ (disease.confidence * 100).toFixed(0) }}%</span>
              </div>
            </div>
            <div class="disease-symptoms">
              <span class="sym-label">匹配症状：</span>
              <span v-for="(s, i) in disease.matchedSymptoms" :key="i" class="sym-tag">{{ s }}{{ i < disease.matchedSymptoms.length - 1 ? '、' : '' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 推荐医生 -->
      <div class="patient-card" v-if="triageResult.recommendedDoctors?.length">
        <div class="card-header">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5" class="header-icon">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
            <circle cx="12" cy="7" r="4" />
          </svg>
          <span>推荐医生</span>
        </div>
        <div class="card-body">
          <div v-for="doc in triageResult.recommendedDoctors" :key="doc.doctorId" class="doctor-card-item">
            <div class="doc-avatar-circle">{{ doc.doctorName.charAt(0) }}</div>
            <div class="doc-info">
              <div class="doc-name-row">
                <span class="doc-name">{{ doc.doctorName }}</span>
                <span class="doc-title">{{ doc.title }}</span>
              </div>
              <div class="doc-dept">{{ doc.departmentName }}</div>
            </div>
            <div class="doc-match">
              <div class="match-val">{{ (doc.matchScore * 100).toFixed(0) }}%</div>
              <div class="match-label">匹配</div>
            </div>
            <el-button type="primary" size="small" class="doc-book-btn" @click="goBookDoctor(doc)">
              挂号
            </el-button>
          </div>
        </div>
      </div>

      <!-- 操作区 -->
      <div class="action-bar-patient">
        <el-button size="large" class="cta-button cta-secondary" @click="resetTriage">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:6px">
            <polyline points="1 4 1 10 7 10" />
            <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10" />
          </svg>
          重新描述
        </el-button>
      </div>

      <!-- 分析详情 -->
      <div class="patient-card meta-card">
        <div class="card-header">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5" class="header-icon">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="16" x2="12" y2="12" />
            <line x1="12" y1="8" x2="12.01" y2="8" />
          </svg>
          <span>分析详情</span>
        </div>
        <div class="card-body">
          <div class="analysis-text">{{ triageResult.analysisDetail }}</div>
          <div class="analysis-meta">
            <span>AI 模型：{{ triageResult.aiModel }}</span>
            <span>响应：{{ triageResult.responseTimeMs }}ms</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { triageApi } from '@/api/ai'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()

const patientId = ref(localStorage.getItem('patientId') || '')

const form = reactive({
  chiefComplaint: '',
  duration: '',
  otherSymptoms: [] as string[]
})

const rules = {
  chiefComplaint: [
    { required: true, message: '请输入主诉症状', trigger: 'blur' },
    { min: 4, message: '请至少输入4个字符', trigger: 'blur' }
  ]
}

const analyzing = ref(false)
const triageResult = ref<any>(null)
const fallbackMode = ref(false)

// 合并推荐科室 + 备选科室为统一列表，按置信度降序（去重：备选中与首选相同的跳过）
const allDepartments = computed(() => {
  const result = triageResult.value
  if (!result) return []
  const list: any[] = []
  const preferredId = result.recommendedDepartment?.departmentId
  if (preferredId) {
    list.push(result.recommendedDepartment)
  }
  if (result.alternativeDepartments) {
    for (const d of result.alternativeDepartments) {
      // 如果备选科室与首选科室相同，跳过
      if (d.departmentId && d.departmentId === preferredId) continue
      if (d.departmentId) list.push(d)
    }
  }
  list.sort((a: any, b: any) => (b.confidence || 0) - (a.confidence || 0))
  return list
})

onMounted(() => {
  if (!patientId.value) {
    ElMessage.warning('请先完善个人信息')
    router.push('/profile')
    return
  }
  if (route.query.complaint) {
    form.chiefComplaint = route.query.complaint as string
  }
})

async function handleTriage() {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }

  analyzing.value = true
  fallbackMode.value = false
  triageResult.value = null

  try {
    const additionalInfo: Record<string, any> = {}
    if (form.duration) additionalInfo.duration = form.duration
    if (form.otherSymptoms.length) additionalInfo.otherSymptoms = form.otherSymptoms

    const res = await triageApi({
      patientId: patientId.value,
      chiefComplaint: form.chiefComplaint,
      additionalInfo: Object.keys(additionalInfo).length ? additionalInfo : undefined
    })
    triageResult.value = res.data
  } catch (err: any) {
    if (err.response?.status === 500 || err.message?.includes('AI 服务')) {
      fallbackMode.value = true
    } else {
      ElMessage.error('分诊失败，请稍后重试')
    }
  } finally {
    analyzing.value = false
  }
}

function resetTriage() {
  triageResult.value = null
  fallbackMode.value = false
  form.chiefComplaint = ''
  form.duration = ''
  form.otherSymptoms = []
}

function goBookAppointment() {
  if (!triageResult.value?.recommendedDepartment?.departmentId) return
  goBookDept(triageResult.value.recommendedDepartment)
}

function goBookDept(dept: any) {
  router.push({
    path: '/appointment/doctor',
    query: {
      deptId: dept.departmentId,
      deptName: dept.departmentName,
      fromTriage: 'true'
    }
  })
}

function goBookDoctor(doc: any) {
  router.push({
    path: '/appointment/doctor',
    query: {
      doctorId: doc.doctorId,
      deptId: doc.departmentId || triageResult.value?.recommendedDepartment?.departmentId,
      deptName: doc.departmentName,
      fromTriage: 'true'
    }
  })
}

function goManualDept() {
  router.push('/appointment/dept')
}
</script>

<style scoped>
.triage-page {
  max-width: 720px;
  margin: 0 auto;
}

/* ===== 页头 ===== */
.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 28px;
}
.page-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #e8f4fd, #d0ebfa);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #007AFF;
  flex-shrink: 0;
}
.page-text h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #1C1C1E;
  line-height: 1.3;
}
.page-subtitle {
  margin: 4px 0 0;
  font-size: 14px;
  color: #8E8E93;
}

/* ===== 患者端通用卡片 ===== */
.patient-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
  overflow: hidden;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 24px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 16px;
  font-weight: 600;
  color: #1C1C1E;
}
.header-icon {
  color: #007AFF;
  flex-shrink: 0;
}
.card-body {
  padding: 24px;
}

/* ===== 输入区 ===== */
.input-tip {
  font-size: 13px;
  color: #8E8E93;
  margin-bottom: 12px;
  line-height: 1.5;
}
.symptom-input :deep(.el-textarea__inner) {
  border-radius: 12px;
  border-color: #e5e5ea;
  font-size: 14px;
  line-height: 1.6;
  padding: 14px 16px;
  transition: all 0.2s;
}
.symptom-input :deep(.el-textarea__inner:focus) {
  border-color: #007AFF;
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.1);
}
.symptom-options {
  margin-top: 8px;
}
.option-label {
  font-size: 13px;
  color: #8E8E93;
  margin-bottom: 6px;
  font-weight: 500;
}
.option-select {
  width: 100%;
}
.option-select :deep(.el-select__wrapper) {
  border-radius: 10px;
  border-color: #e5e5ea;
}

.input-action {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* ===== CTA 按钮 ===== */
.cta-button {
  border-radius: 12px !important;
  padding: 12px 32px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  height: auto !important;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.25s ease;
}
.cta-button:active {
  transform: scale(0.97);
}
.cta-primary {
  min-width: 260px;
}
.cta-secondary {
  min-width: 160px;
}

/* ===== 分析中动画 ===== */
.analyzing-section .patient-card {
  border: 1px solid #e8f4fd;
}
.analyzing-animation {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  height: 48px;
}
.pulse-circle {
  width: 14px;
  height: 14px;
  background: #007AFF;
  border-radius: 50%;
  animation: pulse 1.4s ease-in-out infinite;
}
.pulse-circle.delay-1 {
  animation-delay: 0.2s;
}
.pulse-circle.delay-2 {
  animation-delay: 0.4s;
}
@keyframes pulse {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}
.analyzing-text {
  font-size: 18px;
  font-weight: 600;
  color: #1C1C1E;
  margin-bottom: 8px;
}
.analyzing-sub {
  font-size: 14px;
  color: #8E8E93;
}

/* ===== 置信度横幅 ===== */
.confidence-banner {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  border-radius: 16px;
  margin-bottom: 20px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.confidence-banner.warning {
  border-left: 4px solid #FF9500;
}
.confidence-banner.warning svg {
  color: #FF9500;
  flex-shrink: 0;
}
.confidence-banner.success {
  border-left: 4px solid #34C759;
}
.confidence-banner.success svg {
  color: #34C759;
  flex-shrink: 0;
}
.banner-text {
  flex: 1;
}
.banner-title {
  font-size: 16px;
  font-weight: 600;
  color: #1C1C1E;
  margin-bottom: 4px;
}
.banner-desc {
  font-size: 13px;
  color: #8E8E93;
  line-height: 1.5;
}
.banner-score {
  text-align: center;
  flex-shrink: 0;
}
.score-circle {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  margin: 0 auto 4px;
}
.score-circle.high {
  background: #e8f8e8;
  color: #34C759;
}
.score-circle.low {
  background: #fff3e0;
  color: #FF9500;
}
.score-label {
  font-size: 11px;
  color: #8E8E93;
}

/* ===== 科室推荐 ===== */
.dept-card-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-radius: 12px;
  border-bottom: 1px solid #f0f0f0;
}
.dept-card-item.first {
  background: #f0f7ff;
}
.dept-card-item:last-child {
  border-bottom: none;
}
.dept-card-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}
.dept-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 20px;
  background: #007AFF;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}
.dept-name {
  font-weight: 600;
  font-size: 15px;
  color: #1C1C1E;
}
.dept-confidence {
  font-size: 13px;
  color: #8E8E93;
}
.dept-book-btn {
  border-radius: 10px !important;
  flex-shrink: 0;
}

/* ===== 疾病匹配 ===== */
.disease-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}
.disease-item.last {
  border-bottom: none;
  padding-bottom: 0;
}
.disease-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  gap: 16px;
}
.disease-name {
  font-weight: 600;
  font-size: 15px;
  color: #1C1C1E;
  display: flex;
  align-items: center;
  gap: 6px;
}
.disease-icd {
  font-size: 12px;
  color: #8E8E93;
  font-weight: 400;
}
.disease-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.bar-track {
  width: 80px;
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #007AFF, #4da6ff);
  border-radius: 3px;
  transition: width 0.6s ease;
}
.bar-label {
  font-size: 13px;
  color: #007AFF;
  font-weight: 600;
  min-width: 36px;
}
.disease-symptoms {
  font-size: 13px;
  color: #8E8E93;
}
.sym-label {
  color: #8E8E93;
}
.sym-tag {
  color: #3C3C43;
}

/* ===== 医生卡片 ===== */
.doctor-card-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
}
.doctor-card-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}
.doc-avatar-circle {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #007AFF, #4da6ff);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  flex-shrink: 0;
}
.doc-info {
  flex: 1;
}
.doc-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.doc-name {
  font-weight: 600;
  font-size: 15px;
}
.doc-title {
  font-size: 12px;
  color: #8E8E93;
  background: #f5f5f7;
  padding: 1px 8px;
  border-radius: 10px;
}
.doc-dept {
  font-size: 13px;
  color: #8E8E93;
}
.doc-match {
  text-align: center;
}
.match-val {
  font-size: 20px;
  font-weight: 700;
  color: #007AFF;
}
.match-label {
  font-size: 11px;
  color: #8E8E93;
}
.doc-book-btn {
  border-radius: 10px !important;
  flex-shrink: 0;
}

/* ===== 操作栏 ===== */
.action-bar-patient {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin: 24px 0;
  flex-wrap: wrap;
}

/* ===== 分析详情 ===== */
.analysis-text {
  line-height: 1.8;
  color: #3C3C43;
  font-size: 14px;
  white-space: pre-wrap;
}
.analysis-meta {
  display: flex;
  gap: 24px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  font-size: 12px;
  color: #c7c7cc;
}
.meta-card {
  margin-bottom: 40px;
}

/* ===== 降级 ===== */
.fallback-title {
  font-size: 18px;
  font-weight: 600;
  color: #1C1C1E;
  margin-bottom: 8px;
}
.fallback-desc {
  font-size: 14px;
  color: #8E8E93;
}
</style>
