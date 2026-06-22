<template>
  <div class="patient-record-page">
    <div class="page-title">我的病历</div>

    <div v-loading="loading">
      <!-- 病历列表 -->
      <div v-if="records.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无病历记录" :image-size="100" />
      </div>

      <div v-else class="records-list">
        <el-collapse v-model="activeRecords" accordion>
          <el-collapse-item v-for="record in records" :key="record.recordId" :name="record.recordId">
            <template #title>
              <div class="collapse-title">
                <span class="record-date">{{ formatDate(record.createTime) }}</span>
                <el-tag :type="record.status === 1 ? 'success' : 'warning'" size="small" effect="plain">
                  {{ record.status === 1 ? '已完成' : record.status === 0 ? '草稿' : '已归档' }}
                </el-tag>
                <span class="record-diagnosis-short">{{ record.diagnosis || '待诊断' }}</span>
              </div>
            </template>

            <div class="record-detail">
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="病历编号">{{ record.recordId }}</el-descriptions-item>
                <el-descriptions-item label="就诊时间">{{ record.diagnosisTime || '--' }}</el-descriptions-item>
                <el-descriptions-item label="主诉" :span="2">{{ record.chiefComplaint || '--' }}</el-descriptions-item>
                <el-descriptions-item label="现病史" :span="2">{{ record.presentIllness || '--' }}</el-descriptions-item>
                <el-descriptions-item label="既往史" :span="2">{{ record.pastHistory || '--' }}</el-descriptions-item>
                <el-descriptions-item label="体格检查" :span="2">{{ record.physicalExam || '--' }}</el-descriptions-item>
                <el-descriptions-item label="诊断" :span="2">
                  <strong>{{ record.diagnosis || '待诊断' }}</strong>
                </el-descriptions-item>
                <el-descriptions-item label="治疗意见" :span="2">{{ record.treatmentOpinion || '--' }}</el-descriptions-item>
              </el-descriptions>

              <!-- 处方列表 -->
              <div v-if="record.prescriptions && record.prescriptions.length > 0" class="sub-section">
                <div class="sub-title">处方记录</div>
                <div v-for="pres in record.prescriptions" :key="pres.prescriptionId" class="prescription-card">
                  <div class="pres-header">
                    <span>处方编号：{{ pres.prescriptionId }}</span>
                    <el-tag :type="presStatusTag(pres.status)" size="small">{{ presStatusText(pres.status) }}</el-tag>
                  </div>
                  <el-table :data="pres.items || []" size="small" border style="margin-top: 8px">
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

              <!-- 检查单列表 -->
              <div v-if="record.examinationOrders && record.examinationOrders.length > 0" class="sub-section">
                <div class="sub-title">检查记录</div>
                <div v-for="exam in record.examinationOrders" :key="exam.orderId" class="exam-item">
                  <span>{{ exam.examName }}</span>
                  <el-tag :type="examStatusTag(exam.status)" size="small">{{ examStatusText(exam.status) }}</el-tag>
                </div>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listMedicalRecordsApi, listPrescriptionsApi, getPrescriptionDetailApi, listExaminationOrdersApi } from '@/api/medical'
import { findPatientByUserIdApi } from '@/api/patient'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const records = ref<any[]>([])
const activeRecords = ref('')
const patientId = ref('')

onMounted(async () => {
  await loadRecords()
})

async function loadRecords() {
  loading.value = true
  try {
    // 通过当前登录用户的 userId 查找患者档案
    const userId = userStore.userInfo?.userId
    if (!userId) {
      records.value = []
      return
    }
    try {
      const patRes = await findPatientByUserIdApi(userId)
      patientId.value = (patRes.data as any)?.patientId
    } catch {
      patientId.value = ''
    }
    if (!patientId.value) {
      records.value = []
      return
    }
    const res = await listMedicalRecordsApi(patientId.value)
    const list = (res.data as any[]) || []

    // 加载每个病历的处方和检查单
    for (const record of list) {
      try {
        const presRes = await listPrescriptionsApi(record.recordId)
        const presList = (presRes.data as any[]) || []
        for (const pres of presList) {
          try {
            const detailRes = await getPrescriptionDetailApi(pres.prescriptionId)
            pres.items = (detailRes.data as any)?.items || []
          } catch { pres.items = [] }
        }
        record.prescriptions = presList
      } catch { record.prescriptions = [] }

      try {
        const examRes = await listExaminationOrdersApi(record.recordId)
        record.examinationOrders = (examRes.data as any[]) || []
      } catch { record.examinationOrders = [] }
    }

    records.value = list
  } catch {
    records.value = []
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr: string): string {
  if (!dateStr) return '--'
  return dateStr.substring(0, 10)
}

function presStatusTag(status: number): string {
  const map: Record<number, string> = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'primary', 4: 'danger' }
  return map[status] || 'info'
}

function presStatusText(status: number): string {
  const map: Record<number, string> = { 0: '草稿', 1: '待审核', 2: '已审核', 3: '已发药', 4: '已作废' }
  return map[status] || '未知'
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
.patient-record-page {
  max-width: 900px;
}

.collapse-title {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.record-date {
  color: var(--cb-text-secondary);
  font-size: var(--cb-font-sm);
}

.record-diagnosis-short {
  color: var(--cb-text-primary);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-detail {
  padding: 8px 0;
}

.sub-section {
  margin-top: 16px;
}

.sub-title {
  font-weight: 600;
  color: var(--cb-text-primary);
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 2px solid var(--cb-primary-lighter);
  font-size: var(--cb-font-base);
}

.prescription-card {
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

.exam-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: var(--cb-background);
  border-radius: var(--cb-radius-sm);
  margin-bottom: 4px;
  font-size: var(--cb-font-sm);
}
</style>
