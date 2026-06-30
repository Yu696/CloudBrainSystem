<template>
  <div class="exam-image-list-page">
    <div class="page-title">影像检查单</div>

    <!-- 筛选标签页 -->
    <div class="status-tabs">
      <el-radio-group v-model="statusFilter" size="small">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="0">已开单</el-radio-button>
        <el-radio-button value="1">已缴费</el-radio-button>
        <el-radio-button value="2">检查中</el-radio-button>
        <el-radio-button value="3">已完成</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 检查单表格 -->
    <div class="cb-card">
      <div class="cb-card-body">
        <el-table :data="filteredOrders" border stripe v-loading="loading" empty-text="暂无需要上传影像的检查单">
          <el-table-column prop="examName" label="检查项目" min-width="140" />
          <el-table-column prop="patientName" label="患者姓名" min-width="120" show-overflow-tooltip />
          <el-table-column label="检查类别" width="100">
            <template #default>
              <el-tag size="small" effect="plain" type="info">影像学</el-tag>
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
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status === 1" type="primary" size="small" @click="goUpload(row)">
                <el-icon><Upload /></el-icon>
                上传影像
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
import { useRouter } from 'vue-router'
import { Upload } from '@element-plus/icons-vue'
import { listImagingOrdersApi } from '@/api/medical'

const router = useRouter()
const loading = ref(false)
const orders = ref<any[]>([])
const statusFilter = ref('')

onMounted(async () => {
  loading.value = true
  try {
    const res = await listImagingOrdersApi()
    orders.value = res.data || []
  } finally {
    loading.value = false
  }
})

const filteredOrders = computed(() => {
  if (statusFilter.value === '') return orders.value
  return orders.value.filter((o) => String(o.status) === statusFilter.value)
})

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
</script>

<style scoped>
.exam-image-list-page {
  max-width: 1100px;
}

.status-tabs {
  margin-bottom: 16px;
}

.cb-card-body {
  padding: 0;
}
</style>
