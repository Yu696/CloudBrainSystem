<template>
  <div class="record-page">
    <div class="page-title">
      <el-button text @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      病历编辑
    </div>

    <div v-loading="loading" class="record-content">
      <el-row :gutter="24">
        <el-col :span="16">
          <div class="cb-card">
            <div class="cb-card-header">
              <el-icon class="header-icon"><Document /></el-icon>
              <span>病历信息</span>
            </div>
            <div class="cb-card-body">
              <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="left">
                <el-form-item label="主诉" prop="chiefComplaint">
                  <el-input v-model="form.chiefComplaint" type="textarea" :rows="3" placeholder="请输入主诉" maxlength="1000" />
                </el-form-item>

                <el-form-item label="现病史">
                  <el-input v-model="form.presentIllness" type="textarea" :rows="3" placeholder="请输入现病史" maxlength="2000" />
                </el-form-item>

                <el-form-item label="既往史">
                  <el-input v-model="form.pastHistory" type="textarea" :rows="2" placeholder="请输入既往史" maxlength="1000" />
                </el-form-item>

                <el-form-item label="个人史">
                  <el-input v-model="form.personalHistory" type="textarea" :rows="2" placeholder="请输入个人史" maxlength="500" />
                </el-form-item>

                <el-form-item label="家族史">
                  <el-input v-model="form.familyHistory" type="textarea" :rows="2" placeholder="请输入家族史" maxlength="500" />
                </el-form-item>

                <el-form-item label="体格检查">
                  <el-input v-model="form.physicalExam" type="textarea" :rows="3" placeholder="请输入体格检查结果" maxlength="1000" />
                </el-form-item>

                <el-form-item label="辅助检查">
                  <el-input v-model="form.auxiliaryExam" type="textarea" :rows="2" placeholder="请输入辅助检查结果" maxlength="1000" />
                </el-form-item>

                <el-form-item label="初步诊断" prop="diagnosis">
                  <el-input v-model="form.diagnosis" type="textarea" :rows="2" placeholder="请输入初步诊断" maxlength="1000" />
                </el-form-item>

                <el-form-item label="治疗意见">
                  <el-input v-model="form.treatmentOpinion" type="textarea" :rows="3" placeholder="请输入治疗意见" maxlength="1000" />
                </el-form-item>

                <el-divider />

                <div class="form-actions">
                  <el-button @click="handleSave" :loading="saving">
                    <el-icon><FolderChecked /></el-icon>保存草稿
                  </el-button>
                  <el-button v-if="currentRecordId && recordStatus === 0" type="warning" @click="goPrescription" plain>
                    <el-icon><FirstAidKit /></el-icon>开具处方
                  </el-button>
                  <el-button v-if="currentRecordId && recordStatus === 0" type="info" @click="goExam" plain>
                    <el-icon><Search /></el-icon>开检查单
                  </el-button>
                  <el-button v-if="!currentRecordId || recordStatus === 0" type="success" @click="handleComplete" :loading="completing">
                    <el-icon><Select /></el-icon>完成病历
                  </el-button>
                </div>
              </el-form>
            </div>
          </div>
        </el-col>

        <el-col :span="8">
          <div class="cb-card tips-card">
            <div class="cb-card-header">
              <el-icon class="header-icon"><InfoFilled /></el-icon>
              <span>患者信息</span>
            </div>
            <div class="cb-card-body">
              <div v-if="patientInfo" class="patient-info-side">
                <div class="info-item"><span class="info-label">患者姓名</span><span class="info-val">{{ patientInfo.name }}</span></div>
                <div class="info-item"><span class="info-label">性别</span><span class="info-val">{{ patientInfo.gender === 1 ? '男' : '女' }}</span></div>
                <div class="info-item"><span class="info-label">联系电话</span><span class="info-val">{{ patientInfo.phone }}</span></div>
                <div class="info-item"><span class="info-label">过敏史</span><span class="info-val warning">{{ patientInfo.allergyHistory || '无' }}</span></div>
              </div>
              <el-empty v-else description="加载中..." :image-size="60" />
            </div>
          </div>
          <div v-if="patientInfo?.patientId && myDoctorId" style="margin-top: 16px">
            <AiAssistPanel
              :patient-id="patientInfo.patientId"
              :doctor-id="myDoctorId"
              :appointment-id="appointmentId"
              @apply="handleAiApply"
            />
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Document, InfoFilled, FolderChecked, FirstAidKit, Search, Select } from '@element-plus/icons-vue'
import { createMedicalRecordApi, updateMedicalRecordApi, completeMedicalRecordApi, getMedicalRecordDetailApi } from '@/api/medical'
import { getAppointmentDetailApi, getMyDoctorInfoApi } from '@/api/appointment'
import { getPatientInfoApi } from '@/api/patient'
import AiAssistPanel from '@/components/ai/AiAssistPanel.vue'
import { useUserStore } from '@/stores/user'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const completing = ref(false)
const currentRecordId = ref('')
const recordStatus = ref(0)
const patientInfo = ref<any>(null)
const appointmentId = route.params.appointmentId as string
const myDoctorId = ref('')

const form = reactive({
  chiefComplaint: '',
  presentIllness: '',
  pastHistory: '',
  personalHistory: '',
  familyHistory: '',
  physicalExam: '',
  auxiliaryExam: '',
  diagnosis: '',
  treatmentOpinion: ''
})

const rules = {
  chiefComplaint: [{ required: true, message: '请输入主诉', trigger: 'blur' }]
}

onMounted(async () => {
  // 恢复之前创建的病历 ID（跨页面导航保留）
  const saved = sessionStorage.getItem(`record_${appointmentId}`)
  if (saved) currentRecordId.value = saved

  await loadAppointmentInfo()
  // 获取当前登录医生的 doctorId
  try {
    const meRes = await getMyDoctorInfoApi()
    myDoctorId.value = (meRes.data as any)?.doctorId || ''
  } catch { /* ignore */ }

  // 如果已有 recordId，尝试回填表单，同时校验病历状态
  if (currentRecordId.value) {
    try {
      const recRes = await getMedicalRecordDetailApi(currentRecordId.value)
      const rec = recRes.data as any
      // 已完成或已归档的病历不再允许编辑，清除缓存
      if (rec.status === 1 || rec.status === 2) {
        currentRecordId.value = ''
        recordStatus.value = rec.status
        sessionStorage.removeItem(`record_${appointmentId}`)
        return
      }
      recordStatus.value = rec.status || 0
      form.chiefComplaint = rec.chiefComplaint || ''
      form.presentIllness = rec.presentIllness || ''
      form.pastHistory = rec.pastHistory || ''
      form.personalHistory = rec.personalHistory || ''
      form.familyHistory = rec.familyHistory || ''
      form.physicalExam = rec.physicalExam || ''
      form.auxiliaryExam = rec.auxiliaryExam || ''
      form.diagnosis = rec.diagnosis || ''
      form.treatmentOpinion = rec.treatmentOpinion || ''
    } catch { /* ignore */ }
  }
})

async function loadAppointmentInfo() {
  loading.value = true
  try {
    const res = await getAppointmentDetailApi(appointmentId)
    const appt = res.data as any
    if (appt?.patientId) {
      try {
        const pRes = await getPatientInfoApi(appt.patientId)
        patientInfo.value = pRes.data
      } catch { /* ignore */ }
    }
  } catch {
    ElMessage.error('获取预约信息失败')
  } finally {
    loading.value = false
  }
}

async function getFormData() {
  return {
    patientId: patientInfo.value?.patientId || '',
    doctorId: myDoctorId.value || userStore.userInfo?.userId || '',
    appointmentId,
    ...form
  }
}

async function handleSave() {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }

  saving.value = true
  try {
    if (currentRecordId.value) {
      await updateMedicalRecordApi({ recordId: currentRecordId.value, ...form })
      ElMessage.success('病历已保存')
    } else {
      const data = await getFormData()
      const res = await createMedicalRecordApi(data)
      const result = res.data as { recordId: string }
      currentRecordId.value = result.recordId
      recordStatus.value = 0
      // 跨页面保留 recordId
      sessionStorage.setItem(`record_${appointmentId}`, result.recordId)
      ElMessage.success('病历已创建')
    }
  } catch { /* handled by interceptor */ }
  finally { saving.value = false }
}

async function handleComplete() {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }

  if (!currentRecordId.value) {
    // 先保存
    saving.value = true
    try {
      const data = await getFormData()
      const res = await createMedicalRecordApi(data)
      currentRecordId.value = (res.data as { recordId: string }).recordId
    } catch { saving.value = false; return }
    saving.value = false
  } else {
    await updateMedicalRecordApi({ recordId: currentRecordId.value, ...form })
  }

  completing.value = true
  try {
    await completeMedicalRecordApi(currentRecordId.value)
    sessionStorage.removeItem(`record_${appointmentId}`)
    ElMessage.success('病历已完成')
    router.push('/doctor/waiting')
  } catch { /* handled by interceptor */ }
  finally { completing.value = false }
}

function goPrescription() {
  router.push(`/doctor/prescription/${currentRecordId.value}`)
}

function goExam() {
  router.push(`/doctor/exam/${currentRecordId.value}`)
}

function handleAiApply(preview: any) {
  if (preview.chiefComplaint) form.chiefComplaint = preview.chiefComplaint
  if (preview.presentIllness) form.presentIllness = preview.presentIllness
  if (preview.pastHistory) form.pastHistory = preview.pastHistory
  if (preview.physicalExam) form.physicalExam = preview.physicalExam
  if (preview.preliminaryDiagnosis) form.diagnosis = preview.preliminaryDiagnosis
  if (preview.treatmentPlan) form.treatmentOpinion = preview.treatmentPlan
  ElMessage.success('AI 生成内容已应用到表单，请核对后保存')
}
</script>

<style scoped>
.record-page {
  max-width: 1100px;
}

.header-icon {
  color: var(--cb-primary);
  font-size: 18px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding-top: 8px;
  flex-wrap: wrap;
}

.form-actions .el-button {
  min-width: 110px;
}

.tips-card {
  position: sticky;
  top: 24px;
}

.patient-info-side .info-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--cb-border-light);
}

.info-label {
  color: var(--cb-text-secondary);
  font-size: var(--cb-font-sm);
}

.info-val {
  color: var(--cb-text-primary);
  font-weight: 500;
  font-size: var(--cb-font-sm);
}

.info-val.warning {
  color: var(--cb-danger);
}
</style>
