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
              <span v-if="currentRecordId" class="record-id-tag">编号：{{ currentRecordId }}</span>
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
                  <el-button type="warning" plain @click="handleAiPreDiagnosis" :loading="diagnosing" :disabled="!patientInfo?.patientId">
                    <el-icon><MagicStick /></el-icon>AI预诊
                  </el-button>
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
          <div class="side-sticky">
            <!-- 患者信息 -->
            <div class="cb-card">
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

            <!-- 处方记录 -->
            <div v-if="prescriptions.length > 0" class="cb-card" style="margin-top:16px">
              <div class="cb-card-header">
                <el-icon class="header-icon"><FirstAidKit /></el-icon>
                <span>处方记录 ({{ prescriptions.length }})</span>
              </div>
              <div class="cb-card-body">
                <div v-for="pres in prescriptions" :key="pres.prescriptionId" class="prescription-block">
                  <div class="pres-header">
                    <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap">
                      <el-tag :type="presStatusTag(pres.status)" size="small">{{ presStatusText(pres.status) }}</el-tag>
                      <span class="pres-date">{{ pres.createTime?.substring(0, 10) }}</span>
                    </div>
                    <div style="display:flex;gap:2px">
                      <el-button size="small" text @click="editPrescription(pres)"><el-icon><Edit /></el-icon></el-button>
                      <el-button size="small" text type="danger" @click="deletePrescriptionItem(pres)"><el-icon><Delete /></el-icon></el-button>
                    </div>
                  </div>
                  <el-table :data="pres.items || []" size="small" border>
                    <el-table-column prop="drugName" label="药品" min-width="80" />
                    <el-table-column prop="dosage" label="剂量" width="70" />
                    <el-table-column prop="frequency" label="频次" width="70" />
                    <el-table-column prop="days" label="天数" width="50" />
                  </el-table>
                  <div v-if="pres.items?.length" class="presc-notes">
                    <template v-for="item in pres.items" :key="item.itemId">
                      <div v-if="item.cautiousCrowd || item.sideEffects" class="presc-note-item">
                        <el-icon><WarningFilled /></el-icon>
                        <span><strong>{{ item.drugName }}：</strong></span>
                        <span v-if="item.cautiousCrowd">禁忌人群：{{ item.cautiousCrowd }}</span>
                        <span v-if="item.cautiousCrowd && item.sideEffects"> | </span>
                        <span v-if="item.sideEffects">不良反应：{{ item.sideEffects }}</span>
                      </div>
                    </template>
                  </div>
                  <div v-if="!pres.items?.length" class="pres-empty">暂无药品明细</div>
                </div>
              </div>
            </div>

            <!-- 检查记录 -->
            <div v-if="examinationOrders.length > 0" class="cb-card" style="margin-top:16px">
              <div class="cb-card-header">
                <el-icon class="header-icon"><Search /></el-icon>
                <span>检查记录 ({{ examinationOrders.length }})</span>
              </div>
              <div class="cb-card-body">
                <div v-for="e in examinationOrders" :key="e.orderId" class="prescription-block">
                  <div class="pres-header">
                    <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap">
                      <el-tag :type="examCategoryText(e.examCategory) === '影像' ? 'primary' : examCategoryText(e.examCategory) === '实验室' ? 'success' : 'warning'" size="small" effect="plain">
                        {{ examCategoryText(e.examCategory) }}
                      </el-tag>
                      <strong>{{ e.examName }}</strong>
                      <el-tag :type="examStatusTag(e.status)" size="small">{{ examStatusText(e.status) }}</el-tag>
                    </div>
                    <div style="display:flex;gap:2px">
                      <el-button size="small" text @click="editExam(e)"><el-icon><Edit /></el-icon></el-button>
                      <el-button size="small" text type="danger" @click="deleteExamItem(e)"><el-icon><Delete /></el-icon></el-button>
                    </div>
                  </div>
                  <!-- 检查结果 -->
                  <div v-if="e._result" class="exam-result-info">
                    <div v-if="e._result.resultData" class="exam-row">
                      <span class="exam-label">结果：</span><span>{{ e._result.resultData }}</span>
                    </div>
                    <div v-if="e._result.doctorOpinion" class="exam-row">
                      <span class="exam-label">意见：</span><span>{{ e._result.doctorOpinion }}</span>
                    </div>
                  </div>
                  <!-- 影像缩略图 -->
                  <div v-if="e._images?.length" class="thumb-list">
                    <div
                      v-for="img in e._images"
                      :key="img.imageId"
                      class="exam-thumb"
                      @click="goImageViewer(img.imageId)"
                    >
                      <img :src="imagePreviewUrl(img.imageId)" :alt="img.imageName" />
                    </div>
                  </div>
                  <div v-else class="pres-empty">暂无影像</div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- AI预诊结果对话框 -->
    <el-dialog v-model="showDiagnosisDialog" title="AI 预诊结果" width="700px">
      <div v-if="diagnosisResult" class="diagnosis-content">
        <div class="diag-overall">
          <el-tag :type="diagnosisResult.needsManualReview ? 'warning' : 'success'" size="large">
            {{ diagnosisResult.needsManualReview ? '建议人工复核' : 'AI 诊断完成' }}
          </el-tag>
          <span class="diag-score">置信度 {{ (diagnosisResult.confidenceScore * 100).toFixed(0) }}%</span>
        </div>
        <div v-if="diagnosisResult.diseaseMatches?.length" class="diag-diseases">
          <div class="diag-section-title">疑似疾病</div>
          <div v-for="(d, idx) in diagnosisResult.diseaseMatches" :key="idx" class="diag-disease-item">
            <div class="diag-disease-header">
              <strong>{{ d.diseaseName }}</strong>
              <el-tag size="small" type="info">{{ d.icdCode }}</el-tag>
              <span class="diag-disease-conf">{{ (d.confidence * 100).toFixed(0) }}%</span>
            </div>
            <div class="diag-disease-basis">诊断依据：{{ d.diagnosisBasis }}</div>
            <div v-if="d.differentialDiagnosis?.length" class="diag-disease-diff">
              鉴别诊断：{{ d.differentialDiagnosis.join('、') }}
            </div>
          </div>
        </div>
        <div class="diag-conclusion">
          <div class="diag-section-title">分析结论</div>
          <p v-if="diagnosisResult.analysisResult">{{ diagnosisResult.analysisResult }}</p>
          <el-empty v-else description="AI 未返回有效的分析结论，请补充更多症状信息后重试" :image-size="60" />
        </div>
      </div>
      <div v-if="fallbackMode && !diagnosisResult" class="diag-fallback">
        <el-alert title="AI 服务暂不可用" type="warning" description="请根据临床经验进行诊断" show-icon :closable="false" />
      </div>
      <template #footer>
        <el-button @click="showDiagnosisDialog = false">关闭</el-button>
        <el-button v-if="diagnosisResult?.diagnosisId" type="primary" @click="goReport">
          查看完整报告
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Document, InfoFilled, FolderChecked, FirstAidKit, Search, Select, MagicStick, Edit, Delete, WarningFilled } from '@element-plus/icons-vue'
import { createMedicalRecordApi, updateMedicalRecordApi, completeMedicalRecordApi, getMedicalRecordDetailApi, listMedicalRecordsApi, listPrescriptionsApi, getPrescriptionDetailApi, listExaminationOrdersApi, getExaminationResultApi, deletePrescriptionApi, deleteExaminationOrderApi } from '@/api/medical'
import { getAppointmentDetailApi, getMyDoctorInfoApi } from '@/api/appointment'
import { getPatientInfoApi } from '@/api/patient'
import { imageListApi, imagePreviewUrl } from '@/api/image'
import { diagnosisApi } from '@/api/ai'
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
const diagnosing = ref(false)
const showDiagnosisDialog = ref(false)
const diagnosisResult = ref<any>(null)
const fallbackMode = ref(false)
const prescriptions = ref<any[]>([])
const examinationOrders = ref<any[]>([])

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

  // 如果 sessionStorage 里没有 recordId，尝试通过预约号查找已有病历
  if (!currentRecordId.value && patientInfo.value?.patientId && myDoctorId.value) {
    try {
      const listRes = await listMedicalRecordsApi(patientInfo.value.patientId, myDoctorId.value)
      const records = (listRes.data as any[]) || []
      const match = records.find((r: any) => r.appointmentId === appointmentId)
      if (match) {
        currentRecordId.value = match.recordId
        sessionStorage.setItem(`record_${appointmentId}`, match.recordId)
      }
    } catch { /* ignore */ }
  }

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
    // 加载已有的处方和检查单
    loadRelatedData()
  }
})

async function loadRelatedData() {
  if (!currentRecordId.value) return
  // 加载处方
  try {
    const presRes = await listPrescriptionsApi(currentRecordId.value)
    const presList = (presRes.data as any[]) || []
    for (const pres of presList) {
      try {
        const detailRes = await getPrescriptionDetailApi(pres.prescriptionId)
        pres.items = (detailRes.data as any)?.items || []
      } catch { pres.items = [] }
    }
    prescriptions.value = presList
  } catch { /* ignore */ }

  // 加载检查单（含结果和影像）
  try {
    const examRes = await listExaminationOrdersApi(currentRecordId.value)
    const examList = (examRes.data as any[]) || []
    for (const e of examList) {
      try {
        const resultRes = await getExaminationResultApi(e.orderId)
        e._result = resultRes.data
      } catch { e._result = null }
      try {
        const imgRes = await imageListApi({ examinationId: e.orderId })
        e._images = (imgRes.data as any)?.records || []
      } catch { e._images = [] }
    }
    examinationOrders.value = examList
  } catch { /* ignore */ }
}

function goImageViewer(imageId: string) {
  router.push(`/image/viewer/${imageId}`)
}

function goPrescription() {
  router.push(`/doctor/prescription/${currentRecordId.value}`)
}

function goExam() {
  router.push(`/doctor/exam/${currentRecordId.value}`)
}

function editPrescription(pres: any) {
  router.push(`/doctor/prescription-edit/${currentRecordId.value}/${pres.prescriptionId}`)
}

async function deletePrescriptionItem(pres: any) {
  try {
    await ElMessageBox.confirm('确定删除该处方吗？', '提示', { type: 'warning' })
  } catch { return }
  try {
    await deletePrescriptionApi(pres.prescriptionId)
    ElMessage.success('处方已删除')
    loadRelatedData()
  } catch { /* handled by interceptor */ }
}

function editExam(order: any) {
  router.push(`/doctor/exam-edit/${currentRecordId.value}/${order.orderId}`)
}

async function deleteExamItem(order: any) {
  try {
    await ElMessageBox.confirm('确定删除该检查单吗？', '提示', { type: 'warning' })
  } catch { return }
  try {
    await deleteExaminationOrderApi(order.orderId)
    ElMessage.success('检查单已删除')
    loadRelatedData()
  } catch { /* handled by interceptor */ }
}

function presStatusTag(status: number): string {
  const map: Record<number, string> = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'primary', 4: 'danger' }
  return map[status] || 'info'
}
function presStatusText(status: number): string {
  const map: Record<number, string> = { 0: '草稿', 1: '待审核', 2: '已审核', 3: '已发药', 4: '已作废' }
  return map[status] || '未知'
}
function examCategoryText(cat: number): string {
  const map: Record<number, string> = { 0: '实验室', 1: '影像', 2: '功能检查' }
  return map[cat] || '--'
}
function examStatusTag(status: number): string {
  const map: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger' }
  return map[status] || 'info'
}
function examStatusText(status: number): string {
  const map: Record<number, string> = { 0: '已开单', 1: '已缴费', 2: '检查中', 3: '已完成', 4: '已取消' }
  return map[status] || '未知'
}

async function loadAppointmentInfo() {
  loading.value = true
  try {
    const res = await getAppointmentDetailApi(appointmentId)
    const appt = res.data as any
    if (appt?.patientId) {
      try {
        const pRes = await getPatientInfoApi(appt.patientId)
        patientInfo.value = pRes.data
        // 从预约和患者档案预填表单
        if (!currentRecordId.value) {
          form.chiefComplaint = appt.symptoms || ''
          const allergies = patientInfo.value?.allergyHistory || ''
          const medical = patientInfo.value?.medicalHistory || ''
          const genetic = patientInfo.value?.geneticDiseases || ''
          const parts: string[] = []
          if (allergies) parts.push(`过敏史：${allergies}`)
          if (medical) parts.push(`既往病史：${medical}`)
          if (genetic) parts.push(`遗传病史：${genetic}`)
          form.pastHistory = parts.join('；')
        }
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

async function handleAiPreDiagnosis() {
  if (!patientInfo.value?.patientId) return
  diagnosing.value = true
  fallbackMode.value = false
  diagnosisResult.value = null
  try {
    const res = await diagnosisApi({
      patientId: patientInfo.value.patientId,
      doctorId: myDoctorId.value || userStore.userInfo?.userId || '',
      diagnosisType: 0,
      symptomData: {
        chiefComplaint: form.chiefComplaint,
        presentIllness: form.presentIllness,
        physicalExam: form.physicalExam,
        auxiliaryExam: form.auxiliaryExam
      }
    })
    diagnosisResult.value = res.data
    if (res.data?.diagnosisId === 'FALLBACK' || !res.data?.analysisResult) {
      fallbackMode.value = true
    }
    showDiagnosisDialog.value = true
  } catch (err: any) {
    fallbackMode.value = true
    showDiagnosisDialog.value = true
  } finally {
    diagnosing.value = false
  }
}

function goReport() {
  if (diagnosisResult.value?.diagnosisId) {
    router.push(`/ai/diagnosis-report/${diagnosisResult.value.diagnosisId}`)
  }
}
</script>

<style scoped>
.record-page {
  max-width: 1100px;
}

.record-id-tag {
  font-size: var(--cb-font-xs);
  color: var(--cb-text-placeholder);
  font-weight: 400;
  margin-right: 8px;
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

.side-sticky {
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

/* AI预诊结果 */
.diagnosis-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.diag-overall {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--cb-background);
  border-radius: var(--cb-radius-md);
}

.diag-score {
  margin-left: auto;
  font-size: var(--cb-font-sm);
  color: var(--cb-text-secondary);
}

.diag-section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--cb-text-primary);
  margin-bottom: 8px;
}

.diag-disease-item {
  padding: 12px;
  background: var(--cb-background);
  border-radius: var(--cb-radius-md);
  margin-bottom: 8px;
}

.diag-disease-item:last-child {
  margin-bottom: 0;
}

.diag-disease-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.diag-disease-header strong {
  font-size: 15px;
  color: var(--cb-text-primary);
}

.diag-disease-conf {
  margin-left: auto;
  color: var(--cb-primary);
  font-weight: 600;
  font-size: 14px;
}

.diag-disease-basis,
.diag-disease-diff {
  font-size: 13px;
  color: var(--cb-text-secondary);
  line-height: 1.6;
}

.diag-disease-diff {
  margin-top: 4px;
  padding-top: 4px;
  border-top: 1px dashed var(--cb-border);
}

.diag-conclusion p {
  color: var(--cb-text-secondary);
  line-height: 1.8;
  font-size: 14px;
  white-space: pre-wrap;
  margin: 0;
}

.diag-fallback {
  padding: 16px 0;
}

.prescription-block {
  background: var(--cb-background);
  border-radius: var(--cb-radius-md);
  padding: 12px 16px;
  margin-bottom: 8px;
}

.pres-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--cb-font-sm);
  color: var(--cb-text-secondary);
}

.pres-date {
  font-size: 12px;
  color: var(--cb-text-placeholder);
}

.pres-empty {
  text-align: center;
  font-size: 12px;
  color: var(--cb-text-placeholder);
  padding: 8px 0;
}

.presc-notes {
  margin-top: 6px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.presc-note-item {
  display: flex;
  align-items: flex-start;
  gap: 4px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--el-color-warning);
  padding: 4px 8px;
  background: var(--el-color-warning-light-9);
  border-radius: var(--cb-radius-sm);
}

/* 检查结果 */
.exam-result-info {
  background: var(--cb-background);
  border-radius: 4px;
  padding: 8px 10px;
  margin: 8px 0;
  font-size: 12px;
}

.exam-row {
  margin-bottom: 4px;
  line-height: 1.6;
}

.exam-row:last-child {
  margin-bottom: 0;
}

.exam-label {
  color: var(--cb-text-secondary);
  font-weight: 500;
}

/* 影像缩略图 */
.thumb-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.exam-thumb {
  width: 64px;
  height: 64px;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid var(--cb-border-light);
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.exam-thumb:hover {
  box-shadow: 0 0 0 2px var(--cb-primary);
}

.exam-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
</style>
