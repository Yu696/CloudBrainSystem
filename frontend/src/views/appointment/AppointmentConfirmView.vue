<template>
  <div class="confirm-page">
    <div class="page-title">确认预约</div>

    <div v-loading="bookingLoading" class="confirm-content">
      <!-- 预约信息卡片 -->
      <el-row :gutter="24">
        <el-col :span="16">
          <div class="cb-card">
            <div class="cb-card-header">
              <el-icon class="header-icon"><Edit /></el-icon>
              <span>预约信息</span>
            </div>
            <div class="cb-card-body">
              <!-- 就诊信息概览 -->
              <div class="info-summary">
                <div class="summary-item">
                  <span class="summary-label">科室</span>
                  <span class="summary-value">{{ deptName }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">医生</span>
                  <span class="summary-value">{{ doctorName }}</span>
                  <span class="summary-tag">{{ doctorTitle }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">就诊日期</span>
                  <span class="summary-value">{{ date }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">就诊时段</span>
                  <span class="summary-value">{{ slotTime }}</span>
                </div>
              </div>

              <el-divider />

              <!-- 患者信息 -->
              <div class="patient-section">
                <label class="section-label">患者信息</label>
                <div v-if="patientId" class="patient-info-display">
                  <div class="patient-id-row">
                    <span class="patient-id-label">患者编号</span>
                    <span class="patient-id-value">{{ patientId }}</span>
                  </div>
                </div>
                <div v-else class="patient-input-area">
                  <el-input v-model="patientIdInput" placeholder="请输入患者编号" class="patient-input" />
                  <el-button type="primary" size="small" @click="setPatient">确认</el-button>
                </div>
              </div>

              <el-divider />

              <!-- 症状描述 -->
              <div class="symptoms-section">
                <label class="section-label">症状描述 <span class="optional">（选填）</span></label>
                <el-input
                  v-model="symptoms"
                  type="textarea"
                  :rows="4"
                  placeholder="请简要描述您的症状，如：头痛、发烧、咳嗽等"
                  maxlength="500"
                  show-word-limit
                />
              </div>
            </div>
          </div>
        </el-col>

        <!-- 右侧费用概览 -->
        <el-col :span="8">
          <div class="cb-card fee-card">
            <div class="cb-card-header">
              <el-icon class="header-icon"><Coin /></el-icon>
              <span>费用概览</span>
            </div>
            <div class="cb-card-body">
              <div class="fee-row">
                <span>挂号费</span>
                <span>¥{{ fee || '0' }}</span>
              </div>
              <div class="fee-row">
                <span>诊查费</span>
                <span>免费</span>
              </div>
              <el-divider />
              <div class="fee-total">
                <span>合计</span>
                <span class="total-amount">¥{{ fee || '0' }}</span>
              </div>
              <el-divider />
              <div class="fee-notice">
                <el-alert
                  title="预约后请在 15 分钟内完成支付，超时自动取消"
                  type="warning"
                  :closable="false"
                  show-icon
                  size="small"
                />
              </div>
              <el-button
                type="primary"
                class="book-btn"
                :disabled="!patientId"
                @click="handleBook"
                round
              >
                确认挂号
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Coin } from '@element-plus/icons-vue'
import { bookAppointmentApi } from '@/api/appointment'
import { useAppointmentStore } from '@/stores/appointment'

const router = useRouter()
const route = useRoute()
const apptStore = useAppointmentStore()

const doctorId = route.query.doctorId as string
const doctorName = route.query.doctorName as string
const deptId = route.query.deptId as string
const deptName = route.query.deptName as string
const slotId = route.query.slotId as string
const slotTime = route.query.slotTime as string
const date = route.query.date as string
const fee = route.query.fee as string
const doctorTitle = route.query.doctorTitle as string

const patientId = ref(apptStore.patientId)
const patientIdInput = ref('')
const symptoms = ref('')
const bookingLoading = ref(false)

onMounted(() => {
  if (!doctorId || !slotId) {
    ElMessage.warning('请重新选择医生和时段')
    router.push('/appointment/dept')
  }
})

function setPatient() {
  if (!patientIdInput.value.trim()) {
    ElMessage.warning('请输入患者编号')
    return
  }
  patientId.value = patientIdInput.value.trim()
  apptStore.setPatientId(patientId.value)
}

async function handleBook() {
  if (!patientId.value) {
    ElMessage.warning('请先确认患者信息')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认挂号：${doctorName} | ${date} ${slotTime}，费用 ¥${fee || '0'}`,
      '挂号确认',
      {
        confirmButtonText: '确认挂号',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
  } catch {
    return
  }

  bookingLoading.value = true
  try {
    const res = await bookAppointmentApi({
      patientId: patientId.value,
      doctorId,
      departmentId: deptId,
      slotId,
      symptoms: symptoms.value || undefined,
      appointmentType: 0
    })
    const appointment = res.data as any
    ElMessage.success('挂号成功，请完成支付')
    router.push(`/appointment/pay/${appointment.appointmentId}`)
  } catch {
    // handled by interceptor
  } finally {
    bookingLoading.value = false
  }
}
</script>

<style scoped>
.confirm-page {
  max-width: 1100px;
}

.header-icon {
  color: var(--cb-primary);
  font-size: 18px;
}

/* 信息概览 */
.info-summary {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: var(--cb-font-base);
}

.summary-label {
  color: var(--cb-text-secondary);
  min-width: 80px;
}

.summary-value {
  color: var(--cb-text-primary);
  font-weight: 500;
}

.summary-tag {
  font-size: var(--cb-font-xs);
  color: var(--cb-text-placeholder);
}

/* 患者信息 */
.patient-section {
  margin: 4px 0;
}

.section-label {
  display: block;
  font-size: var(--cb-font-sm);
  font-weight: 600;
  color: var(--cb-text-primary);
  margin-bottom: 12px;
}

.patient-info-display {
  padding: 12px 16px;
  background: var(--cb-primary-lighter);
  border-radius: var(--cb-radius-md);
}

.patient-id-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.patient-id-label {
  font-size: var(--cb-font-sm);
  color: var(--cb-text-secondary);
}

.patient-id-value {
  font-size: var(--cb-font-base);
  color: var(--cb-primary);
  font-weight: 600;
  font-family: monospace;
}

.patient-input-area {
  display: flex;
  gap: 12px;
}

.patient-input {
  flex: 1;
}

.optional {
  color: var(--cb-text-placeholder);
  font-weight: 400;
}

/* 症状输入 */
.symptoms-section {
  margin: 4px 0;
}

/* 费用卡片 */
.fee-card {
  position: sticky;
  top: 24px;
}

.fee-row {
  display: flex;
  justify-content: space-between;
  font-size: var(--cb-font-base);
  color: var(--cb-text-secondary);
  padding: 8px 0;
}

.fee-total {
  display: flex;
  justify-content: space-between;
  font-size: var(--cb-font-lg);
  font-weight: 600;
  color: var(--cb-text-primary);
}

.total-amount {
  color: var(--cb-danger);
  font-size: var(--cb-font-xl);
}

.fee-notice {
  margin: 4px 0 16px;
}

.book-btn {
  width: 100%;
  height: 44px;
  font-size: var(--cb-font-lg);
  letter-spacing: 2px;
  margin-top: 8px;
}
</style>
