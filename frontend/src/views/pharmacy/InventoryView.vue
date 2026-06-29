<template>
  <div class="list-page">
    <div class="page-title">库存管理</div>

    <!-- 库存查询 -->
    <div class="cb-card search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="药品编码">
          <el-input
            v-model="searchForm.drugId"
            placeholder="输入 drugId 查询库存"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearchStock"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearchStock">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleResetStock">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 库存信息表格 -->
    <div class="cb-card" v-if="stockInfo !== null">
      <div class="section-title">库存信息</div>
      <el-table :data="stockInfo" v-loading="stockLoading" stripe style="width: 100%">
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
      </el-table>
      <el-empty v-if="!stockLoading && stockInfo.length === 0" description="无库存记录" :image-size="80" />
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
        <el-table-column prop="drugName" label="药品名称" min-width="120" />
        <el-table-column prop="alertType" label="预警类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="alertTypeTagType(row.alertType)" size="small">
              {{ alertTypeLabel(row.alertType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentStock" label="当前库存" width="100" align="right" />
        <el-table-column prop="threshold" label="阈值" width="100" align="right" />
        <el-table-column prop="alertMessage" label="预警信息" min-width="200" />
        <el-table-column prop="isHandled" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isHandled ? 'success' : 'warning'" size="small">
              {{ row.isHandled ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="预警时间" width="170" />
      </el-table>
      <el-empty v-if="!alertLoading && alertList.length === 0" description="暂无预警数据" :image-size="80" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { drugStockApi, lowStockAlertApi } from '@/api/pharmacy'
import type { TabsPaneContext } from 'element-plus'

const searchForm = reactive({
  drugId: ''
})

// --- 库存查询 ---
const stockInfo = ref<any[] | null>(null)
const stockLoading = ref(false)

async function handleSearchStock() {
  if (!searchForm.drugId.trim()) return
  stockLoading.value = true
  stockInfo.value = null
  try {
    const res = await drugStockApi(searchForm.drugId.trim())
    const data = res.data as any
    if (data) {
      stockInfo.value = Array.isArray(data) ? data : [data]
    } else {
      stockInfo.value = []
    }
  } catch {
    stockInfo.value = []
  } finally {
    stockLoading.value = false
  }
}

function handleResetStock() {
  searchForm.drugId = ''
  stockInfo.value = null
}

// --- 预警列表 ---
const activeAlertTab = ref('0')
const alertList = ref<any[]>([])
const alertLoading = ref(false)

async function loadAlertList() {
  alertLoading.value = true
  try {
    const type = parseInt(activeAlertTab.value, 10)
    const res = await lowStockAlertApi(type)
    const data = res.data as any
    if (data) {
      alertList.value = data.records || data.list || data.items || (Array.isArray(data) ? data : [])
    } else {
      alertList.value = []
    }
  } catch {
    alertList.value = []
  } finally {
    alertLoading.value = false
  }
}

function handleAlertTabChange(_tab: TabsPaneContext) {
  loadAlertList()
}

function alertTypeLabel(type: number): string {
  const map: Record<number, string> = { 0: '低库存', 1: '过期预警', 2: '库存积压' }
  return map[type] ?? '未知'
}

function alertTypeTagType(type: number): 'warning' | 'danger' | 'info' {
  const map: Record<number, 'warning' | 'danger' | 'info'> = { 0: 'warning', 1: 'danger', 2: 'info' }
  return map[type] ?? 'info'
}

onMounted(() => {
  loadAlertList()
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
</style>
