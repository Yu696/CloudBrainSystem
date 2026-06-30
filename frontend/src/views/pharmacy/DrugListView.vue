<template>
  <div class="list-page">
    <div class="page-title">药品管理</div>

    <!-- 搜索栏 -->
    <div class="cb-card search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="药品名称 / 编码"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="全部" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="抗生素" value="抗生素" />
            <el-option label="解热镇痛药" value="解热镇痛药" />
            <el-option label="心血管药物" value="心血管药物" />
            <el-option label="维生素类" value="维生素类" />
          </el-select>
        </el-form-item>
        <el-form-item label="处方类型">
          <el-select v-model="searchForm.prescriptionType" placeholder="全部" clearable style="width: 140px">
            <el-option label="全部" :value="undefined" />
            <el-option label="OTC" :value="0" />
            <el-option label="处方药" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
      <div style="margin-top: 12px; text-align: right">
        <el-button type="success" @click="router.push('/pharmacy/drug/new')">
          <el-icon><Plus /></el-icon>新增
        </el-button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="cb-card">
      <el-table :data="drugList" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="drugId" label="药品 ID" width="140" />
        <el-table-column prop="drugName" label="药品名称" width="140" />
        <el-table-column prop="genericName" label="通用名" width="140" />
        <el-table-column prop="spec" label="规格" width="120" />
        <el-table-column prop="dosageForm" label="剂型" width="100" />
        <el-table-column prop="manufacturer" label="生产厂家" min-width="180" />
        <el-table-column prop="unitPrice" label="单价" width="100" align="right">
          <template #default="{ row }">
            <span>{{ formatPrice(row.unitPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="prescriptionType" label="处方类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.prescriptionType === 0 ? 'success' : 'warning'" size="small">
              {{ row.prescriptionType === 0 ? 'OTC' : '处方药' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="router.push(`/pharmacy/drug/${row.drugId}`)">
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && drugList.length === 0" description="暂无药品数据" :image-size="80" />

      <!-- 分页器 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { searchDrugApi, deleteDrugApi } from '@/api/pharmacy'

const router = useRouter()

const drugList = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  category: '',
  prescriptionType: undefined as number | undefined
})

onMounted(() => {
  loadDrugList()
})

async function loadDrugList() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.category) params.category = searchForm.category
    if (searchForm.prescriptionType !== undefined && searchForm.prescriptionType !== null) {
      params.prescriptionType = searchForm.prescriptionType
    }

    const res = await searchDrugApi(params)
    const data = res.data as any
    if (data) {
      drugList.value = data.records || data.list || data.items || (Array.isArray(data) ? data : [])
      total.value = data.total || drugList.value.length
    } else {
      drugList.value = []
      total.value = 0
    }
  } catch {
    drugList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  loadDrugList()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.category = ''
  searchForm.prescriptionType = undefined
  currentPage.value = 1
  loadDrugList()
}

function formatPrice(price: number | string | undefined): string {
  if (price === undefined || price === null) return '-'
  return '¥' + Number(price).toFixed(2)
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确认删除药品「${row.drugName}」吗？此操作不可撤销。`, '删除确认', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteDrugApi(row.drugId)
    ElMessage.success('删除成功')
    loadDrugList()
  } catch {
    // cancelled or error — do nothing
  }
}
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

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 16px 0;
}
</style>
