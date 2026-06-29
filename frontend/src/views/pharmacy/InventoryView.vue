<template>
  <div class="list-page">
    <div class="page-title">库存管理</div>

    <!-- 搜索栏 -->
    <div class="cb-card search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="仓库">
          <el-select v-model="searchForm.warehouseId" placeholder="全部仓库" clearable style="width: 160px" @change="handleSearch">
            <el-option v-for="w in warehouses" :key="w.warehouseId" :label="w.name" :value="w.warehouseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="药品名称 / ID / 编码"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
          <el-button type="success" @click="openStockInDialog">
            <el-icon><Plus /></el-icon>入库
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 库存表格 -->
    <div class="cb-card">
      <div class="section-title">
        <span>库存信息</span>
      </div>
      <el-table :data="stockList" v-loading="stockLoading" stripe style="width: 100%">
        <el-table-column prop="drugId" label="药品 ID" width="120" />
        <el-table-column prop="drugName" label="药品名称" min-width="120" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="currentStock" label="当前库存" width="100" align="right" />
        <el-table-column prop="minStock" label="最低库存" width="100" align="right" />
        <el-table-column prop="maxStock" label="最高库存" width="100" align="right" />
        <el-table-column prop="batchNo" label="批号" width="140" />
        <el-table-column prop="productionDate" label="生产日期" width="110" />
        <el-table-column prop="expiryDate" label="有效期" width="110" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="warning" size="small" @click="openAdjustFromTable(row)">
              调整库存
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!stockLoading && stockList.length === 0" description="无库存记录" :image-size="80" />

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadStockList"
          @current-change="loadStockList"
        />
      </div>
    </div>

    <!-- 预警列表 -->
    <div class="cb-card" style="margin-top: 16px">
      <div class="section-title">库存预警</div>
      <el-tabs v-model="activeAlertTab" @tab-click="handleAlertTabChange">
        <el-tab-pane label="低库存预警" name="0" />
        <el-tab-pane label="过期预警" name="1" />
        <el-tab-pane label="库存积压" name="2" />
      </el-tabs>

      <el-table :data="alertList" v-loading="alertLoading" stripe style="width: 100%">
        <el-table-column prop="drugId" label="药品 ID" width="120" />
        <el-table-column prop="drugName" label="药品名称" min-width="120" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="alertTypeName" label="预警类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="alertTypeTagType(row.alertType)" size="small">
              {{ row.alertTypeName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="当前库存" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.alertType !== 1">{{ row.currentStock }}</span>
            <span v-else style="color: #999">—</span>
          </template>
        </el-table-column>
        <el-table-column label="阈值" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.alertType !== 1">{{ row.threshold }}</span>
            <span v-else style="color: #999">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="alertMessage" label="预警信息" min-width="200" />
        <el-table-column prop="isHandled" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isHandled ? 'success' : 'warning'" size="small">
              {{ row.isHandled ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="预警时间" width="170" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="warning" size="small" @click="handleAlertAdjust(row)">
              处理
            </el-button>
            <el-button v-if="row.alertType === 0 && row.currentStock === 0" type="danger" size="small" @click="handleDeleteAlert(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!alertLoading && alertList.length === 0" description="暂无预警数据" :image-size="80" />
    </div>
  </div>

  <!-- 处理弹窗（根据预警类型显示不同内容） -->
  <el-dialog v-model="showDialog" :title="dialogTitle" width="500px">
    <!-- ====== Body ====== -->
    <!-- 低库存预警 → 入库 -->
    <template v-if="dialogType === 0">
      <el-form :model="adjustForm" label-width="100px" ref="adjustFormRef" :rules="adjustRules">
        <el-form-item label="药品ID">
          <el-input :model-value="adjustForm.drugId" disabled />
        </el-form-item>
        <el-form-item label="当前库存">
          <el-tag>{{ adjustForm.currentStock }}</el-tag>
        </el-form-item>
        <el-form-item label="入库数量" prop="quantity">
          <el-input-number v-model="adjustForm.quantity" :min="1" :max="99999" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="adjustForm.remark" placeholder="选填" maxlength="200" />
        </el-form-item>
      </el-form>
    </template>

    <!-- 库存表格 → 入库 / 出库 / 转仓 -->
    <template v-else-if="dialogType === 'stock'">
      <el-form label-width="100px">
        <el-form-item label="药品ID">
          <el-input :model-value="adjustForm.drugId" disabled />
        </el-form-item>
        <el-form-item label="当前库存">
          <el-tag>{{ adjustForm.currentStock }}</el-tag>
        </el-form-item>
      </el-form>

      <div class="action-selector">
        <div
          v-for="opt in stockOptions"
          :key="opt.value"
          class="action-card"
          :class="{ active: stockAction === opt.value }"
          @click="stockAction = opt.value"
        >
          <span class="action-label">{{ opt.label }}</span>
          <span class="action-sub">{{ opt.sub }}</span>
        </div>
      </div>

      <el-form label-width="100px" ref="stockFormRef" :rules="stockRules">
        <!-- 入库 -->
        <template v-if="stockAction === 'in'">
          <el-form-item label="入库数量" prop="quantity">
            <el-input-number v-model="stockQty" :min="1" :max="99999" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="stockRemark" placeholder="选填" maxlength="200" />
          </el-form-item>
        </template>

        <!-- 出库 -->
        <template v-else-if="stockAction === 'out'">
          <el-form-item label="出库数量" prop="quantity">
            <el-input-number v-model="stockQty" :min="1" :max="adjustForm.currentStock" />
            <div style="font-size: 12px; color: #999; margin-top: 4px;">当前库存 {{ adjustForm.currentStock }}，最大可出库 {{ adjustForm.currentStock }}</div>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="stockRemark" placeholder="选填" maxlength="200" />
          </el-form-item>
        </template>

        <!-- 转仓 -->
        <template v-else>
          <el-form-item label="源仓库">
            <el-input :model-value="stockSourceWarehouseName" disabled />
          </el-form-item>
          <el-form-item label="目标仓库" prop="targetWarehouse">
            <el-select v-model="stockTargetWarehouseId" placeholder="请选择目标仓库" style="width: 100%">
              <el-option v-for="w in warehouses" :key="w.warehouseId" :label="w.name" :value="w.warehouseId" />
            </el-select>
          </el-form-item>
          <el-form-item label="转移数量" prop="transferQty">
            <el-input-number v-model="stockQty" :min="1" :max="adjustForm.currentStock" />
            <div style="font-size: 12px; color: #999; margin-top: 4px;">当前库存 {{ adjustForm.currentStock }}，最大可转移 {{ adjustForm.currentStock }}</div>
          </el-form-item>
        </template>
      </el-form>
    </template>

    <template v-else-if="dialogType === 1">
      <div style="padding: 12px 0">
        <el-alert title="该药品已过期，销毁后库存将置为 0" type="error" :closable="false" show-icon style="margin-bottom: 20px" />
        <el-descriptions :column="1" border>
          <el-descriptions-item label="药品ID">{{ adjustForm.drugId }}</el-descriptions-item>
          <el-descriptions-item label="当前库存">
            <el-tag type="danger">{{ adjustForm.currentStock }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="预警信息">{{ adjustForm.alertMessage }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </template>

    <template v-else-if="dialogType === 2">
      <div class="action-selector">
        <div
          v-for="opt in overstockOptions"
          :key="opt.value"
          class="action-card"
          :class="{ active: overstockAction === opt.value }"
          @click="overstockAction = opt.value"
        >
          <span class="action-label">{{ opt.label }}</span>
          <span class="action-sub">{{ opt.sub }}</span>
        </div>
      </div>

      <el-form label-width="100px" ref="overstockFormRef" :rules="overstockRules">
        <el-form-item label="药品ID">
          <el-input :model-value="adjustForm.drugId" disabled />
        </el-form-item>
        <el-form-item label="当前库存">
          <el-tag type="warning">{{ adjustForm.currentStock }}</el-tag>
        </el-form-item>

        <template v-if="overstockAction === 'transfer'">
          <el-form-item label="源仓库" prop="sourceWarehouseId">
            <el-select v-model="transferSourceWarehouseId" placeholder="请选择源仓库" style="width: 100%">
              <el-option v-for="w in warehouses" :key="w.warehouseId" :label="w.name" :value="w.warehouseId" />
            </el-select>
          </el-form-item>
          <el-form-item label="目标仓库" prop="targetWarehouseId">
            <el-select v-model="transferTargetWarehouseId" placeholder="请选择目标仓库" style="width: 100%">
              <el-option v-for="w in warehouses" :key="w.warehouseId" :label="w.name" :value="w.warehouseId" />
            </el-select>
          </el-form-item>
          <el-form-item label="转移数量" prop="transferQty">
            <el-input-number v-model="transferQty" :min="1" :max="adjustForm.currentStock" />
            <div style="font-size: 12px; color: #999; margin-top: 4px;">最大可转移 {{ adjustForm.currentStock }}</div>
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item label="销毁数量" prop="destroyQty">
            <el-input-number v-model="destroyQty" :min="1" :max="adjustForm.currentStock" />
            <div style="font-size: 12px; color: #999; margin-top: 4px;">最大可销毁 {{ adjustForm.currentStock }}</div>
          </el-form-item>
        </template>
      </el-form>
    </template>

    <!-- ====== Footer（直接子节点，不嵌套在 v-if 里） ====== -->
    <template #footer>
      <template v-if="dialogType === 0">
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAdjustStock" :loading="adjustLoading">确认入库</el-button>
      </template>
      <template v-else-if="dialogType === 'stock'">
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleStockConfirm" :loading="stockActionLoading">
          {{ stockAction === 'in' ? '确认入库' : stockAction === 'out' ? '确认出库' : '确认转仓' }}
        </el-button>
      </template>
      <template v-else-if="dialogType === 1">
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="danger" @click="handleDestroyExpired" :loading="destroyLoading">确认销毁</el-button>
      </template>
      <template v-else-if="dialogType === 2">
        <template v-if="overstockAction === 'transfer'">
          <el-button @click="showDialog = false">取消</el-button>
          <el-button type="primary" @click="handleTransferStock" :loading="transferLoading">确认转仓</el-button>
        </template>
        <template v-else>
          <el-button @click="showDialog = false">取消</el-button>
          <el-button type="danger" @click="handleDestroyQty" :loading="destroyLoading">确认销毁</el-button>
        </template>
      </template>
    </template>
  </el-dialog>

  <!-- 入库弹窗（从顶部按钮打开） -->
  <el-dialog v-model="showStockInDialog" title="药品入库" width="500px">
    <el-form ref="stockInFormRef" :model="stockInForm" label-width="100px" :rules="stockInRules">
      <el-form-item label="药品" prop="drugId">
        <el-select
          v-model="stockInForm.drugId"
          filterable
          remote
          reserve-keyword
          placeholder="输入药品名称/ID搜索"
          :remote-method="searchDrugs"
          :loading="drugSearchLoading"
          style="width: 100%"
        >
          <el-option v-for="d in drugOptions" :key="d.drugId" :label="d.drugId + ' - ' + d.drugName" :value="d.drugId" />
        </el-select>
      </el-form-item>
      <el-form-item label="仓库" prop="warehouseId">
        <el-select v-model="stockInForm.warehouseId" placeholder="请选择仓库" style="width: 100%">
          <el-option v-for="w in warehouses" :key="w.warehouseId" :label="w.name" :value="w.warehouseId" />
        </el-select>
      </el-form-item>
      <el-form-item label="入库数量" prop="quantity">
        <el-input-number v-model="stockInForm.quantity" :min="1" :max="99999" />
      </el-form-item>
      <el-form-item label="批号">
        <el-input :model-value="stockInForm.batchNo" disabled />
      </el-form-item>
      <el-form-item label="生产日期">
        <el-date-picker v-model="stockInForm.productionDate" type="date" placeholder="选择生产日期" value-format="YYYY-MM-DD" style="width: 100%" />
      </el-form-item>
      <el-form-item label="有效期至">
        <el-date-picker v-model="stockInForm.expiryDate" type="date" placeholder="选择有效期" value-format="YYYY-MM-DD" style="width: 100%" />
      </el-form-item>
      <el-form-item label="最低库存">
        <el-input-number v-model="stockInForm.minStock" :min="0" :max="99999" />
      </el-form-item>
      <el-form-item label="最高库存">
        <el-input-number v-model="stockInForm.maxStock" :min="1" :max="999999" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="stockInForm.remark" placeholder="选填" maxlength="200" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showStockInDialog = false">取消</el-button>
      <el-button type="primary" @click="handleStockIn" :loading="stockInLoading">确认入库</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { drugStockListApi, lowStockAlertApi, adjustStockApi, destroyExpiredApi, transferStockApi, warehouseListApi, deleteAlertApi, searchDrugApi } from '@/api/pharmacy'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

// --- 库存列表（分页） ---
const searchForm = reactive({ keyword: '', warehouseId: '' })
const stockList = ref<any[]>([])
const stockLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function loadStockList() {
  stockLoading.value = true
  try {
    const res = await drugStockListApi({
      keyword: searchForm.keyword || undefined,
      warehouseId: searchForm.warehouseId || undefined,
      page: currentPage.value,
      pageSize: pageSize.value
    })
    const data = res.data as any
    if (data) {
      stockList.value = data.records || data.list || data.items || (Array.isArray(data) ? data : [])
      total.value = data.total || stockList.value.length
    } else {
      stockList.value = []
      total.value = 0
    }
  } catch {
    stockList.value = []
    total.value = 0
  } finally {
    stockLoading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  loadStockList()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.warehouseId = ''
  currentPage.value = 1
  loadStockList()
}

// --- 弹窗状态 ---
const showDialog = ref(false)
const dialogType = ref<'stock' | 0 | 1 | 2>('stock')

const dialogTitle = computed(() => {
  const titles: Record<string, string> = {
    stock: '调整库存',
    0: '补充入库',
    1: '销毁过期药品',
    2: '处理积压库存'
  }
  return titles[String(dialogType.value)] || '处理'
})

// --- 低库存预警→入库（原有逻辑） ---
const adjustLoading = ref(false)
const adjustFormRef = ref<FormInstance>()
const adjustForm = reactive({
  drugId: '',
  currentStock: 0,
  quantity: 0,
  remark: '',
  alertMessage: '',
  warehouseId: ''
})
const adjustRules: FormRules = {
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

// --- 库存表格→入库/出库/转仓 ---
const stockAction = ref<'in' | 'out' | 'transfer'>('in')
const stockOptions = [
  { value: 'in', label: '入库', sub: '增加库存' },
  { value: 'out', label: '出库', sub: '减少库存' },
  { value: 'transfer', label: '转仓', sub: '调拨仓库' }
]
const stockQty = ref(1)
const stockRemark = ref('')
const stockSourceWarehouseId = ref('')
const stockSourceWarehouseName = ref('')
const stockSourceBatchNo = ref('')
const stockTargetWarehouseId = ref('')
const stockActionLoading = ref(false)
const stockFormRef = ref<FormInstance>()
const stockRules: FormRules = {
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  targetWarehouse: [{ required: true, message: '请选择目标仓库', trigger: 'change' }]
}

function openAdjustFromTable(row: any) {
  dialogType.value = 'stock'
  stockAction.value = 'in'
  adjustForm.drugId = row.drugId
  adjustForm.currentStock = row.currentStock
  stockQty.value = 1
  stockRemark.value = ''
  stockSourceWarehouseId.value = row.warehouseId || ''
  stockSourceWarehouseName.value = row.warehouseName || ''
  stockSourceBatchNo.value = row.batchNo || ''
  stockTargetWarehouseId.value = ''
  loadWarehouses()
  showDialog.value = true
}

async function handleStockConfirm() {
  if (stockAction.value === 'in') {
    // 入库 → adjustStockApi 传正数
    stockActionLoading.value = true
    try {
      await adjustStockApi({ drugId: adjustForm.drugId, quantity: stockQty.value, remark: stockRemark.value || undefined, warehouseId: stockSourceWarehouseId.value || undefined, batchNo: stockSourceBatchNo.value || undefined })
      ElMessage.success('入库成功')
      showDialog.value = false
      loadStockList()
      loadAlertList()
    } catch { ElMessage.error('入库失败') }
    finally { stockActionLoading.value = false }
  } else if (stockAction.value === 'out') {
    // 出库 → adjustStockApi 传负数
    if (stockQty.value > adjustForm.currentStock) {
      ElMessage.warning('出库数量不能超过当前库存')
      return
    }
    stockActionLoading.value = true
    try {
      await adjustStockApi({ drugId: adjustForm.drugId, quantity: -stockQty.value, remark: stockRemark.value || undefined, warehouseId: stockSourceWarehouseId.value || undefined, batchNo: stockSourceBatchNo.value || undefined })
      ElMessage.success('出库成功')
      showDialog.value = false
      loadStockList()
      loadAlertList()
    } catch { ElMessage.error('出库失败') }
    finally { stockActionLoading.value = false }
  } else {
    // 转仓 → transferStockApi
    if (!stockTargetWarehouseId.value) { ElMessage.warning('请选择目标仓库'); return }
    if (stockTargetWarehouseId.value === stockSourceWarehouseId.value) { ElMessage.warning('源仓库和目标仓库不能相同'); return }
    stockActionLoading.value = true
    try {
      await transferStockApi({
        drugId: adjustForm.drugId,
        fromWarehouseId: stockSourceWarehouseId.value,
        toWarehouseId: stockTargetWarehouseId.value,
        quantity: stockQty.value
      })
      ElMessage.success('转仓成功')
      showDialog.value = false
      loadStockList()
      loadAlertList()
    } catch { ElMessage.error('转仓失败') }
    finally { stockActionLoading.value = false }
  }
}

async function handleAlertAdjust(row: any) {
  dialogType.value = row.alertType as 0 | 1 | 2
  adjustForm.drugId = row.drugId
  adjustForm.currentStock = row.currentStock
  adjustForm.alertMessage = row.alertMessage || ''
  adjustForm.warehouseId = row.warehouseId || ''

  if (row.alertType === 0) {
    adjustForm.quantity = 1
    adjustForm.remark = ''
    // 低库存预警的目标仓库已知，直接赋值
    stockSourceWarehouseId.value = row.warehouseId || ''
    stockSourceWarehouseName.value = row.warehouseName || ''
  }
  if (row.alertType === 2) {
    overstockAction.value = 'transfer'
    transferQty.value = Math.min(10, row.currentStock)
    destroyQty.value = Math.min(10, row.currentStock)
    transferSourceWarehouseId.value = row.warehouseId || ''
    transferTargetWarehouseId.value = ''
    loadWarehouses()
  }

  showDialog.value = true
}

async function handleAdjustStock() {
  const valid = await adjustFormRef.value?.validate().catch(() => false)
  if (!valid) return
  adjustLoading.value = true
  try {
    await adjustStockApi({
      drugId: adjustForm.drugId,
      quantity: adjustForm.quantity,
      remark: adjustForm.remark || undefined,
      warehouseId: adjustForm.warehouseId || undefined
    })
    ElMessage.success('入库成功')
    showDialog.value = false
    loadStockList()
    loadAlertList()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    adjustLoading.value = false
  }
}

// --- 销毁过期药品 ---
const destroyLoading = ref(false)

async function handleDestroyExpired() {
  destroyLoading.value = true
  try {
    await destroyExpiredApi(adjustForm.drugId)
    ElMessage.success('过期药品已销毁')
    showDialog.value = false
    loadStockList()
    loadAlertList()
  } catch {
    ElMessage.error('销毁失败')
  } finally {
    destroyLoading.value = false
  }
}

// --- 库存转移 ---
const transferLoading = ref(false)
const overstockAction = ref<'transfer' | 'destroy'>('transfer')
const overstockOptions = [
  { value: 'transfer', label: '转仓调拨', sub: '移至其他仓库' },
  { value: 'destroy', label: '销毁部分', sub: '减少库存' }
]
const transferSourceWarehouseId = ref('')
const transferTargetWarehouseId = ref('')
const transferQty = ref(10)
const destroyQty = ref(10)
const warehouses = ref<any[]>([])
const overstockFormRef = ref<FormInstance>()
const overstockRules: FormRules = {
  sourceWarehouseId: [{ required: true, message: '请选择源仓库', trigger: 'change' }],
  targetWarehouseId: [{ required: true, message: '请选择目标仓库', trigger: 'change' }]
}

async function loadWarehouses() {
  try {
    const res = await warehouseListApi()
    const data = res.data as any
    warehouses.value = Array.isArray(data) ? data : []
  } catch {
    warehouses.value = []
  }
}

async function handleTransferStock() {
  if (!transferSourceWarehouseId.value) {
    ElMessage.warning('请选择源仓库')
    return
  }
  if (!transferTargetWarehouseId.value) {
    ElMessage.warning('请选择目标仓库')
    return
  }
  if (transferSourceWarehouseId.value === transferTargetWarehouseId.value) {
    ElMessage.warning('源仓库和目标仓库不能相同')
    return
  }
  if (transferQty.value < 1) {
    ElMessage.warning('转移数量必须大于 0')
    return
  }
  transferLoading.value = true
  try {
    await transferStockApi({
      drugId: adjustForm.drugId,
      fromWarehouseId: transferSourceWarehouseId.value,
      toWarehouseId: transferTargetWarehouseId.value,
      quantity: transferQty.value
    })
    ElMessage.success('转仓成功')
    showDialog.value = false
    loadStockList()
    loadAlertList()
  } catch {
    ElMessage.error('转仓失败')
  } finally {
    transferLoading.value = false
  }
}

async function handleDestroyQty() {
  if (destroyQty.value < 1) {
    ElMessage.warning('销毁数量必须大于 0')
    return
  }
  destroyLoading.value = true
  try {
    await adjustStockApi({
      drugId: adjustForm.drugId,
      quantity: -destroyQty.value,
      warehouseId: adjustForm.warehouseId || undefined,
      remark: '积压销毁'
    })
    ElMessage.success('销毁成功')
    showDialog.value = false
    loadStockList()
    loadAlertList()
  } catch {
    ElMessage.error('销毁失败')
  } finally {
    destroyLoading.value = false
  }
}

async function handleDeleteAlert(row: any) {
  try {
    await deleteAlertApi(row.id)
    ElMessage.success('已删除')
    loadAlertList()
  } catch {
    ElMessage.error('删除失败')
  }
}

// --- 顶部入库弹窗 ---
const showStockInDialog = ref(false)
const stockInLoading = ref(false)
const stockInFormRef = ref<FormInstance>()
const stockInForm = reactive({
  drugId: '',
  warehouseId: '',
  quantity: 1,
  batchNo: '',
  productionDate: '',
  expiryDate: '',
  minStock: 10,
  maxStock: 1000,
  remark: ''
})
const stockInRules: FormRules = {
  drugId: [{ required: true, message: '请选择药品', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}
const drugSearchLoading = ref(false)
const drugOptions = ref<any[]>([])

async function searchDrugs(query: string) {
  if (!query || query.length < 1) { drugOptions.value = []; return }
  drugSearchLoading.value = true
  try {
    const res = await searchDrugApi({ keyword: query, pageSize: 20 })
    const data = res.data as any
    drugOptions.value = data?.records || data?.list || (Array.isArray(data) ? data : [])
  } catch {
    drugOptions.value = []
  } finally {
    drugSearchLoading.value = false
  }
}

function openStockInDialog() {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  stockInForm.batchNo = `${y}${m}${d}`
  stockInForm.drugId = ''
  stockInForm.warehouseId = ''
  stockInForm.quantity = 1
  stockInForm.productionDate = ''
  stockInForm.expiryDate = ''
  stockInForm.minStock = 10
  stockInForm.maxStock = 1000
  stockInForm.remark = ''
  drugOptions.value = []
  if (warehouses.value.length === 0) loadWarehouses()
  showStockInDialog.value = true
}

async function handleStockIn() {
  const valid = await stockInFormRef.value?.validate().catch(() => false)
  if (!valid) return
  stockInLoading.value = true
  try {
    await adjustStockApi({
      drugId: stockInForm.drugId,
      quantity: stockInForm.quantity,
      warehouseId: stockInForm.warehouseId,
      batchNo: stockInForm.batchNo,
      productionDate: stockInForm.productionDate || undefined,
      expiryDate: stockInForm.expiryDate || undefined,
      minStock: stockInForm.minStock,
      maxStock: stockInForm.maxStock,
      remark: stockInForm.remark || undefined
    })
    ElMessage.success('入库成功')
    showStockInDialog.value = false
    loadStockList()
    loadAlertList()
  } catch {
    ElMessage.error('入库失败')
  } finally {
    stockInLoading.value = false
  }
}

// --- 预警列表 ---
const activeAlertTab = ref('0')
const alertList = ref<any[]>([])
const alertLoading = ref(false)
let alertRequestSeq = 0

async function loadAlertList(tabType?: number) {
  const seq = ++alertRequestSeq
  alertLoading.value = true
  try {
    const type = tabType ?? parseInt(activeAlertTab.value, 10)
    const res = await lowStockAlertApi(type)
    if (seq !== alertRequestSeq) return
    const data = res.data as any
    if (data) {
      alertList.value = data.records || data.list || data.items || (Array.isArray(data) ? data : [])
    } else {
      alertList.value = []
    }
  } catch {
    if (seq === alertRequestSeq) alertList.value = []
  } finally {
    if (seq === alertRequestSeq) alertLoading.value = false
  }
}

function handleAlertTabChange(tab: any) {
  loadAlertList(parseInt(tab.props.name, 10))
}

function alertTypeTagType(type: number): 'warning' | 'danger' | 'info' {
  const map: Record<number, 'warning' | 'danger' | 'info'> = { 0: 'warning', 1: 'danger', 2: 'info' }
  return map[type] ?? 'info'
}

onMounted(() => {
  loadStockList()
  loadAlertList()
  loadWarehouses()
})
</script>

<style scoped>
.search-card {
  margin-bottom: 16px;
}

.search-card .el-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.search-card .el-form-item {
  margin-bottom: 0;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--el-text-color-primary);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 16px 0;
}

.action-selector {
  display: flex;
  gap: 12px;
  margin: 16px 0 24px;
}

.action-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 14px 8px;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: var(--el-fill-color-light);
  user-select: none;
}

.action-card:hover {
  border-color: var(--el-color-primary-light-3);
  background: var(--el-color-primary-light-9);
}

.action-card.active {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  box-shadow: 0 0 0 1px var(--el-color-primary);
}

.action-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.action-sub {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.action-card.active .action-label {
  color: var(--el-color-primary);
}
</style>
