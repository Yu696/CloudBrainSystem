<template>
  <div class="pay-page">
    <div class="page-title">检查费支付</div>

    <div v-loading="loading" class="pay-content">
      <!-- 步骤条 -->
      <div class="steps-bar">
        <div class="step" :class="{ completed: step > 1, active: step === 1 }">
          <div class="step-circle">{{ step > 1 ? '✓' : '1' }}</div>
          <span class="step-text">确认信息</span>
        </div>
        <div class="step-line" :class="{ done: step > 1 }" />
        <div class="step" :class="{ completed: step > 2, active: step === 2 }">
          <div class="step-circle">{{ step > 2 ? '✓' : '2' }}</div>
          <span class="step-text">支付</span>
        </div>
        <div class="step-line" :class="{ done: step > 2 }" />
        <div class="step" :class="{ active: step === 3 }">
          <div class="step-circle">3</div>
          <span class="step-text">完成</span>
        </div>
      </div>

      <el-row :gutter="24">
        <!-- 检查单详情 -->
        <el-col :span="14">
          <div class="cb-card">
            <div class="cb-card-header">
              <el-icon class="header-icon"><InfoFilled /></el-icon>
              <span>检查单详情</span>
            </div>
            <div class="cb-card-body">
              <div v-if="order" class="detail">
                <div class="detail-row">
                  <span class="detail-label">检查单编号</span>
                  <span class="detail-value mono">{{ order.orderId }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">检查项目</span>
                  <span class="detail-value">{{ order.examName }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">检查类别</span>
                  <span class="detail-value">{{ examCategoryText(order.examCategory) }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">检查目的</span>
                  <span class="detail-value">{{ order.examPurpose || '未描述' }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">费用</span>
                  <span class="detail-value" style="font-weight: 700; color: #e6a23c">￥{{ order.amount }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">状态</span>
                  <el-tag :type="order.status === 0 ? 'warning' : 'success'" size="small">
                    {{ order.status === 0 ? '待支付' : '已缴费' }}
                  </el-tag>
                </div>
              </div>
              <el-empty v-else-if="!loading" description="检查单不存在" :image-size="80" />
            </div>
          </div>
        </el-col>

        <!-- 支付 -->
        <el-col :span="10">
          <div class="cb-card">
            <div class="cb-card-header">
              <el-icon class="header-icon"><Coin /></el-icon>
              <span>钱包支付</span>
            </div>
            <div class="cb-card-body">
              <div v-if="step === 1 && order">
                <div class="fee-display">
                  <span class="fee-label">应付金额</span>
                  <div class="fee-amount">
                    <span class="fee-symbol">¥</span>
                    <span class="fee-number">{{ order.amount }}</span>
                  </div>
                </div>
                <div class="fee-display" style="margin-top: 8px">
                  <span class="fee-label">钱包余额</span>
                  <span :class="balance < order.amount ? 'text-red' : 'text-green'" style="font-size: 16px; font-weight: 600">
                    ￥{{ balance.toFixed(2) }}
                  </span>
                </div>
                <el-divider />
                <el-button type="primary" class="pay-btn" :loading="paying" @click="step = 2" round>
                  确认支付 ¥{{ order.amount }}
                </el-button>
              </div>

              <!-- 确认 -->
              <div v-else-if="step === 2 && order">
                <div class="fee-display">
                  <span class="fee-label">将从钱包扣除</span>
                  <div class="fee-amount">
                    <span class="fee-symbol">¥</span>
                    <span class="fee-number">{{ order.amount }}</span>
                  </div>
                </div>
                <el-divider />
                <el-button type="primary" class="pay-btn" :loading="paying" @click="handlePay" round>
                  立即支付
                </el-button>
                <el-button class="pay-btn" style="margin-top: 8px" @click="step = 1" round>返回</el-button>
              </div>

              <!-- 完成 -->
              <div v-else-if="step === 3" class="pay-success">
                <div class="success-icon">
                  <el-icon :size="64" color="#52c41a"><CircleCheckFilled /></el-icon>
                </div>
                <h3 class="success-title">支付成功</h3>
                <p class="success-desc">检查费已从钱包扣除</p>
                <div class="success-actions">
                  <el-button type="primary" @click="router.push('/wallet')" round>返回钱包</el-button>
                  <el-button @click="router.push('/dashboard')" round>返回首页</el-button>
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
import { InfoFilled, Coin, CircleCheckFilled } from '@element-plus/icons-vue'
import { payExamApi, walletBalanceApi } from '@/api/wallet'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const orderId = route.params.orderId as string
const order = ref<any>(null)
const balance = ref(0)
const loading = ref(false)
const paying = ref(false)
const step = ref(1)

const patientId = (userStore.userInfo as any)?.patientId || ''

onMounted(async () => {
  if (!orderId) {
    ElMessage.warning('检查单不存在')
    router.push('/wallet')
    return
  }
  await Promise.all([loadOrder(), loadBalance()])
})

async function loadOrder() {
  loading.value = true
  try {
    // 检查单列表接口：用 /examination/result 不行，需要单独获取
    // 通过详情接口获取
    const res = await request.get<any>('/examination/detail', { params: { orderId } })
    order.value = res.data
    if (order.value?.status !== 0) {
      step.value = 3 // 已支付直接显示完成
    }
  } catch {
    order.value = null
  } finally {
    loading.value = false
  }
}

async function loadBalance() {
  if (!patientId) return
  try {
    const res = await walletBalanceApi(patientId)
    balance.value = (res.data as any)?.balance ?? 0
  } catch { balance.value = 0 }
}

function examCategoryText(c: number): string {
  const map: Record<number, string> = { 0: '实验室检查', 1: '影像学检查', 2: '功能检查' }
  return map[c] ?? '其他'
}

async function handlePay() {
  paying.value = true
  try {
    await payExamApi(orderId)
    ElMessage.success('支付成功')
    step.value = 3
    loadBalance()
  } catch {
    ElMessage.error('支付失败')
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.pay-page { max-width: 1000px; }
.page-title { font-size: 18px; font-weight: 600; margin-bottom: 24px; }

.header-icon { color: var(--cb-primary); font-size: 18px; }

/* 步骤条 */
.steps-bar {
  display: flex; align-items: center; justify-content: center; margin-bottom: 28px;
  padding: 20px; background: var(--cb-white); border-radius: 12px; box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.step { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.step-circle {
  width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center;
  justify-content: center; font-size: 13px; font-weight: 700; background: #f0f0f0; color: #999; transition: all .3s;
}
.step.active .step-circle { background: var(--cb-primary); color: #fff; box-shadow: 0 4px 12px rgba(26,127,191,.3); }
.step.completed .step-circle { background: #52c41a; color: #fff; }
.step-text { font-size: 12px; color: #999; white-space: nowrap; }
.step.active .step-text { color: var(--cb-primary); font-weight: 600; }
.step.completed .step-text { color: #52c41a; }
.step-line { width: 80px; height: 2px; background: #f0f0f0; margin: 0 12px 24px; }
.step-line.done { background: #52c41a; }

/* 详情 */
.detail-row { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px solid #f0f0f0; }
.detail-row:last-child { border-bottom: none; }
.detail-label { min-width: 80px; font-size: 13px; color: #666; }
.detail-value { font-size: 14px; color: #333; }
.mono { font-family: monospace; color: var(--cb-primary); font-weight: 600; }

/* 支付 */
.fee-display { text-align: center; padding: 8px 0; }
.fee-label { font-size: 13px; color: #666; }
.fee-amount { margin-top: 8px; display: flex; align-items: baseline; justify-content: center; gap: 4px; }
.fee-symbol { font-size: 20px; color: #e6a23c; }
.fee-number { font-size: 40px; font-weight: 700; color: #e6a23c; line-height: 1; }
.pay-btn { width: 100%; height: 46px; font-size: 16px; letter-spacing: 2px; }

.text-green { color: #52c41a; }
.text-red { color: #ff4d4f; }

/* 成功 */
.pay-success { text-align: center; padding: 20px 0 8px; }
.success-icon { margin-bottom: 16px; }
.success-title { font-size: 22px; margin: 0 0 8px; }
.success-desc { font-size: 14px; color: #666; margin: 0 0 24px; }
.success-actions { display: flex; flex-direction: column; gap: 12px; }
</style>
