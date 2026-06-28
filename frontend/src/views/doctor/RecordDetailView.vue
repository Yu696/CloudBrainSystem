<template>
  <div class="record-detail-page">
    <div class="page-title">
      <el-button text @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      病历详情
      <div style="margin-left:auto">
        <el-button v-if="!editing" type="primary" plain @click="startEdit">
          <el-icon><Edit /></el-icon>编辑
        </el-button>
        <el-button v-else type="info" plain @click="cancelEdit">
          <el-icon><Close /></el-icon>取消
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="detail-content">
      <el-empty v-if="!record && !loading" description="病历不存在" :image-size="100" />

      <div v-else class="detail-body">
        <!-- 病历信息 -->
        <div class="cb-card">
          <div class="cb-card-header">
            <el-icon class="header-icon"><Document /></el-icon>
            <span>病历信息</span>
            <el-tag :type="record.status === 1 ? 'success' : 'warning'" size="small" style="margin-left:12px">
              {{ record.status === 1 ? '已完成' : record.status === 0 ? '草稿' : '已归档' }}
            </el-tag>
          </div>
          <div class="cb-card-body">
            <!-- 查看模式 -->
            <el-descriptions v-if="!editing" :column="2" border size="small">
              <el-descriptions-item label="病历编号">{{ record.recordId }}</el-descriptions-item>
              <el-descriptions-item label="患者ID">{{ record.patientId }}</el-descriptions-item>
              <el-descriptions-item label="诊断时间">{{ formatDate(record.diagnosisTime) }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ formatDate(record.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="主诉" :span="2">{{ record.chiefComplaint || '--' }}</el-descriptions-item>
              <el-descriptions-item label="现病史" :span="2">{{ record.presentIllness || '--' }}</el-descriptions-item>
              <el-descriptions-item label="既往史" :span="2">{{ record.pastHistory || '--' }}</el-descriptions-item>
              <el-descriptions-item label="个人史" :span="2">{{ record.personalHistory || '--' }}</el-descriptions-item>
              <el-descriptions-item label="家族史" :span="2">{{ record.familyHistory || '--' }}</el-descriptions-item>
              <el-descriptions-item label="体格检查" :span="2">{{ record.physicalExam || '--' }}</el-descriptions-item>
              <el-descriptions-item label="辅助检查" :span="2">{{ record.auxiliaryExam || '--' }}</el-descriptions-item>
              <el-descriptions-item label="诊断" :span="2">
                <strong>{{ record.diagnosis || '待诊断' }}</strong>
              </el-descriptions-item>
              <el-descriptions-item label="治疗意见" :span="2">{{ record.treatmentOpinion || '--' }}</el-descriptions-item>
            </el-descriptions>

            <!-- 编辑模式 -->
            <el-form v-else ref="formRef" :model="form" label-width="100px" label-position="left">
              <el-form-item label="主诉">
                <el-input v-model="form.chiefComplaint" type="textarea" :rows="3" maxlength="1000" />
              </el-form-item>
              <el-form-item label="现病史">
                <el-input v-model="form.presentIllness" type="textarea" :rows="3" maxlength="2000" />
              </el-form-item>
              <el-form-item label="既往史">
                <el-input v-model="form.pastHistory" type="textarea" :rows="2" maxlength="1000" />
              </el-form-item>
              <el-form-item label="个人史">
                <el-input v-model="form.personalHistory" type="textarea" :rows="2" maxlength="500" />
              </el-form-item>
              <el-form-item label="家族史">
                <el-input v-model="form.familyHistory" type="textarea" :rows="2" maxlength="500" />
              </el-form-item>
              <el-form-item label="体格检查">
                <el-input v-model="form.physicalExam" type="textarea" :rows="3" maxlength="1000" />
              </el-form-item>
              <el-form-item label="辅助检查">
                <el-input v-model="form.auxiliaryExam" type="textarea" :rows="2" maxlength="1000" />
              </el-form-item>
              <el-form-item label="诊断">
                <el-input v-model="form.diagnosis" type="textarea" :rows="2" maxlength="1000" />
              </el-form-item>
              <el-form-item label="治疗意见">
                <el-input v-model="form.treatmentOpinion" type="textarea" :rows="3" maxlength="1000" />
              </el-form-item>
              <el-button type="primary" @click="handleSave" :loading="saving" style="width:100%">
                <el-icon><Select /></el-icon>保存修改
              </el-button>
            </el-form>
          </div>
        </div>

        <!-- 处方 -->
        <div v-if="prescriptions.length > 0" class="cb-card" style="margin-top:16px">
          <div class="cb-card-header">
            <el-icon class="header-icon"><FirstAidKit /></el-icon>
            <span>处方记录</span>
          </div>
          <div class="cb-card-body">
            <div v-for="pres in prescriptions" :key="pres.prescriptionId" class="prescription-block">
              <div class="pres-header">
                <div style="display:flex;align-items:center;gap:12px">
                  <span>处方编号：{{ pres.prescriptionId }}</span>
                  <el-tag :type="presStatusTag(pres.status)" size="small">{{ presStatusText(pres.status) }}</el-tag>
                </div>
                <div style="display:flex;gap:4px">
                  <el-button type="warning" size="small" text @click="editPrescription(pres)">
                    <el-icon><Edit /></el-icon>修改
                  </el-button>
                  <el-button type="danger" size="small" text @click="deletePrescription(pres)">
                    <el-icon><Delete /></el-icon>删除
                  </el-button>
                </div>
              </div>
              <el-table :data="pres.items || []" size="small" border style="margin-top:8px">
                <el-table-column prop="drugName" label="药品名称" />
                <el-table-column prop="spec" label="规格" width="100" />
                <el-table-column prop="dosage" label="剂量" width="80" />
                <el-table-column prop="frequency" label="频次" width="100" />
                <el-table-column prop="days" label="天数" width="60" />
                <el-table-column prop="quantity" label="数量" width="60" />
                <el-table-column prop="subtotal" label="小计" width="80">
                  <template #default="{ row }">¥{{ row.subtotal }}</template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </div>

        <!-- 检查单 -->
        <div v-if="examinationOrders.length > 0" class="cb-card" style="margin-top:16px">
          <div class="cb-card-header">
            <el-icon class="header-icon"><Search /></el-icon>
            <span>检查记录</span>
          </div>
          <div class="cb-card-body">
            <el-table :data="examinationOrders" size="small" border>
              <el-table-column prop="examName" label="检查项目" />
              <el-table-column label="类别" width="80">
                <template #default="{ row }">{{ examCategoryText(row.examCategory) }}</template>
              </el-table-column>
              <el-table-column label="费用" width="100">
                <template #default="{ row }">¥{{ row.amount || 0 }}</template>
              </el-table-column>
              <el-table-column label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="examStatusTag(row.status)" size="small">{{ examStatusText(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" v-if="!editing">
                <template #default="{ row }">
                  <el-button size="small" text type="warning" @click="editExam(row)">修改</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 开处方 / 开检查单 快捷入口 -->
        <div v-if="!editing" class="action-bar">
          <el-button type="warning" plain @click="goPrescription">
            <el-icon><FirstAidKit /></el-icon>开具处方
          </el-button>
          <el-button type="info" plain @click="goExam">
            <el-icon><Search /></el-icon>开检查单
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Document, FirstAidKit, Search, Edit, Close, Select, Delete } from '@element-plus/icons-vue'
import { getMedicalRecordDetailApi, updateMedicalRecordApi, listPrescriptionsApi, getPrescriptionDetailApi, listExaminationOrdersApi, deletePrescriptionApi } from '@/api/medical'

const router = useRouter()
const route = useRoute()
const recordId = route.params.recordId as string

const loading = ref(false)
const editing = ref(false)
const saving = ref(false)
const record = ref<any>(null)
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

onMounted(async () => {
  loading.value = true
  try {
    await loadData()
  } finally {
    loading.value = false
  }
})

async function loadData() {
  const res = await getMedicalRecordDetailApi(recordId)
  record.value = res.data

  // 加载处方
  try {
    const presRes = await listPrescriptionsApi(recordId)
    const presList = (presRes.data as any[]) || []
    for (const pres of presList) {
      try {
        const detailRes = await getPrescriptionDetailApi(pres.prescriptionId)
        pres.items = (detailRes.data as any)?.items || []
      } catch { pres.items = [] }
    }
    prescriptions.value = presList
  } catch { /* ignore */ }

  // 加载检查单
  try {
    const examRes = await listExaminationOrdersApi(recordId)
    examinationOrders.value = (examRes.data as any[]) || []
  } catch { /* ignore */ }
}

function startEdit() {
  form.chiefComplaint = record.value?.chiefComplaint || ''
  form.presentIllness = record.value?.presentIllness || ''
  form.pastHistory = record.value?.pastHistory || ''
  form.personalHistory = record.value?.personalHistory || ''
  form.familyHistory = record.value?.familyHistory || ''
  form.physicalExam = record.value?.physicalExam || ''
  form.auxiliaryExam = record.value?.auxiliaryExam || ''
  form.diagnosis = record.value?.diagnosis || ''
  form.treatmentOpinion = record.value?.treatmentOpinion || ''
  editing.value = true
}

function cancelEdit() {
  editing.value = false
}

async function handleSave() {
  saving.value = true
  try {
    await updateMedicalRecordApi({ recordId, ...form })
    ElMessage.success('病历已更新')
    editing.value = false
    // 刷新数据
    await loadData()
  } catch { /* handled by interceptor */ }
  finally { saving.value = false }
}

function editPrescription(pres: any) {
  router.push(`/doctor/prescription-edit/${recordId}/${pres.prescriptionId}`)
}

async function deletePrescription(pres: any) {
  try {
    await ElMessageBox.confirm('确定删除该处方吗？处方明细将一并删除。', '提示', { type: 'warning' })
  } catch { return }
  try {
    await deletePrescriptionApi(pres.prescriptionId)
    ElMessage.success('处方已删除')
    // 刷新处方列表
    const presRes = await listPrescriptionsApi(recordId)
    const presList = (presRes.data as any[]) || []
    for (const p of presList) {
      try {
        const detailRes = await getPrescriptionDetailApi(p.prescriptionId)
        p.items = (detailRes.data as any)?.items || []
      } catch { p.items = [] }
    }
    prescriptions.value = presList
  } catch { /* handled by interceptor */ }
}

function goPrescription() {
  router.push(`/doctor/prescription/${recordId}`)
}

function editExam(order: any) {
  router.push(`/doctor/exam-edit/${recordId}/${order.orderId}`)
}

function goExam() {
  router.push(`/doctor/exam/${recordId}`)
}

function formatDate(dateStr: string): string {
  if (!dateStr) return '--'
  return dateStr.substring(0, 16)
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
</script>

<style scoped>
.record-detail-page {
  max-width: 900px;
}

.header-icon {
  color: var(--cb-primary);
  font-size: 18px;
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

.action-bar {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 16px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
