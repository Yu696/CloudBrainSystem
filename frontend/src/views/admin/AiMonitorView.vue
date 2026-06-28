<template>
  <div class="admin-page">
    <div class="page-title">AI 调用监控</div>

    <!-- 时间筛选 -->
    <div class="search-bar">
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width:280px" />
      <el-button type="primary" @click="loadStats">查询</el-button>
    </div>

    <!-- 统计概览 -->
    <div class="stat-grid" v-loading="statsLoading">
      <div class="stat-card">
        <div class="stat-icon blue">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 20V10" /><path d="M18 20V4" /><path d="M6 20v-4" />
          </svg>
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ stats.totalCalls || 0 }}</div>
          <div class="stat-label">总调用次数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
            <polyline points="22 4 12 14.01 9 11.01" />
          </svg>
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ stats.successRate }}%</div>
          <div class="stat-label">成功率</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="12" r="10" />
            <polyline points="12 6 12 12 16 14" />
          </svg>
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ stats.avgResponseTimeMs || 0 }}ms</div>
          <div class="stat-label">平均响应</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon red">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="12" r="10" />
            <line x1="15" y1="9" x2="9" y2="15" /><line x1="9" y1="9" x2="15" y2="15" />
          </svg>
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ stats.failedCalls || 0 }}</div>
          <div class="stat-label">失败次数（含降级）</div>
        </div>
      </div>
    </div>

    <!-- 按类型分布 -->
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="12">
        <div class="cb-card">
          <div class="cb-card-header"><span>按类型分布</span></div>
          <div class="cb-card-body">
            <div v-if="typeStats.length" class="type-bars">
              <div v-for="t in typeStats" :key="t.key" class="type-bar-row">
                <span class="type-bar-label">{{ t.label }}</span>
                <div class="type-bar-track">
                  <div class="type-bar-fill" :style="{ width: t.pct + '%', background: t.color }"></div>
                </div>
                <span class="type-bar-count">{{ t.calls }} 次</span>
                <span class="type-bar-ms">{{ t.avgMs }}ms</span>
              </div>
            </div>
            <div v-else style="text-align:center;padding:24px;color:#8E8E93">暂无数据</div>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="cb-card">
          <div class="cb-card-header"><span>每日趋势</span></div>
          <div class="cb-card-body">
            <div v-if="dailyTrend.length" class="daily-chart">
              <div v-for="(d, i) in dailyTrend" :key="i" class="daily-col">
                <div class="daily-bar-wrap">
                  <div class="daily-bar" :style="{ height: dailyBarHeight(d.calls) + '%', background: d.failed > 0 ? 'linear-gradient(180deg, #007AFF, #FF3B30)' : '#007AFF' }"></div>
                </div>
                <div class="daily-calls">{{ d.calls }}</div>
                <div class="daily-date">{{ formatDailyDate(d.date) }}</div>
              </div>
            </div>
            <div v-else style="text-align:center;padding:24px;color:#8E8E93">暂无趋势数据</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 调用明细 -->
    <div class="cb-card">
      <div class="cb-card-header">
        <span>调用明细</span>
        <div style="display:flex;gap:8px">
          <el-select v-model="logFilter.type" placeholder="类型" clearable style="width:120px" size="small" @change="loadLogs">
            <el-option label="分诊" :value="0" />
            <el-option label="诊断" :value="1" />
            <el-option label="处方审核" :value="2" />
          </el-select>
        </div>
      </div>
      <el-table :data="logs" v-loading="logsLoading" stripe style="width:100%">
        <el-table-column prop="callId" label="ID" width="180" show-overflow-tooltip />
        <el-table-column label="类型" width="100">
          <template #default="{ row }"><el-tag size="small">{{ getTypeName(row.callType) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="用户" width="130">
          <template #default="{ row }">{{ getUserDisplay(row) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responseTimeMs" label="响应时间" width="100">
          <template #default="{ row }">{{ row.responseTimeMs }}ms</template>
        </el-table-column>
        <el-table-column prop="aiModel" label="AI模型" width="140" />
        <el-table-column prop="createTime" label="调用时间" min-width="160" />
      </el-table>

      <div v-if="logTotal > logFilter.pageSize" style="display:flex;justify-content:center;padding:16px">
        <el-pagination v-model:current-page="logFilter.page" :page-size="logFilter.pageSize" :total="logTotal" layout="prev, pager, next" @current-change="loadLogs" small background />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { aiMonitorStatsApi, aiMonitorLogsApi } from '@/api/ai'

const dateRange = ref<[string, string]>(['', ''])
const statsLoading = ref(false)
const logsLoading = ref(false)
const stats = ref<any>({})
const logs = ref<any[]>([])
const logTotal = ref(0)

const logFilter = reactive({
  type: undefined as number | undefined,
  page: 1,
  pageSize: 20
})

const typeColors: Record<string, string> = {
  triage: '#007AFF',
  diagnosis: '#34C759',
  prescriptionCheck: '#FF9500',
  recordGenerate: '#FF3B30'
}

const typeLabels: Record<string, string> = {
  triage: '智能分诊',
  diagnosis: '诊断分析',
  prescriptionCheck: '处方审核',
  recordGenerate: '病历生成'
}

const typeStats = computed(() => {
  const byType = stats.value.byType
  if (!byType) return []
  const keys = Object.keys(byType)
  const maxCalls = Math.max(...keys.map(k => byType[k].calls), 1)
  return keys.map(key => ({
    key,
    label: typeLabels[key] || key,
    calls: byType[key].calls,
    avgMs: byType[key].avgMs,
    pct: (byType[key].calls / maxCalls) * 100,
    color: typeColors[key] || '#007AFF'
  }))
})

const dailyTrend = computed(() => stats.value.dailyTrend || [])

const maxDailyCalls = computed(() => Math.max(...dailyTrend.value.map(d => d.calls), 1))

function dailyBarHeight(calls: number) {
  return Math.max((calls / maxDailyCalls.value) * 100, 5)
}

function formatDailyDate(dateStr: string) {
  return dateStr?.slice(5) || ''
}

function getTypeName(callType: number) {
  const map: Record<number, string> = { 0: '智能分诊', 1: '诊断分析', 2: '处方审核' }
  return map[callType] || '未知'
}

function getStatusName(status: number) {
  return status === 1 ? '成功' : '失败'
}

function getUserDisplay(row: any) {
  return row.patientId || row.doctorId || '-'
}

onMounted(() => {
  const now = new Date()
  const start = new Date(now.getTime() - 7 * 86400000)
  dateRange.value = [
    start.toISOString().slice(0, 10),
    now.toISOString().slice(0, 10)
  ]
  loadStats()
  loadLogs()
})

async function loadStats() {
  statsLoading.value = true
  try {
    const [start, end] = dateRange.value
    const res = await aiMonitorStatsApi({ startDate: start, endDate: end || start })
    stats.value = res.data || {}
  } catch { stats.value = {} }
  finally { statsLoading.value = false }
}

async function loadLogs() {
  logsLoading.value = true
  try {
    const [start, end] = dateRange.value
    const res = await aiMonitorLogsApi({
      type: logFilter.type,
      page: logFilter.page,
      pageSize: logFilter.pageSize,
      startDate: start || undefined,
      endDate: end || undefined
    })
    const data = res.data as any
    logs.value = data.records || []
    logTotal.value = data.total || 0
  } catch { logs.value = []; logTotal.value = 0 }
  finally { logsLoading.value = false }
}
</script>

<style scoped>
/* 统计卡片 */
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-icon.blue { background: #e8f4fd; color: #007AFF; }
.stat-icon.green { background: #e8f8e8; color: #34C759; }
.stat-icon.orange { background: #fff3e0; color: #FF9500; }
.stat-icon.red { background: #ffe8e8; color: #FF3B30; }
.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #1C1C1E;
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: #8E8E93;
  margin-top: 2px;
}

/* 类型分布 */
.type-bars { display: flex; flex-direction: column; gap: 14px; }
.type-bar-row { display: flex; align-items: center; gap: 10px; }
.type-bar-label { min-width: 72px; font-size: 13px; color: #1C1C1E; }
.type-bar-track { flex: 1; height: 10px; background: #f0f0f0; border-radius: 5px; overflow: hidden; }
.type-bar-fill { height: 100%; border-radius: 5px; transition: width 0.6s ease; }
.type-bar-count { min-width: 60px; font-size: 13px; color: #8E8E93; text-align: right; }
.type-bar-ms { min-width: 50px; font-size: 12px; color: #c7c7cc; text-align: right; }

/* 每日趋势 */
.daily-chart {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  height: 160px;
  padding: 0 4px;
}
.daily-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  height: 100%;
}
.daily-bar-wrap {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.daily-bar {
  width: 60%;
  min-height: 4px;
  border-radius: 4px 4px 0 0;
  transition: height 0.5s ease;
}
.daily-calls { font-size: 11px; color: #8E8E93; font-weight: 500; }
.daily-date { font-size: 10px; color: #c7c7cc; }
</style>
