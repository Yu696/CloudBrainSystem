<template>
  <div class="admin-appointments">
    <div class="cb-card">
      <div class="cb-card-header">
        <span>预约管理</span>
        <el-button size="small" @click="loadData" :loading="loading">刷新</el-button>
      </div>
      <div class="cb-card-body">
        <!-- 筛选栏 -->
        <el-row :gutter="12" class="filter-row">
          <el-col :span="5">
            <el-input v-model="filters.patientId" placeholder="患者ID" clearable @change="loadData" />
          </el-col>
          <el-col :span="5">
            <el-input v-model="filters.doctorId" placeholder="医生ID" clearable @change="loadData" />
          </el-col>
          <el-col :span="5">
            <el-date-picker v-model="filters.date" type="date" placeholder="预约日期" value-format="YYYY-MM-DD" clearable @change="loadData" style="width:100%" />
          </el-col>
          <el-col :span="5">
            <el-select v-model="filters.status" placeholder="状态" clearable @change="loadData" style="width:100%">
              <el-option label="待接诊" :value="0" />
              <el-option label="已接诊" :value="1" />
              <el-option label="已完成" :value="2" />
              <el-option label="已取消" :value="4" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-button @click="resetFilters">重置</el-button>
          </el-col>
        </el-row>

        <el-table :data="appointments" border stripe v-loading="loading" class="table-top">
          <el-table-column prop="appointmentId" label="预约ID" width="120" />
          <el-table-column prop="patientName" label="患者" width="100">
            <template #default="{ row }">{{ row.patientName || row.patientId }}</template>
          </el-table-column>
          <el-table-column prop="doctorName" label="医生" width="100">
            <template #default="{ row }">{{ row.doctorName || row.doctorId }}</template>
          </el-table-column>
          <el-table-column prop="departmentName" label="科室" width="100">
            <template #default="{ row }">{{ row.departmentName || row.departmentId }}</template>
          </el-table-column>
          <el-table-column prop="appointmentDate" label="日期" width="110" />
          <el-table-column prop="timeSlotDesc" label="时段" width="130" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="支付" width="90">
            <template #default="{ row }">
              <el-tag :type="row.paymentStatus === 1 ? 'success' : 'warning'" size="small">
                {{ row.paymentStatus === 1 ? '已支付' : '待支付' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="totalFee" label="费用" width="80">
            <template #default="{ row }">&yen;{{ row.totalFee }}</template>
          </el-table-column>
          <el-table-column prop="symptoms" label="症状" min-width="120" show-overflow-tooltip />
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-popconfirm title="确定删除该预约？" @confirm="handleDelete(row.appointmentId)">
                <template #reference>
                  <el-button link type="danger" size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminListAppointmentsApi, deleteAppointmentApi } from '@/api/appointment'

interface Appointment {
  appointmentId: string
  patientId: string
  patientName: string
  doctorId: string
  doctorName: string
  departmentId: string
  departmentName: string
  appointmentDate: string
  timeSlotDesc: string
  status: number
  paymentStatus: number
  totalFee: number
  symptoms: string
}

const appointments = ref<Appointment[]>([])
const loading = ref(false)
const filters = ref({ patientId: '', doctorId: '', date: '', status: null as number | null })

async function loadData() {
  loading.value = true
  try {
    const params: any = {}
    if (filters.value.patientId) params.patientId = filters.value.patientId
    if (filters.value.doctorId) params.doctorId = filters.value.doctorId
    if (filters.value.date) params.date = filters.value.date
    if (filters.value.status !== null && filters.value.status !== undefined) params.status = filters.value.status
    const res = await adminListAppointmentsApi(params)
    appointments.value = res.data as Appointment[]
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = { patientId: '', doctorId: '', date: '', status: null }
  loadData()
}

async function handleDelete(appointmentId: string) {
  try {
    await deleteAppointmentApi(appointmentId)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // error handled by interceptor
  }
}

function statusText(status: number) {
  const map: Record<number, string> = { 0: '待接诊', 1: '已接诊', 2: '已完成', 4: '已取消' }
  return map[status] || '未知'
}

function statusTag(status: number) {
  const map: Record<number, string> = { 0: 'warning', 1: 'primary', 2: 'success', 4: 'info' }
  return map[status] || 'info'
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
