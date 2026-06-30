<template>
  <div class="exam-image-list-page">
    <div class="page-title">{{ pageTitle }}</div>

    <!-- 筛选标签页 -->
    <div class="filter-bar">
      <div class="status-tabs">
        <span class="filter-label">状态：</span>
        <el-radio-group v-model="statusFilter" size="small">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="0">已开单</el-radio-button>
          <el-radio-button value="1">已缴费</el-radio-button>
          <el-radio-button value="2">检查中</el-radio-button>
          <el-radio-button value="3">已完成</el-radio-button>
        </el-radio-group>
      </div>
      <div v-if="showAllCategories" class="category-tabs">
        <span class="filter-label">类别：</span>
        <el-radio-group v-model="categoryFilter" size="small">
          <el-radio-button value="">全部类别</el-radio-button>
          <el-radio-button value="0">实验室</el-radio-button>
          <el-radio-button value="1">影像学</el-radio-button>
          <el-radio-button value="2">功能检查</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 检查单表格 -->
    <div class="cb-card">
      <div class="cb-card-body">
        <el-table :data="filteredOrders" border stripe v-loading="loading" empty-text="暂无检查单">
          <el-table-column prop="examName" label="检查项目" min-width="140" />
          <el-table-column prop="patientName" label="患者姓名" min-width="120" show-overflow-tooltip />
          <el-table-column label="检查类别" width="100">
            <template #default="{ row }">
              <el-tag size="small" effect="plain" :type="categoryTagType(row.examCategory)">
                {{ categoryText(row.examCategory) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="检查状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">
                {{ statusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="费用" width="100">
            <template #default="{ row }">
              &yen;{{ row.amount || 0 }}
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="170">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="130" align="center" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.examCategory === 1 && row.status === 1" type="primary" size="small" @click="goUpload(row)">
                <el-icon><Upload /></el-icon>
                上传影像
              </el-button>
              <el-button v-else-if="row.examCategory !== 1 && (row.status === 1 || row.status === 2)" type="primary" size="small" plain @click="goFillResult(row)">
                <el-icon><EditPen /></el-icon>
                填写结果
              </el-button>
              <el-tag v-else-if="row.status === 2" type="info" size="small">检查中</el-tag>
              <el-tag v-else-if="row.status === 3" type="success" size="small">已完成</el-tag>
              <el-tag v-else type="warning" size="small">待缴费</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Upload, EditPen } from '@element-plus/icons-vue'
import { listImagingOrdersApi, listAllExaminationOrdersApi } from '@/api/medical'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const orders = ref<any[]>([])
const statusFilter = ref('')
const categoryFilter = ref('')

const showAllCategories = computed(() => route.query.category !== '1')
const pageTitle = computed(() => showAllCategories.value ? '全部检查单' : '影像检查单')

onMounted(async () => {
  loading.value = true
  try {
    if (showAllCategories.value) {
      const res = await listAllExaminationOrdersApi()
      orders.value = res.data || []
    } else {
      const res = await listImagingOrdersApi()
      orders.value = res.data || []
    }
  } finally {
    loading.value = false
  }
})

const filteredOrders = computed(() => {
  let list = orders.value
  if (statusFilter.value !== '') {
    list = list.filter((o) => String(o.status) === statusFilter.value)
  }
  if (categoryFilter.value !== '') {
    list = list.filter((o) => String(o.examCategory) === categoryFilter.value)
  }
  return list
})

const categoryMap: Record<number, string> = {
  0: '实验室',
  1: '影像学',
  2: '功能检查'
}

function categoryText(cat: number): string {
  return categoryMap[cat] || '其他'
}

function categoryTagType(cat: number): '' | 'success' | 'info' | 'warning' | 'danger' {
  if (cat === 0) return 'success'
  if (cat === 1) return 'info'
  if (cat === 2) return 'warning'
  return ''
}

const statusMap: Record<number, string> = {
  0: '已开单',
  1: '已缴费',
  2: '检查中',
  3: '已完成',
  4: '已取消'
}

function statusText(status: number): string {
  return statusMap[status] || '未知'
}

function statusTagType(status: number): '' | 'warning' | 'success' | 'info' | 'danger' {
  if (status === 0) return 'warning'
  if (status === 1) return ''
  if (status === 2) return 'info'
  if (status === 3) return 'success'
  return 'info'
}

function formatDateTime(dateStr?: string): string {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ')
}

function goUpload(order: any) {
  router.push({
    path: '/image/upload/' + order.orderId,
    query: {
      patientId: order.patientId,
      examName: order.examName,
      status: order.status
    }
  })
}

function goFillResult(order: any) {
  router.push({
    path: '/doctor/exam-result/' + order.orderId
  })
}
</script>

<style scoped>
.exam-image-list-page {
  max-width: 1100px;
}

.filter-bar {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-tabs,
.category-tabs {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 13px;
  color: var(--cb-text-secondary);
  min-width: 48px;
}

.cb-card-body {
  padding: 0;
}
</style>
