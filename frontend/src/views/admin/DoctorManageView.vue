<template>
  <div class="doctor-manage">
    <div class="cb-card">
      <div class="cb-card-header">
        <span>医生管理</span>
        <el-button type="primary" size="small" @click="loadData" :loading="loading">刷新</el-button>
      </div>
      <div class="cb-card-body">
        <el-table :data="doctors" border stripe v-loading="loading">
          <el-table-column prop="doctorId" label="医生ID" width="110" />
          <el-table-column prop="realName" label="姓名" width="100" />
          <el-table-column prop="departmentName" label="科室" width="120">
            <template #default="{ row }">{{ row.departmentName || row.departmentId }}</template>
          </el-table-column>
          <el-table-column prop="title" label="职称" width="100" />
          <el-table-column prop="specialty" label="专长" min-width="150" show-overflow-tooltip />
          <el-table-column prop="introduction" label="简介" min-width="180" show-overflow-tooltip />
          <el-table-column prop="consultationFee" label="挂号费" width="100">
            <template #default="{ row }">&yen;{{ row.consultationFee }}</template>
          </el-table-column>
          <el-table-column prop="maxDailyPatients" label="日限额" width="80" />
          <el-table-column label="接诊状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.available === 1 ? 'success' : 'danger'" size="small">
                {{ row.available === 1 ? '接诊中' : '已停诊' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
              <el-button link :type="row.available === 1 ? 'warning' : 'success'" size="small" @click="handleToggle(row)">
                {{ row.available === 1 ? '停诊' : '开诊' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="编辑医生信息" width="520px" destroy-on-close>
      <el-form :model="form" label-width="90px">
        <el-form-item label="科室">
          <el-input v-model="form.departmentId" placeholder="科室ID" />
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="form.title" placeholder="如：主任医师" />
        </el-form-item>
        <el-form-item label="专长">
          <el-input v-model="form.specialty" placeholder="擅长的领域" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.introduction" type="textarea" :rows="3" placeholder="医生简介" />
        </el-form-item>
        <el-form-item label="挂号费">
          <el-input-number v-model="form.consultationFee" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="日接诊上限">
          <el-input-number v-model="form.maxDailyPatients" :min="1" :max="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminListDoctorsApi, updateDoctorApi, toggleDoctorAvailableApi } from '@/api/appointment'

interface Doctor {
  doctorId: string
  realName: string
  departmentId: string
  title: string
  specialty: string
  introduction: string
  consultationFee: number
  maxDailyPatients: number
  available: number
}

const doctors = ref<Doctor[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref('')
const form = ref({ departmentId: '', title: '', specialty: '', introduction: '', consultationFee: 0, maxDailyPatients: 30 })

async function loadData() {
  loading.value = true
  try {
    const res = await adminListDoctorsApi()
    doctors.value = res.data as Doctor[]
  } finally {
    loading.value = false
  }
}

function openEdit(row: Doctor) {
  editingId.value = row.doctorId
  form.value = {
    departmentId: row.departmentId || '',
    title: row.title || '',
    specialty: row.specialty || '',
    introduction: row.introduction || '',
    consultationFee: row.consultationFee || 0,
    maxDailyPatients: row.maxDailyPatients || 30
  }
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    await updateDoctorApi(editingId.value, form.value)
    ElMessage.success('更新成功')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function handleToggle(row: Doctor) {
  const action = row.available === 1 ? '停诊' : '开诊'
  try {
    await ElMessageBox.confirm(`确定将该医生${action}？`, '提示', { type: 'warning' })
    await toggleDoctorAvailableApi(row.doctorId)
    ElMessage.success('操作成功')
    loadData()
  } catch {
    // cancelled
  }
}

onMounted(loadData)
</script>
