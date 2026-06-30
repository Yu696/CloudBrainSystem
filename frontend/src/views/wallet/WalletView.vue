<template>
  <div class="wallet-page">
    <div class="page-title">我的钱包</div>

    <!-- 余额卡片 -->
    <div class="balance-card">
      <div class="balance-label">钱包余额</div>
      <div class="balance-amount" :class="{ negative: balance < 0 }">
        ￥{{ balance.toFixed(2) }}
      </div>
      <div class="balance-actions">
        <el-button @click="rechargeVisible = true">
          <el-icon><Plus /></el-icon>充值
        </el-button>
      </div>
    </div>

    <el-row :gutter="24" class="section-row">
      <!-- 待支付 -->
      <el-col :span="12">
        <div class="cb-card">
          <div class="cb-card-header">
            <el-icon class="header-icon"><Clock /></el-icon>
            <span>待支付</span>
            <el-badge v-if="pendingOrders.length > 0" :value="pendingOrders.length" class="badge" />
          </div>
          <div class="cb-card-body">
            <div v-if="pendingOrders.length === 0" class="empty-mini">暂无待支付订单</div>
            <div v-for="(order, i) in pendingOrders" :key="i" class="pending-item">
              <div class="pending-info">
                <el-tag :type="order.type === '挂号费' ? 'warning' : 'info'" size="small">{{ order.type }}</el-tag>
                <span class="pending-desc">{{ order.desc }}</span>
                <span v-if="order.doctorName" class="pending-doctor">{{ order.doctorName }}</span>
              </div>
              <div class="pending-right">
                <span class="pending-amount">￥{{ order.amount }}</span>
                <el-button
                  v-if="order.routePath"
                  type="primary"
                  size="small"
                  @click="router.push(order.routePath)"
                >去支付</el-button>
                <el-tag v-else size="small" type="info">暂不支持</el-tag>
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 交易记录 -->
      <el-col :span="12">
        <div class="cb-card">
          <div class="cb-card-header">
            <el-icon class="header-icon"><List /></el-icon>
            <span>最近交易</span>
          </div>
          <div class="cb-card-body">
            <div v-if="transactions.length === 0" class="empty-mini">暂无交易记录</div>
            <div v-for="(tx, i) in transactions.slice(0, 10)" :key="i" class="tx-item">
              <div class="tx-info">
                <el-tag :type="tx.type === 0 ? 'success' : 'warning'" size="small">{{ tx.typeName }}</el-tag>
                <span class="tx-remark">{{ tx.remark }}</span>
              </div>
              <div class="tx-right">
                <span :class="tx.type === 0 ? 'text-green' : 'text-red'" class="tx-amount">
                  {{ tx.type === 0 ? '+' : '-' }}￥{{ Math.abs(tx.amount).toFixed(2) }}
                </span>
                <span class="tx-time">{{ formatTime(tx.createTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 充值弹窗 -->
    <el-dialog v-model="rechargeVisible" title="充值" width="400px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="充值金额">
          <el-input-number
            v-model="rechargeAmount"
            :min="1" :max="99999" :step="100"
            controls-position="right" style="width: 100%"
            placeholder="请输入充值金额"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" :loading="recharging" @click="handleRecharge">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, List, Clock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { walletBalanceApi, walletRechargeApi, walletTransactionsApi, pendingOrdersApi } from '@/api/wallet'

const router = useRouter()
const userStore = useUserStore()
const balance = ref(0)
const loading = ref(false)
const transactions = ref<any[]>([])
const pendingOrders = ref<any[]>([])

const rechargeVisible = ref(false)
const rechargeAmount = ref(100)
const recharging = ref(false)

const patientId = computedPatientId()

function computedPatientId(): string {
  return (userStore.userInfo as any)?.patientId || ''
}

onMounted(() => {
  if (patientId) {
    loadAll()
  }
})

async function loadAll() {
  loadBalance()
  await Promise.all([loadTransactions(), loadPendingOrders()])
}

async function loadBalance() {
  try {
    const res = await walletBalanceApi(patientId)
    balance.value = (res.data as any)?.balance ?? 0
  } catch {
    balance.value = 0
  }
}

async function loadTransactions() {
  try {
    const res = await walletTransactionsApi(patientId)
    transactions.value = Array.isArray(res.data) ? res.data : []
  } catch {
    transactions.value = []
  }
}

async function loadPendingOrders() {
  try {
    const res = await pendingOrdersApi(patientId)
    pendingOrders.value = Array.isArray(res.data) ? res.data : []
  } catch {
    pendingOrders.value = []
  }
}

function formatTime(t: string) {
  return t ? t.substring(0, 16) : ''
}

async function handleRecharge() {
  if (rechargeAmount.value <= 0) {
    ElMessage.warning('请填写充值金额')
    return
  }
  recharging.value = true
  try {
    await walletRechargeApi(patientId, rechargeAmount.value)
    ElMessage.success(`充值成功 +￥${rechargeAmount.value}`)
    rechargeVisible.value = false
    rechargeAmount.value = 100
    loadBalance()
    loadTransactions()
  } catch {
    ElMessage.error('充值失败')
  } finally {
    recharging.value = false
  }
}
</script>

<style scoped>
.wallet-page { max-width: 1100px; }
.page-title { font-size: 18px; font-weight: 600; margin-bottom: 24px; }

.balance-card {
  background: linear-gradient(135deg, #1a7fbf, #0d5a8a);
  border-radius: 12px; padding: 28px 28px; color: #fff; margin-bottom: 24px;
  display: flex; align-items: center; justify-content: space-between;
}
.balance-label { font-size: 14px; opacity: 0.85; }
.balance-amount { font-size: 32px; font-weight: 700; }
.balance-amount.negative { color: #ff7875; }
.balance-actions { display: flex; gap: 12px; }

.section-row { margin-top: 0; }

.header-icon { color: var(--cb-primary); font-size: 18px; }
.badge { margin-left: 8px; }

.empty-mini {
  text-align: center; padding: 32px 0; color: var(--el-text-color-placeholder);
  font-size: 13px;
}

/* 待支付 */
.pending-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 0; border-bottom: 1px solid var(--el-border-color-light);
}
.pending-item:last-child { border-bottom: none; }
.pending-info { display: flex; align-items: center; gap: 8px; flex: 1; overflow: hidden; }
.pending-desc { font-size: 13px; color: var(--el-text-color-regular); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.pending-doctor { font-size: 12px; color: var(--el-text-color-secondary); }
.pending-right { display: flex; align-items: center; gap: 10px; flex-shrink: 0; }
.pending-amount { font-size: 15px; font-weight: 600; color: #e6a23c; }

/* 交易 */
.tx-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 0; border-bottom: 1px solid var(--el-border-color-light);
}
.tx-item:last-child { border-bottom: none; }
.tx-info { display: flex; align-items: center; gap: 8px; }
.tx-remark { font-size: 13px; color: var(--el-text-color-regular); }
.tx-right { display: flex; flex-direction: column; align-items: flex-end; gap: 2px; }
.tx-amount { font-size: 14px; font-weight: 600; }
.tx-time { font-size: 11px; color: var(--el-text-color-placeholder); }

.text-green { color: #52c41a; }
.text-red { color: #ff4d4f; }
</style>
