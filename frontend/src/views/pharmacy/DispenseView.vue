<template>
  <div class="dispense-page">
    <div class="page-title">发药管理</div>

    <!-- 订单列表视图 -->
    <div v-if="!currentOrder" class="order-list-view">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待发药" name="pending">
          <div class="cb-card">
            <el-table :data="pendingList" v-loading="loading" stripe>
              <el-table-column prop="prescriptionId" label="处方 ID" width="180" />
              <el-table-column prop="patientName" label="患者" width="100" />
              <el-table-column prop="doctorName" label="医生" width="100" />
              <el-table-column prop="prescriptionDesc" label="处方描述" min-width="160" />
              <el-table-column label="药品明细" min-width="240">
                <template #default="{ row }">
                  <div v-for="(it, i) in row.items" :key="i" class="drug-tag-row">
                    <el-tag size="small" type="info">{{ it.drugName || it.drugId }}</el-tag>
                    <span class="qty">×{{ it.quantity }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="totalAmount" label="金额" width="90" align="right">
                <template #default="{ row }">￥{{ row.totalAmount }}</template>
              </el-table-column>
              <el-table-column prop="createTime" label="开方时间" width="170" />
              <el-table-column label="操作" width="80" fixed="right" align="center">
                <template #default="{ row }">
                  <el-button type="primary" size="small" @click="enterDispense(row)">发药</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!loading && pendingList.length === 0" description="暂无待发药订单" :image-size="80" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="已发药" name="done">
          <div class="cb-card">
            <el-table :data="doneList" v-loading="loading" stripe>
              <el-table-column prop="recordId" label="发药记录 ID" width="200" />
              <el-table-column prop="prescriptionId" label="处方 ID" width="180" />
              <el-table-column prop="patientName" label="患者" width="100" />
              <el-table-column prop="doctorName" label="医生" width="100" />
              <el-table-column prop="shipNum" label="件数" width="60" align="center" />
              <el-table-column prop="payAmount" label="金额" width="90" align="right">
                <template #default="{ row }">￥{{ row.payAmount }}</template>
              </el-table-column>
              <el-table-column prop="shipTime" label="发药时间" width="170" />
              <el-table-column prop="printStatus" label="打印" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.printStatus === 1 ? 'success' : 'warning'" size="small">
                    {{ row.printStatus === 1 ? '已打印' : '未打印' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" fixed="right" align="center">
                <template #default="{ row }">
                  <el-button type="primary" size="small" link @click="handlePrint(row)">打印</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!loading && doneList.length === 0" description="暂无已发药记录" :image-size="80" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 发药表单视图 -->
    <div v-else v-loading="submitting" class="dispense-form-view">
      <div class="back-row">
        <el-button text @click="currentOrder = null">
          <el-icon><ArrowLeft /></el-icon>返回订单列表
        </el-button>
      </div>

      <div class="cb-card">
        <div class="cb-card-header">
          <el-icon class="header-icon"><Goods /></el-icon>
          <span>发药出库</span>
        </div>
        <div class="cb-card-body">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="left">
            <el-row :gutter="24">
              <el-col :span="8">
                <el-form-item label="处方 ID">
                  <el-input :model-value="currentOrder.prescriptionId" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="患者">
                  <el-input :model-value="currentOrder.patientName" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="患者 ID">
                  <el-input :model-value="currentOrder.patientId" disabled />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="24">
              <el-col :span="8">
                <el-form-item label="医生">
                  <el-input :model-value="currentOrder.doctorName" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="处方描述">
                  <el-input :model-value="currentOrder.prescriptionDesc" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="总金额">
                  <span class="total-amount">￥{{ computedTotal.toFixed(2) }}</span>
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider />

            <div class="section-title">
              <el-icon><List /></el-icon>
              <span>发药明细</span>
            </div>

            <!-- 明细表头 -->
            <el-row :gutter="8" class="item-header-row">
              <el-col :span="3"><span class="col-label">药品 ID</span></el-col>
              <el-col :span="6"><span class="col-label">药品名称</span></el-col>
              <el-col :span="3"><span class="col-label">单价</span></el-col>
              <el-col :span="4"><span class="col-label">数量</span></el-col>
              <el-col :span="4"><span class="col-label">小计</span></el-col>
              <el-col :span="4"><span class="col-label">操作</span></el-col>
            </el-row>

            <div v-for="(item, index) in form.items" :key="index" class="dispense-item-row">
              <el-row :gutter="8" align="middle">
                <el-col :span="3">
                  <el-form-item
                    :prop="`items.${index}.drugId`"
                    :rules="[{ required: true, message: '必填', trigger: 'blur' }]"
                    label-width="0"
                  >
                    <el-input v-model="item.drugId" placeholder="药品 ID" size="small"
                      @change="onDrugIdChange(index)" />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item
                    :prop="`items.${index}.drugName`"
                    :rules="[{ required: true, message: '必选', trigger: 'change' }]"
                    label-width="0"
                  >
                    <el-select
                      v-model="item.drugName"
                      placeholder="搜索药品"
                      filterable
                      size="small"
                      style="width: 100%"
                      @change="onDrugNameChange(index)"
                    >
                      <el-option
                        v-for="d in drugOptions"
                        :key="d.drugId"
                        :label="d.drugName"
                        :value="d.drugName"
                      >
                        <span>{{ d.drugName }}</span>
                        <span style="float: right; color: var(--el-text-color-secondary); font-size: 12px">{{ d.spec || '' }} ￥{{ d.unitPrice }}</span>
                      </el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="3">
                  <el-form-item label-width="0">
                    <el-input :model-value="item.unitPrice ? '￥' + item.unitPrice : '-'" size="small" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="4">
                  <el-form-item label-width="0">
                    <el-input :model-value="String(item.quantity)" size="small" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="4">
                  <el-form-item label-width="0">
                    <el-input :model-value="itemSubtotal(index)" size="small" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="4">
                  <el-button type="danger" size="small" :disabled="form.items.length <= 1" @click="removeItem(index)">
                    删除
                  </el-button>
                </el-col>
              </el-row>
            </div>

            <el-divider />

            <div class="form-actions">
              <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting">确认发药</el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ArrowLeft, Goods, List } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { dispenseDrugApi, printShipRecordApi, dispenseListApi, dispenseRecordsApi, allDrugsApi } from '@/api/pharmacy'

interface DispenseItem {
  drugId: string
  drugName: string
  quantity: number
  unitPrice: number | null
}

const loading = ref(false)
const submitting = ref(false)
const activeTab = ref('pending')
const formRef = ref<FormInstance>()

const pendingList = ref<any[]>([])
const doneList = ref<any[]>([])
const currentOrder = ref<any>(null)
const drugOptions = ref<any[]>([])

interface DispenseForm {
  items: DispenseItem[]
}

const form = reactive<DispenseForm>({
  items: [{ drugId: '', drugName: '', quantity: 1, unitPrice: null }]
})

const rules: FormRules<DispenseForm> = {}

/** 单行小计 */
function itemSubtotal(index: number) {
  const item = form.items[index]
  if (item.unitPrice != null && item.quantity > 0) {
    return '￥' + (item.unitPrice * item.quantity).toFixed(2)
  }
  return '-'
}

/** 总金额 = 所有行小计之和 */
const computedTotal = computed(() => {
  return form.items.reduce((sum, it) => {
    if (it.unitPrice != null && it.quantity > 0) {
      return sum + it.unitPrice * it.quantity
    }
    return sum
  }, 0)
})

onMounted(() => {
  loadOrders()
  loadDrugOptions()
})

async function loadDrugOptions() {
  try {
    const res = await allDrugsApi()
    drugOptions.value = Array.isArray(res.data) ? res.data : []
  } catch {
    drugOptions.value = []
  }
}

function findDrugById(id: string) {
  return drugOptions.value.find((d: any) => d.drugId === id)
}

function findDrugByName(name: string) {
  return drugOptions.value.find((d: any) => d.drugName === name)
}

/** 输入药品 ID → 自动匹配药品名 + 单价 */
function onDrugIdChange(index: number) {
  const item = form.items[index]
  if (!item.drugId) {
    item.drugName = ''
    item.unitPrice = null
    return
  }
  const matched = findDrugById(item.drugId)
  if (matched) {
    item.drugName = matched.drugName
    item.unitPrice = matched.unitPrice ?? null
  }
}

/** 下拉选药品名 → 自动填药品 ID + 单价 */
function onDrugNameChange(index: number) {
  const item = form.items[index]
  if (!item.drugName) {
    item.drugId = ''
    item.unitPrice = null
    return
  }
  const matched = findDrugByName(item.drugName)
  if (matched) {
    item.drugId = matched.drugId
    item.unitPrice = matched.unitPrice ?? null
  }
}

async function loadOrders() {
  loading.value = true
  try {
    const [pendingRes, doneRes] = await Promise.all([
      dispenseListApi(),
      dispenseRecordsApi()
    ])
    pendingList.value = Array.isArray(pendingRes.data) ? pendingRes.data : []
    doneList.value = Array.isArray(doneRes.data) ? doneRes.data : []
  } catch {
    pendingList.value = []
    doneList.value = []
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  loadOrders()
}

function enterDispense(order: any) {
  currentOrder.value = order
  // 从处方明细预填药品（ID + 名称 + 单价联动）
  if (order.items && order.items.length > 0) {
    form.items = order.items.map((it: any) => {
      const matched = findDrugById(it.drugId)
      return {
        drugId: it.drugId || '',
        drugName: matched ? matched.drugName : (it.drugName || ''),
        quantity: it.quantity || 1,
        unitPrice: matched ? (matched.unitPrice ?? null) : null
      }
    })
  } else {
    form.items = [{ drugId: '', drugName: '', quantity: 1, unitPrice: null }]
  }
}

function removeItem(index: number) {
  if (form.items.length <= 1) return
  form.items.splice(index, 1)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await dispenseDrugApi({
      prescriptionId: currentOrder.value.prescriptionId,
      patientId: currentOrder.value.patientId,
      items: form.items
    })
    ElMessage.success('发药成功')
    currentOrder.value = null
    form.items = [{ drugId: '', drugName: '', quantity: 1, unitPrice: null }]
    loadOrders()
  } catch {
    ElMessage.error('发药失败')
  } finally {
    submitting.value = false
  }
}

async function handlePrint(row: any) {
  try {
    await printShipRecordApi(row.recordId)
    ElMessage.success('打印成功')
    loadOrders()
  } catch {
    ElMessage.error('打印失败')
  }
}
</script>

<style scoped>
.dispense-page {
  max-width: 1200px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
}

.order-list-view .cb-card {
  margin-bottom: 16px;
}

.dispense-form-view .back-row {
  margin-bottom: 12px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 16px;
}

.item-header-row {
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color);
  margin-bottom: 4px;
}

.col-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-regular);
  padding-left: 4px;
}

.dispense-item-row {
  padding: 12px 0;
  border-bottom: 1px solid var(--el-border-color-light);
}

.dispense-item-row:last-child {
  border-bottom: none;
}

.total-amount {
  font-size: 22px;
  font-weight: 700;
  color: #e6a23c;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.drug-tag-row {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-right: 8px;
  margin-bottom: 2px;
}

.qty {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
