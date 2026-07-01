<template>
  <div class="payment-page">
    <div class="page-title">支付</div>

    <div v-loading="pageLoading" class="payment-content">
      <!-- 步骤指示 -->
      <div class="steps-bar">
        <div class="step completed">
          <div class="step-circle">✓</div>
          <span class="step-text">确认信息</span>
        </div>
        <div class="step-line" />
        <div class="step active">
          <div class="step-circle">2</div>
          <span class="step-text">支付</span>
        </div>
        <div class="step-line" />
        <div class="step" :class="{ completed: paymentDone }">
          <div class="step-circle">{{ paymentDone ? '✓' : '3' }}</div>
          <span class="step-text">完成</span>
        </div>
      </div>

      <el-row :gutter="24">
        <!-- 支付信息 -->
        <el-col :span="14">
          <div class="cb-card">
            <div class="cb-card-header">
              <el-icon class="header-icon"><InfoFilled /></el-icon>
              <span>预约详情</span>
            </div>
            <div class="cb-card-body">
              <div v-if="appointment" class="appt-detail">
                <div class="detail-row">
                  <span class="detail-label">预约编号</span>
                  <span class="detail-value mono">{{ appointment.appointmentId }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">就诊日期</span>
                  <span class="detail-value">{{ appointment.appointmentDate }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">就诊时段</span>
                  <span class="detail-value">{{ appointment.timeSlotDesc }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">症状</span>
                  <span class="detail-value">{{ appointment.symptoms || '未描述' }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">预约状态</span>
                  <el-tag :type="statusTag(appointment.status)" size="small">{{ statusText(appointment.status) }}</el-tag>
                </div>
                <div class="detail-row">
                  <span class="detail-label">支付状态</span>
                  <el-tag :type="appointment.paymentStatus === 1 ? 'success' : 'danger'" size="small">
                    {{ appointment.paymentStatus === 1 ? '已支付' : '未支付' }}
                  </el-tag>
                </div>
              </div>
              <el-empty v-else-if="!pageLoading" description="未找到预约信息" :image-size="80" />
            </div>
          </div>
        </el-col>

        <!-- 支付操作 -->
        <el-col :span="10">
          <div class="cb-card">
            <div class="cb-card-header">
              <el-icon class="header-icon"><Coin /></el-icon>
              <span>支付方式</span>
            </div>
            <div class="cb-card-body">
              <div v-if="!paymentDone">
                <!-- 费用显示 -->
                <div class="fee-display">
                  <span class="fee-label">应付金额</span>
                  <div class="fee-amount">
                    <span class="fee-symbol">¥</span>
                    <span class="fee-number">{{ appointment?.totalFee || '0' }}</span>
                  </div>
                </div>
                <div class="fee-display" style="margin-top: 8px">
                  <span class="fee-label">钱包余额</span>
                  <span :class="balance < (appointment?.totalFee || 0) ? 'text-red' : 'text-green'" class="balance-text">
                    ￥{{ balance.toFixed(2) }}
                  </span>
                </div>

                <el-divider />

                <!-- 支付方式选择 -->
                <label class="pay-label">选择支付方式</label>
                <div class="pay-methods">
                  <div
                    v-for="method in payMethods"
                    :key="method.value"
                    class="pay-method-item"
                    :class="{ active: selectedMethod === method.value }"
                    @click="selectedMethod = method.value"
                  >
                    <div class="pay-method-icon" :style="{ background: method.color }">
                      <el-icon :size="22"><component :is="method.icon" /></el-icon>
                    </div>
                    <div class="pay-method-info">
                      <div class="pay-method-name">{{ method.label }}</div>
                    </div>
                    <div class="pay-method-check">
                      <el-icon v-if="selectedMethod === method.value" color="#1a7fbf"><CircleCheck /></el-icon>
                    </div>
                  </div>
                </div>

                <el-divider />

                <el-button
                  type="primary"
                  class="pay-btn"
                  :loading="payLoading"
                  @click="handlePay"
                  round
                >
                  立即支付 ¥{{ appointment?.totalFee || '0' }}
                </el-button>
              </div>

              <!-- 支付完成 -->
              <div v-else class="pay-success">
                <div class="success-icon">
                  <el-icon :size="64" color="#52c41a"><CircleCheckFilled /></el-icon>
                </div>
                <h3 class="success-title">支付成功</h3>
                <p class="success-desc">您已成功支付挂号费用</p>
                <div class="success-actions">
                  <el-button type="primary" @click="router.push('/appointment/records')" round>
                    查看预约记录
                  </el-button>
                  <el-button @click="router.push('/dashboard')" round>
                    返回首页
                  </el-button>
                </div>
              </div>
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
import { ElMessage } from 'element-plus'
import {
  InfoFilled, Coin, CircleCheck, CircleCheckFilled,
  CreditCard, Wallet, Shop, Money
} from '@element-plus/icons-vue'
import { getAppointmentDetailApi, createPaymentApi } from '@/api/appointment'
import { walletBalanceApi } from '@/api/wallet'
import { useAppointmentStore } from '@/stores/appointment'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const apptStore = useAppointmentStore()
const userStore = useUserStore()

const appointmentId = route.params.id as string
const appointment = ref<any>(null)
const pageLoading = ref(false)
const payLoading = ref(false)
const selectedMethod = ref(4)
const paymentDone = ref(false)
const balance = ref(0)

const patientId = (userStore.userInfo as any)?.patientId || apptStore.patientId

const payMethods = [
  { value: 4, label: '钱包支付', icon: Money, color: 'linear-gradient(135deg, #1a7fbf, #0d5a8a)' },
  { value: 0, label: '医保卡支付', icon: CreditCard, color: 'linear-gradient(135deg, #1a7fbf, #0d5a8a)' },
  { value: 2, label: '扫码支付', icon: Wallet, color: 'linear-gradient(135deg, #52c41a, #389e0d)' },
  { value: 3, label: '银行卡支付', icon: CreditCard, color: 'linear-gradient(135deg, #faad14, #d48806)' },
  { value: 1, label: '现金支付', icon: Shop, color: 'linear-gradient(135deg, #ff4d4f, #cf1322)' },
]

onMounted(async () => {
  if (!appointmentId) {
    ElMessage.warning('预约ID不存在')
    router.push('/appointment/dept')
    return
  }
  await Promise.all([loadAppointment(), loadBalance()])
})

async function loadAppointment() {
  pageLoading.value = true
  try {
    const res = await getAppointmentDetailApi(appointmentId)
    appointment.value = res.data as any
  } catch {
    appointment.value = null
  } finally {
    pageLoading.value = false
  }
}

async function loadBalance() {
  if (!patientId) return
  try {
    const res = await walletBalanceApi(patientId)
    balance.value = (res.data as any)?.balance ?? 0
  } catch { balance.value = 0 }
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

async function handlePay() {
  if (!appointment.value) return
  if (!apptStore.patientId) {
    ElMessage.warning('患者信息缺失，请重新预约')
    router.push('/appointment/dept')
    return
  }

  payLoading.value = true
  try {
    await createPaymentApi({
      appointmentId: appointment.value.appointmentId,
      patientId: apptStore.patientId,
      paymentMethod: selectedMethod.value
    })
    paymentDone.value = true
    ElMessage.success('支付成功')
    // 刷新预约信息
    await loadAppointment()
  } catch {
    // handled by interceptor
  } finally {
    payLoading.value = false
  }
}
</script>

<style scoped>
.payment-page {
  max-width: 1000px;
}

.header-icon {
  color: var(--cb-primary);
  font-size: 18px;
}

/* 步骤条 */
.steps-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28px;
  padding: 20px;
  background: var(--cb-white);
  border-radius: var(--cb-radius-lg);
  box-shadow: var(--cb-shadow-sm);
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.step-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--cb-font-sm);
  font-weight: 700;
  background: var(--cb-background);
  color: var(--cb-text-placeholder);
  transition: all 0.3s;
}

.step.active .step-circle {
  background: var(--cb-primary);
  color: #fff;
  box-shadow: 0 4px 12px rgba(26, 127, 191, 0.3);
}

.step.completed .step-circle {
  background: var(--cb-success);
  color: #fff;
}

.step-text {
  font-size: var(--cb-font-xs);
  color: var(--cb-text-placeholder);
  white-space: nowrap;
}

.step.active .step-text {
  color: var(--cb-primary);
  font-weight: 600;
}

.step.completed .step-text {
  color: var(--cb-success);
}

.step-line {
  width: 80px;
  height: 2px;
  background: var(--cb-background);
  margin: 0 12px;
  margin-bottom: 24px;
}

/* 预约详情 */
.detail-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--cb-border-light);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  min-width: 80px;
  font-size: var(--cb-font-sm);
  color: var(--cb-text-secondary);
}

.detail-value {
  font-size: var(--cb-font-base);
  color: var(--cb-text-primary);
}

.mono {
  font-family: monospace;
  color: var(--cb-primary);
  font-weight: 600;
}

/* 支付方式 */
.fee-display {
  text-align: center;
  padding: 8px 0;
}

.fee-label {
  font-size: var(--cb-font-sm);
  color: var(--cb-text-secondary);
}

.fee-amount {
  margin-top: 8px;
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
}

.fee-symbol {
  font-size: var(--cb-font-xl);
  color: var(--cb-danger);
}

.fee-number {
  font-size: 40px;
  font-weight: 700;
  color: var(--cb-danger);
  line-height: 1;
}

.pay-label {
  display: block;
  font-size: var(--cb-font-sm);
  font-weight: 600;
  color: var(--cb-text-primary);
  margin-bottom: 12px;
}

.pay-methods {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pay-method-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid var(--cb-border);
  border-radius: var(--cb-radius-md);
  cursor: pointer;
  transition: all 0.2s;
}

.pay-method-item:hover {
  border-color: var(--cb-primary-light);
  background: var(--cb-primary-lighter);
}

.pay-method-item.active {
  border-color: var(--cb-primary);
  background: var(--cb-primary-lighter);
}

.pay-method-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.pay-method-info {
  flex: 1;
}

.pay-method-name {
  font-size: var(--cb-font-base);
  font-weight: 500;
  color: var(--cb-text-primary);
}

.pay-method-check {
  flex-shrink: 0;
}

.pay-btn {
  width: 100%;
  height: 46px;
  font-size: var(--cb-font-lg);
  letter-spacing: 2px;
}

.balance-text {
  font-size: 16px;
  font-weight: 600;
}

.text-green { color: #52c41a; }
.text-red { color: #ff4d4f; }

/* 支付成功 */
.pay-success {
  text-align: center;
  padding: 20px 0 8px;
}

.success-icon {
  margin-bottom: 16px;
}

.success-title {
  font-size: var(--cb-font-2xl);
  color: var(--cb-text-primary);
  margin: 0 0 8px;
}

.success-desc {
  font-size: var(--cb-font-base);
  color: var(--cb-text-secondary);
  margin: 0 0 24px;
}

.success-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
