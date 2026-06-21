<template>
  <div class="history-page">
    <div class="page-title">已诊列表</div>

    <div v-loading="loading" class="history-content">
      <div v-if="records.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无已诊记录" :image-size="100" />
      </div>

      <div v-else class="history-list">
        <div v-for="item in records" :key="item.recordId" class="history-card">
          <div class="card-left">
            <el-avatar :size="48" class="patient-avatar">
              {{ (item.patientName || '患')[0] }}
            </el-avatar>
            <div class="patient-info">
              <div class="patient-name">{{ item.patientName || '未知患者' }}</div>
              <div class="patient-meta">
                <span>{{ item.patientGender === 1 ? '男' : item.patientGender === 2 ? '女' : '未知' }}</span>
                <el-divider direction="vertical" />
                <span>诊断时间：{{ formatDate(item.diagnosisTime) }}</span>
              </div>
              <div v-if="item.diagnosis" class="patient-diagnosis">
                <el-tag size="small" type="success" effect="plain">诊断：{{ item.diagnosis }}</el-tag>
              </div>
            </div>
          </div>
          <div class="card-right">
            <el-tag :type="statusTag(item.status)" size="small" effect="dark">
              {{ statusText(item.status) }}
            </el-tag>
            <el-button type="primary" plain text @click="viewDetail(item)">
              <el-icon><View /></el-icon>查看
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { View } from '@element-plus/icons-vue'
import { listMedicalRecordsApi } from '@/api/medical'
import { getPatientInfoApi } from '@/api/patient'
import { getMyDoctorInfoApi } from '@/api/appointment'

const router = useRouter()

const records = ref<any[]>([])
const loading = ref(false)

onMounted(async () => {
  await loadHistoryList()
})

async function loadHistoryList() {
  loading.value = true
  try {
    let doctorId = ''
    try {
      const meRes = await getMyDoctorInfoApi()
      doctorId = (meRes.data as any)?.doctorId
    } catch { /* ignore */ }
    if (!doctorId) {
      records.value = []
      return
    }
    const res = await listMedicalRecordsApi(undefined, doctorId)
    const list = (res.data as any[]) || []
    // 只显示已完成的病历
    const completed = list.filter((r: any) => r.status === 1)

    // 补充患者姓名信息
    for (const item of completed) {
      try {
        const pRes = await getPatientInfoApi(item.patientId)
        const patient = pRes.data as any
        item.patientName = patient?.name
        item.patientGender = patient?.gender
      } catch {
        item.patientName = '未知患者'
      }
    }
    records.value = completed
  } catch {
    records.value = []
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr: string): string {
  if (!dateStr) return '--'
  return dateStr.substring(0, 16)
}

function viewDetail(item: any) {
  // 跳转到病历详情页（含完整病历、处方、检查单）
  if (item.recordId) {
    router.push(`/doctor/record-detail/${item.recordId}`)
  }
}

function statusTag(status: number): string {
  const map: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[status] || 'info'
}

function statusText(status: number): string {
  const map: Record<number, string> = { 0: '草稿', 1: '已完成', 2: '已归档' }
  return map[status] || '未知'
}
</script>

<style scoped>
.history-page {
  max-width: 900px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--cb-white);
  border-radius: var(--cb-radius-lg);
  box-shadow: var(--cb-shadow-sm);
  padding: 20px 24px;
  transition: box-shadow 0.25s;
  cursor: pointer;
}

.history-card:hover {
  box-shadow: var(--cb-shadow-md);
}

.card-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.patient-avatar {
  background: linear-gradient(135deg, var(--cb-success), #6ce8a6) !important;
  font-weight: 600;
  font-size: 18px;
  flex-shrink: 0;
}

.patient-name {
  font-size: var(--cb-font-lg);
  font-weight: 600;
  color: var(--cb-text-primary);
}

.patient-meta {
  font-size: var(--cb-font-sm);
  color: var(--cb-text-secondary);
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.patient-diagnosis {
  margin-top: 8px;
}

.card-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}
</style>
