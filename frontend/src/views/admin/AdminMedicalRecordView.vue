<template>
  <div class="admin-records">
    <div class="cb-card">
      <div class="cb-card-header">
        <span>病历管理</span>
        <el-button size="small" @click="loadData" :loading="loading">刷新</el-button>
      </div>
      <div class="cb-card-body">
        <!-- 筛选栏 -->
        <el-row :gutter="12" class="filter-row">
          <el-col :span="6">
            <el-input v-model="filters.patientId" placeholder="患者ID" clearable @change="loadData" />
          </el-col>
          <el-col :span="6">
            <el-input v-model="filters.doctorId" placeholder="医生ID" clearable @change="loadData" />
          </el-col>
          <el-col :span="6">
            <el-button @click="resetFilters">重置</el-button>
          </el-col>
        </el-row>

        <el-table :data="records" border stripe v-loading="loading" class="table-top">
          <el-table-column prop="recordId" label="病历ID" width="120" />
          <el-table-column prop="patientName" label="患者" width="100">
            <template #default="{ row }">{{ row.patientName || row.patientId }}</template>
          </el-table-column>
          <el-table-column prop="doctorName" label="医生" width="100">
            <template #default="{ row }">{{ row.doctorName || row.doctorId }}</template>
          </el-table-column>
          <el-table-column prop="diagnosis" label="诊断" min-width="180" show-overflow-tooltip />
          <el-table-column prop="chiefComplaint" label="主诉" min-width="150" show-overflow-tooltip />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
                {{ row.status === 1 ? '已完成' : '草稿' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170">
            <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openDetail(row)">详情</el-button>
              <el-popconfirm title="确定删除该病历？" @confirm="handleDelete(row.recordId)">
                <template #reference>
                  <el-button link type="danger" size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="病历详情" width="680px" destroy-on-close>
      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="病历ID">{{ detail.recordId }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail.status === 1 ? 'success' : 'warning'" size="small">
            {{ detail.status === 1 ? '已完成' : '草稿' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="患者">{{ detail.patientName || detail.patientId }}</el-descriptions-item>
        <el-descriptions-item label="医生">{{ detail.doctorName || detail.doctorId }}</el-descriptions-item>
        <el-descriptions-item label="主诉" :span="2">{{ detail.chiefComplaint || '-' }}</el-descriptions-item>
        <el-descriptions-item label="现病史" :span="2">{{ detail.presentIllness || '-' }}</el-descriptions-item>
        <el-descriptions-item label="既往史" :span="2">{{ detail.pastHistory || '-' }}</el-descriptions-item>
        <el-descriptions-item label="体格检查" :span="2">{{ detail.physicalExam || '-' }}</el-descriptions-item>
        <el-descriptions-item label="辅助检查" :span="2">{{ detail.auxiliaryExam || '-' }}</el-descriptions-item>
        <el-descriptions-item label="诊断" :span="2">{{ detail.diagnosis || '-' }}</el-descriptions-item>
        <el-descriptions-item label="治疗意见" :span="2">{{ detail.treatmentOpinion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="诊断时间">{{ detail.diagnosisTime ? formatDate(detail.diagnosisTime) : '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listMedicalRecordsApi, getMedicalRecordDetailApi, deleteMedicalRecordApi } from '@/api/medical'

interface Record {
  recordId: string
  patientId: string
  patientName: string
  doctorId: string
  doctorName: string
  diagnosis: string
  chiefComplaint: string
  presentIllness: string
  pastHistory: string
  physicalExam: string
  auxiliaryExam: string
  treatmentOpinion: string
  status: number
  createTime: string
  diagnosisTime: string
}

const records = ref<Record[]>([])
const loading = ref(false)
const filters = ref({ patientId: '', doctorId: '' })

const detailVisible = ref(false)
const detail = ref<Record | null>(null)

async function loadData() {
  loading.value = true
  try {
    const res = await listMedicalRecordsApi(
      filters.value.patientId || undefined,
      filters.value.doctorId || undefined
    )
    records.value = res.data as Record[]
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = { patientId: '', doctorId: '' }
  loadData()
}

async function openDetail(row: Record) {
  try {
    const res = await getMedicalRecordDetailApi(row.recordId)
    detail.value = res.data as Record
    detailVisible.value = true
  } catch {
    // error handled by interceptor
  }
}

async function handleDelete(recordId: string) {
  try {
    await deleteMedicalRecordApi(recordId)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // error handled by interceptor
  }
}

function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

onMounted(loadData)
</script>

<style scoped>
.filter-row {
  margin-bottom: 16px;
}
.table-top {
  margin-top: 0;
}
</style>
