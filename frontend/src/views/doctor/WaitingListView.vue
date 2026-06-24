<template>
  <div class="waiting-page">
    <div class="page-title">待诊列表</div>

    <div v-loading="loading" class="waiting-content">
      <div v-if="appointments.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无待诊患者" :image-size="100" />
      </div>

      <div v-else class="waiting-list">
        <div v-for="item in appointments" :key="item.appointmentId" class="waiting-card">
          <div class="card-left">
            <el-avatar :size="48" class="patient-avatar">
              {{ (item.patientName || '患')[0] }}
            </el-avatar>
            <div class="patient-info">
              <div class="patient-name">{{ item.patientName || '未知患者' }}</div>
              <div class="patient-meta">
                <span>{{ item.patientGender === 1 ? '男' : item.patientGender === 2 ? '女' : '未知' }}</span>
                <el-divider direction="vertical" />
                <span>{{ item.appointmentDate }}</span>
                <el-divider direction="vertical" />
                <span>{{ item.timeSlotDesc }}</span>
              </div>
              <div v-if="item.symptoms" class="patient-symptoms">
                <el-tag size="small" type="warning" effect="plain">主诉：{{ item.symptoms }}</el-tag>
              </div>
            </div>
          </div>
          <div class="card-right">
            <el-tag :type="statusTag(item.status)" size="small" effect="dark">
              {{ statusText(item.status) }}
            </el-tag>
            <el-button type="primary" @click="handleReceive(item)" :disabled="item.status !== 1">
              <el-icon><Edit /></el-icon>接诊
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
import { Edit } from '@element-plus/icons-vue'
import { listAppointmentsApi, getMyDoctorInfoApi } from '@/api/appointment'
import { getPatientInfoApi } from '@/api/patient'

const router = useRouter()

const appointments = ref<any[]>([])
const loading = ref(false)

onMounted(async () => {
  await loadWaitingList()
})

async function loadWaitingList() {
  loading.value = true
  try {
    // 当前医生查看自己的预约列表 — 先查自己的 doctorId
    let doctorId = ''
    try {
      const meRes = await getMyDoctorInfoApi()
      doctorId = (meRes.data as any)?.doctorId
    } catch { /* 不是医生则返回空 */ }
    if (!doctorId) {
      appointments.value = []
      return
    }
    const res = await listAppointmentsApi(undefined, doctorId)
    const list = (res.data as any[]) || []
    // 筛选已支付待就诊的预约（只显示未完成的）
    const waiting = list.filter((a: any) => a.status === 1)

    // 补充患者姓名信息
    for (const item of waiting) {
      try {
        const pRes = await getPatientInfoApi(item.patientId)
        const patient = pRes.data as any
        item.patientName = patient?.name
        item.patientGender = patient?.gender
      } catch {
        item.patientName = '未知患者'
      }
    }
    appointments.value = waiting
  } catch {
    appointments.value = []
  } finally {
    loading.value = false
  }
}

function statusTag(status: number): string {
  const map: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'primary', 3: 'info', 4: 'danger' }
  return map[status] || 'info'
}

function statusText(status: number): string {
  const map: Record<number, string> = { 0: '待支付', 1: '已支付', 2: '已完成', 3: '已取消', 4: '已退号' }
  return map[status] || '未知'
}

async function handleReceive(item: any) {
  router.push(`/doctor/record/${item.appointmentId}`)
}
</script>

<style scoped>
.waiting-page {
  max-width: 900px;
}

.waiting-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.waiting-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--cb-white);
  border-radius: var(--cb-radius-lg);
  box-shadow: var(--cb-shadow-sm);
  padding: 20px 24px;
  transition: box-shadow 0.25s;
}

.waiting-card:hover {
  box-shadow: var(--cb-shadow-md);
}

.card-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.patient-avatar {
  background: linear-gradient(135deg, var(--cb-primary), var(--cb-primary-light)) !important;
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

.patient-symptoms {
  margin-top: 8px;
}

.card-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}
</style>
