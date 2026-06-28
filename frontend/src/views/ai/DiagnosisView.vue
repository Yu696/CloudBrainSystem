<template>
  <div class="diag-page">
    <div class="page-title">
      <el-button text @click="router.back()" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <span>AI 辅助诊断</span>
      <el-tag v-if="patientInfo" type="info" effect="plain" class="patient-tag">
        {{ patientInfo.name }} · {{ patientInfo.gender === 1 ? '男' : '女' }} · {{ patientInfo.age || '-' }}岁
      </el-tag>
    </div>

    <div v-loading="loading" class="diag-layout">
      <el-row :gutter="20">
        <!-- 主工作区 -->
        <el-col :span="16">
          <!-- 症状输入 -->
          <div class="doctor-card">
            <div class="card-title">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
              </svg>
              <span>症状与体征</span>
            </div>
            <div class="card-body">
              <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" size="default">
                <el-form-item label="诊断类型">
                  <el-radio-group v-model="form.diagnosisType">
                    <el-radio :value="0" class="diag-radio">症状分析</el-radio>
                    <el-radio :value="1" class="diag-radio" disabled>影像诊断（预留）</el-radio>
                  </el-radio-group>
                </el-form-item>

                <el-form-item label="主诉" prop="chiefComplaint">
                  <el-input v-model="form.chiefComplaint" type="textarea" :rows="3" placeholder="患者主诉..." maxlength="500" show-word-limit class="diag-input" />
                </el-form-item>

                <el-form-item label="现病史">
                  <el-input v-model="form.presentIllness" type="textarea" :rows="3" placeholder="现病史描述..." maxlength="2000" class="diag-input" />
                </el-form-item>

                <el-form-item label="体格检查">
                  <el-input v-model="form.physicalExam" type="textarea" :rows="3" placeholder="体格检查结果..." maxlength="2000" class="diag-input" />
                </el-form-item>

                <el-form-item label="辅助检查">
                  <el-input v-model="form.auxiliaryExam" type="textarea" :rows="2" placeholder="辅助检查结果..." maxlength="2000" class="diag-input" />
                </el-form-item>
              </el-form>

              <div class="form-action-bar">
                <el-button type="primary" size="large" @click="handleDiagnosis" :loading="diagnosing" class="action-btn">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:4px">
                    <circle cx="11" cy="11" r="8" />
                    <path d="M21 21l-4.35-4.35" />
                  </svg>
                  <span>开始诊断分析</span>
                </el-button>
              </div>
            </div>
          </div>

          <!-- 诊断结果 -->
          <div v-if="diagnosisResult" class="doctor-card result-card">
            <div class="card-title">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
              </svg>
              <span>诊断建议</span>
              <el-tag :type="diagnosisResult.needsManualReview ? 'warning' : 'success'" effect="dark" size="small">
                {{ diagnosisResult.needsManualReview ? '建议人工复核' : 'AI 诊断完成' }}
              </el-tag>
              <span class="result-confidence">置信度 {{ (diagnosisResult.confidenceScore * 100).toFixed(0) }}%</span>
            </div>
            <div class="card-body">
              <div class="result-block">
                <div class="block-label">疑似疾病</div>
                <div v-for="(d, idx) in diagnosisResult.diseaseMatches" :key="idx" class="disease-row">
                  <div class="disease-row-header">
                    <strong>{{ d.diseaseName }}</strong>
                    <el-tag size="small" type="info" effect="plain">{{ d.icdCode }}</el-tag>
                    <span class="disease-conf">{{ (d.confidence * 100).toFixed(0) }}%</span>
                  </div>
                  <div class="disease-row-detail">诊断依据：{{ d.diagnosisBasis }}</div>
                  <div v-if="d.differentialDiagnosis?.length" class="disease-row-detail diff">
                    鉴别诊断：{{ d.differentialDiagnosis.join('、') }}
                  </div>
                </div>
              </div>

              <el-divider style="margin:16px 0" />

              <div class="result-block">
                <div class="block-label">分析结论</div>
                <div v-if="diagnosisResult.analysisResult" class="conclusion-text">{{ diagnosisResult.analysisResult }}</div>
                <el-empty v-else description="AI 未返回有效的分析结论，请补充更多症状信息后重试" :image-size="60" />
              </div>

              <div class="form-action-bar" style="margin-top:16px">
                <el-button type="primary" @click="goReport" class="action-btn">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:4px">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                    <polyline points="14 2 14 8 20 8" />
                  </svg>
                  查看完整报告
                </el-button>
                <el-button @click="resetForm">重新诊断</el-button>
              </div>
            </div>
          </div>

          <!-- 降级提示 -->
          <div v-if="fallbackMode" class="doctor-card">
            <div class="card-body" style="text-align:center;padding:48px 24px">
              <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#faad14" stroke-width="1.5" style="margin-bottom:16px">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
                <line x1="12" y1="9" x2="12" y2="13" />
                <line x1="12" y1="17" x2="12.01" y2="17" />
              </svg>
              <div style="font-size:16px;font-weight:600;color:#1C1C1E;margin-bottom:8px">AI 服务暂不可用</div>
              <div style="font-size:14px;color:#8E8E93;margin-bottom:20px">请根据临床经验进行诊断</div>
              <el-button type="primary" @click="resetForm">重新尝试</el-button>
            </div>
          </div>
        </el-col>

        <!-- 侧边患者信息 -->
        <el-col :span="8">
          <div class="doctor-card sidebar-card">
            <div class="card-title" style="border-bottom-color:#e8f4fd">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
              <span>患者信息</span>
            </div>
            <div class="card-body" style="padding:16px">
              <div v-if="patientInfo" class="patient-detail">
                <div class="p-row"><span class="p-label">姓名</span><span class="p-val">{{ patientInfo.name }}</span></div>
                <div class="p-row"><span class="p-label">性别/年龄</span><span class="p-val">{{ patientInfo.gender === 1 ? '男' : '女' }} / {{ patientInfo.age || '-' }}</span></div>
                <div class="p-row"><span class="p-label">联系电话</span><span class="p-val">{{ patientInfo.phone || '-' }}</span></div>
                <div class="p-divider"></div>
                <div class="p-row warning"><span class="p-label">过敏史</span><span class="p-val">{{ patientInfo.allergyHistory || '无' }}</span></div>
                <div class="p-row"><span class="p-label">既往病史</span><span class="p-val">{{ patientInfo.medicalHistory || '无' }}</span></div>
                <div class="p-row"><span class="p-label">遗传病史</span><span class="p-val">{{ patientInfo.geneticDiseases || '无' }}</span></div>
              </div>
              <div v-else style="text-align:center;padding:24px 0;color:#8E8E93;font-size:14px">
                <svg viewBox="0 0 24 24" width="40" height="40" fill="none" stroke="#c7c7cc" stroke-width="1.5" style="margin-bottom:8px">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
                <div>请从患者列表选择</div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 分析中遮罩 -->
    <div v-if="diagnosing" class="diag-overlay">
      <div class="diag-modal">
        <div class="modal-spinner">
          <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#007AFF" stroke-width="2" class="spinner-svg">
            <circle cx="12" cy="12" r="10" stroke-dasharray="31.4 31.4" stroke-linecap="round" />
          </svg>
        </div>
        <div class="modal-title">AI 正在分析中</div>
        <div class="modal-desc">正在结合患者病史和症状进行综合分析</div>
        <el-progress :percentage="loadingProgress" :stroke-width="5" color="#007AFF" style="width:280px;margin-top:20px" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { diagnosisApi } from '@/api/ai'
import { getPatientInfoApi } from '@/api/patient'
import { useUserStore } from '@/stores/user'
import { getMyDoctorInfoApi } from '@/api/appointment'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()

const patientId = route.params.patientId as string
const patientInfo = ref<any>(null)
const myDoctorId = ref('')
const loading = ref(false)
const diagnosing = ref(false)
const loadingProgress = ref(0)
const diagnosisResult = ref<any>(null)
const fallbackMode = ref(false)

const form = reactive({
  diagnosisType: 0,
  chiefComplaint: '',
  presentIllness: '',
  physicalExam: '',
  auxiliaryExam: ''
})

const rules = {
  chiefComplaint: [{ required: true, message: '请输入主诉', trigger: 'blur' }]
}

onMounted(async () => {
  loading.value = true
  try {
    const meRes = await getMyDoctorInfoApi()
    myDoctorId.value = (meRes.data as any)?.doctorId || userStore.userInfo?.userId || ''
  } catch { /* ignore */ }

  if (patientId) {
    try {
      const pRes = await getPatientInfoApi(patientId)
      patientInfo.value = pRes.data
    } catch {
      ElMessage.error('获取患者信息失败')
    }
  }
  // 从路由参数预填主诉（由待诊列表/患者列表传入）
  if (route.query.complaint) {
    form.chiefComplaint = route.query.complaint as string
  }
  loading.value = false
})

async function handleDiagnosis() {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }

  fallbackMode.value = false
  diagnosisResult.value = null
  diagnosing.value = true
  loadingProgress.value = 0

  const timer = setInterval(() => {
    if (loadingProgress.value < 90) loadingProgress.value += Math.random() * 15
  }, 500)

  try {
    const res = await diagnosisApi({
      patientId,
      doctorId: myDoctorId.value,
      diagnosisType: form.diagnosisType,
      symptomData: {
        chiefComplaint: form.chiefComplaint,
        presentIllness: form.presentIllness,
        physicalExam: form.physicalExam,
        auxiliaryExam: form.auxiliaryExam
      }
    })
    loadingProgress.value = 100
    setTimeout(() => { diagnosisResult.value = res.data; diagnosing.value = false }, 400)
    clearInterval(timer)
    return
  } catch (err: any) {
    clearInterval(timer)
    diagnosing.value = false
    if (err.response?.status === 500 || err.message?.includes('AI 服务')) {
      fallbackMode.value = true
    } else {
      ElMessage.error('诊断分析失败，请稍后重试')
    }
  }
  diagnosing.value = false
}

function resetForm() {
  diagnosisResult.value = null
  fallbackMode.value = false
  form.chiefComplaint = ''
  form.presentIllness = ''
  form.physicalExam = ''
  form.auxiliaryExam = ''
}

function goReport() {
  if (diagnosisResult.value?.diagnosisId) {
    router.push(`/ai/diagnosis-report/${diagnosisResult.value.diagnosisId}`)
  }
}
</script>

<style scoped>
.diag-page {
  max-width: 1200px;
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
.patient-tag {
  margin-left: 8px;
  font-size: 13px;
  border-radius: 20px;
  padding: 0 12px;
}

/* ===== 医生端卡片 ===== */
.doctor-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
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
.result-confidence {
  margin-left: auto;
  font-size: 13px;
  font-weight: 500;
  color: #007AFF;
}
.card-body {
  padding: 20px;
}

/* ===== 表单 ===== */
.diag-input :deep(.el-textarea__inner) {
  border-radius: 8px;
  border-color: #e5e5ea;
  font-size: 14px;
  transition: all 0.2s;
}
.diag-input :deep(.el-textarea__inner:focus) {
  border-color: #007AFF;
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.08);
}
.diag-radio {
  margin-right: 16px;
}

.form-action-bar {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}
.action-btn {
  border-radius: 10px !important;
  min-width: 180px;
  height: 42px !important;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* ===== 诊断结果 ===== */
.disease-row {
  padding: 14px;
  background: #f8f9fc;
  border-radius: 10px;
  margin-bottom: 8px;
}
.disease-row:last-child { margin-bottom: 0; }
.disease-row-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.disease-row-header strong {
  font-size: 15px;
  color: #1C1C1E;
}
.disease-conf {
  margin-left: auto;
  color: #007AFF;
  font-weight: 600;
  font-size: 14px;
}
.disease-row-detail {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}
.disease-row-detail.diff {
  margin-top: 4px;
  padding-top: 4px;
  border-top: 1px dashed #e5e5ea;
}

.result-block {
  margin-bottom: 4px;
}
.block-label {
  font-size: 14px;
  font-weight: 600;
  color: #1C1C1E;
  margin-bottom: 10px;
}
.conclusion-text {
  line-height: 1.8;
  color: #3C3C43;
  font-size: 14px;
  white-space: pre-wrap;
}

/* ===== 侧边患者信息 ===== */
.sidebar-card {
  position: sticky;
  top: 20px;
}
.patient-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.p-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f7;
  font-size: 13px;
}
.p-row.warning .p-val {
  color: #FF3B30;
  font-weight: 500;
}
.p-label {
  color: #8E8E93;
}
.p-val {
  color: #1C1C1E;
  font-weight: 500;
  text-align: right;
  max-width: 60%;
}
.p-divider {
  height: 4px;
}

/* ===== 分析中遮罩 ===== */
.diag-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}
.diag-modal {
  background: #fff;
  border-radius: 16px;
  padding: 48px;
  text-align: center;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}
.modal-spinner {
  margin-bottom: 20px;
}
.spinner-svg {
  animation: spin 1.2s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #1C1C1E;
  margin-bottom: 8px;
}
.modal-desc {
  font-size: 14px;
  color: #8E8E93;
}
</style>
