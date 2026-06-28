<template>
  <div class="history-page">
    <div class="page-header">
      <div class="page-icon">
        <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="12" r="10" />
          <polyline points="12 6 12 12 16 14" />
        </svg>
      </div>
      <div>
        <h1>分诊历史</h1>
        <p class="page-subtitle">查看您的历史分诊记录</p>
      </div>
    </div>

    <div v-loading="loading" class="patient-card">
      <div class="card-body" style="padding:0">
        <div v-if="records.length > 0" class="history-list">
          <div
            v-for="(item, idx) in records"
            :key="item.triageId"
            class="history-item"
            :class="{ last: idx === records.length - 1 }"
          >
            <div class="hi-left">
              <div class="hi-icon" :class="{ warning: item.needsManualReview }">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
                  <circle cx="12" cy="12" r="10" />
                  <polyline points="12 6 12 12 16 14" />
                </svg>
              </div>
            </div>
            <div class="hi-body">
              <div class="hi-top">
                <span class="hi-complaint">{{ item.chiefComplaint }}</span>
                <div class="hi-tags">
                  <el-tag v-if="item.needsManualReview" type="warning" size="small" effect="light" class="tag-round">低置信度</el-tag>
                  <el-tag :type="item.status === 1 ? 'success' : 'info'" size="small" effect="light" class="tag-round">
                    {{ item.statusName || (item.status === 1 ? '完成' : '待处理') }}
                  </el-tag>
                </div>
              </div>
              <div class="hi-mid">
                <span class="hi-dept">{{ item.recommendedDepartmentName }}</span>
                <span class="hi-score">置信度 {{ (item.confidenceScore * 100).toFixed(0) }}%</span>
              </div>
              <div class="hi-time">{{ item.createTime }}</div>
            </div>
            <div class="hi-action">
              <el-button text type="primary" size="small" @click="handleRetriage(item)">
                复诊
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" style="margin-left:2px">
                  <polyline points="9 18 15 12 9 6" />
                </svg>
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="!loading && records.length === 0" style="padding:48px 0;text-align:center">
          <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#c7c7cc" stroke-width="1.5" style="margin-bottom:12px">
            <circle cx="12" cy="12" r="10" />
            <polyline points="12 6 12 12 16 14" />
          </svg>
          <div style="color:#8E8E93;font-size:14px">暂无分诊记录</div>
        </div>

        <div v-if="total > pageSize" class="page-foot">
          <el-pagination
            v-model:current-page="page"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadHistory"
            small
            background
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { triageHistoryApi } from '@/api/ai'

const router = useRouter()
const patientId = localStorage.getItem('patientId') || ''

const records = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => {
  if (!patientId) {
    ElMessage.warning('请先完善个人信息')
    router.push('/profile')
    return
  }
  loadHistory()
})

async function loadHistory() {
  loading.value = true
  try {
    const res = await triageHistoryApi({ patientId, page: page.value, pageSize: pageSize.value })
    const data = res.data as any
    records.value = data.records || []
    total.value = data.total || 0
  } catch {
    records.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleRetriage(row: any) {
  router.push({ path: '/ai/triage', query: { complaint: row.chiefComplaint } })
}
</script>

<style scoped>
.history-page {
  max-width: 720px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 28px;
}
.page-icon {
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, #e8f4fd, #d0ebfa);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #007AFF;
  flex-shrink: 0;
}
.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #1C1C1E;
}
.page-subtitle {
  margin: 4px 0 0;
  font-size: 14px;
  color: #8E8E93;
}

.patient-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.history-list {
  display: flex;
  flex-direction: column;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}
.history-item:hover {
  background: #fafafa;
}
.history-item.last {
  border-bottom: none;
}

.hi-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: #e8f4fd;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #007AFF;
}
.hi-icon.warning {
  background: #fff3e0;
  color: #FF9500;
}

.hi-body {
  flex: 1;
  min-width: 0;
}
.hi-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.hi-complaint {
  font-weight: 600;
  font-size: 15px;
  color: #1C1C1E;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.tag-round {
  border-radius: 20px !important;
}
.hi-tags {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}
.hi-mid {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 2px;
}
.hi-dept {
  font-size: 13px;
  color: #007AFF;
  font-weight: 500;
}
.hi-score {
  font-size: 12px;
  color: #8E8E93;
}
.hi-time {
  font-size: 12px;
  color: #c7c7cc;
}

.hi-action {
  flex-shrink: 0;
}

.page-foot {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}
</style>
