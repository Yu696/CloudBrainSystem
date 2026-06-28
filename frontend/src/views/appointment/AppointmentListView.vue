<template>
  <div class="records-page">
    <div class="page-title">挂号记录</div>

    <!-- 状态筛选 -->
    <div class="search-bar">
      <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 140px" @change="loadRecords">
        <el-option label="全部状态" value="" />
        <el-option label="待支付" :value="0" />
        <el-option label="已支付" :value="1" />
        <el-option label="已完成" :value="2" />
        <el-option label="已取消" :value="3" />
      </el-select>
      <el-button type="primary" @click="router.push('/appointment/dept')">
        <el-icon><Plus /></el-icon>预约挂号
      </el-button>
    </div>

    <!-- 挂号记录列表 -->
    <div v-loading="loading" class="records-list">
      <div v-for="item in filteredRecords" :key="item.appointmentId" class="record-card">
        <div class="record-header">
          <div class="record-doctor">
            <el-avatar :size="44" class="record-avatar">{{ '医' }}</el-avatar>
            <div class="record-doctor-info">
              <div class="record-doctor-name">{{ item.doctorName || '未知医生' }}</div>
              <div class="record-dept-name">{{ item.departmentName || '未知科室' }}</div>
            </div>
          </div>
          <div class="record-status">
            <el-tag :type="statusTag(item.status)" effect="dark" size="small">
              {{ statusText(item.status) }}
            </el-tag>
          </div>
        </div>

        <el-divider style="margin: 12px 0" />

        <div class="record-body">
          <div class="record-info-grid">
            <div class="record-info-item">
              <span class="record-info-label">预约编号</span>
              <span class="record-info-value mono">{{ item.appointmentId }}</span>
            </div>
            <div class="record-info-item">
              <span class="record-info-label">就診日期</span>
              <span class="record-info-value">{{ item.appointmentDate }}</span>
            </div>
            <div class="record-info-item">
              <span class="record-info-label">就诊时段</span>
              <span class="record-info-value">{{ item.timeSlotDesc }}</span>
            </div>
            <div class="record-info-item">
              <span class="record-info-label">挂号费</span>
              <span class="record-info-value fee">¥{{ item.totalFee || '0' }}</span>
            </div>
          </div>
          <div v-if="item.symptoms" class="record-symptoms">
            <span class="record-info-label">症状：</span>
            <span class="symptoms-text">{{ item.symptoms }}</span>
          </div>
          <div v-if="item.cancelReason" class="record-cancel-reason">
            <span class="record-info-label">取消原因：</span>
            <span class="cancel-text">{{ item.cancelReason }}</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div v-if="item.status === 0" class="record-actions">
          <el-button type="primary" size="small" @click="goPay(item.appointmentId)">
            <el-icon><Coin /></el-icon>去支付
          </el-button>
          <el-button size="small" @click="handleCancel(item)">
            <el-icon><Close /></el-icon>取消预约
          </el-button>
        </div>
      </div>

      <el-empty v-if="!loading && filteredRecords.length === 0" description="暂无挂号记录" :image-size="80" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Coin, Close } from '@element-plus/icons-vue'
import { listAppointmentsApi, cancelAppointmentApi } from '@/api/appointment'
import { useAppointmentStore } from '@/stores/appointment'

const router = useRouter()
const apptStore = useAppointmentStore()

const records = ref<any[]>([])
const loading = ref(false)
const statusFilter = ref('')

const filteredRecords = computed(() => {
  if (statusFilter.value === '') return records.value
  return records.value.filter(r => r.status === Number(statusFilter.value))
})

onMounted(async () => {
  await loadRecords()
})

async function loadRecords() {
  if (!apptStore.patientId) {
    records.value = []
    return
  }

  loading.value = true
  try {
    const res = await listAppointmentsApi(apptStore.patientId)
    records.value = (res.data as any[]) || []
  } catch {
    records.value = []
  } finally {
    loading.value = false
  }
}

function statusTag(status: number): string {
  const map: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'primary', 3: 'info', 4: 'danger', 5: 'info' }
  return map[status] || 'info'
}

function statusText(status: number): string {
  const map: Record<number, string> = {
    0: '待支付', 1: '已支付', 2: '已完成',
    3: '已取消', 4: '已退号', 5: '已过号'
  }
  return map[status] || '未知'
}

function goPay(appointmentId: string) {
  router.push(`/appointment/pay/${appointmentId}`)
}

async function handleCancel(item: any) {
  try {
    await ElMessageBox.confirm('确定要取消该预约吗？', '取消确认', {
      confirmButtonText: '确定取消',
      cancelButtonText: '暂不取消',
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    await cancelAppointmentApi({
      appointmentId: item.appointmentId,
      reason: '患者主动取消'
    })
    ElMessage.success('预约已取消')
    await loadRecords()
  } catch {
    // handled by interceptor
  }
}
</script>

<style scoped>
.records-page {
  max-width: 900px;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.record-card {
  background: var(--cb-white);
  border-radius: var(--cb-radius-lg);
  box-shadow: var(--cb-shadow-sm);
  padding: 20px 24px;
  transition: box-shadow 0.25s;
}

.record-card:hover {
  box-shadow: var(--cb-shadow-md);
}

.record-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.record-doctor {
  display: flex;
  align-items: center;
  gap: 14px;
}

.record-avatar {
  background: linear-gradient(135deg, var(--cb-primary), var(--cb-primary-light)) !important;
  font-weight: 600;
  flex-shrink: 0;
}

.record-doctor-name {
  font-size: var(--cb-font-lg);
  font-weight: 600;
  color: var(--cb-text-primary);
}

.record-dept-name {
  font-size: var(--cb-font-xs);
  color: var(--cb-text-secondary);
  margin-top: 2px;
}

.record-status {
  display: flex;
  align-items: center;
}

.record-body {
  padding: 0;
}

.record-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.record-info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.record-info-label {
  font-size: var(--cb-font-xs);
  color: var(--cb-text-placeholder);
}

.record-info-value {
  font-size: var(--cb-font-base);
  color: var(--cb-text-primary);
}

.mono {
  font-family: monospace;
  font-size: var(--cb-font-xs);
  color: var(--cb-primary);
}

.fee {
  color: var(--cb-danger);
  font-weight: 600;
}

.record-symptoms,
.record-cancel-reason {
  margin-top: 8px;
  font-size: var(--cb-font-sm);
}

.symptoms-text {
  color: var(--cb-text-secondary);
}

.cancel-text {
  color: var(--cb-danger);
}

.record-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--cb-border-light);
}
</style>
