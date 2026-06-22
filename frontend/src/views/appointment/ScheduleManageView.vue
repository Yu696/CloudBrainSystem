<template>
  <div class="schedule-page">
    <div class="page-title">排班管理</div>

    <el-row :gutter="24">
      <!-- 左侧：创建排班 -->
      <el-col :span="12">
        <div class="cb-card">
          <div class="cb-card-header">
            <el-icon class="header-icon"><Edit /></el-icon>
            <span>设置排班</span>
          </div>
          <div class="cb-card-body">
            <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="form-section">
              <el-form-item label="科室" prop="departmentId">
                <el-select v-model="form.departmentId" placeholder="请选择科室" filterable @change="handleDeptChange">
                  <el-option
                    v-for="dept in departments"
                    :key="dept.departmentId"
                    :label="dept.name"
                    :value="dept.departmentId"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="医生" prop="doctorId">
                <el-select v-model="form.doctorId" placeholder="请选择医生" filterable>
                  <el-option
                    v-for="doc in filteredDoctors"
                    :key="doc.doctorId"
                    :label="(doc.title ? doc.title + ' - ' + doc.realName : (doc.realName || '医师'))"
                    :value="doc.doctorId"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="排班日期" prop="scheduleDate">
                <el-date-picker
                  v-model="form.scheduleDate"
                  type="date"
                  placeholder="选择日期"
                  :disabled-date="disabledPastDate"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="班次" prop="workShift">
                <el-radio-group v-model="form.workShift">
                  <el-radio-button :value="0">上午</el-radio-button>
                  <el-radio-button :value="1">下午</el-radio-button>
                  <el-radio-button :value="2">晚班</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="开始时间" prop="startTime">
                <el-time-picker
                  v-model="form.startTime"
                  placeholder="开始时间"
                  format="HH:mm"
                  value-format="HH:mm:ss"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="结束时间" prop="endTime">
                <el-time-picker
                  v-model="form.endTime"
                  placeholder="结束时间"
                  format="HH:mm"
                  value-format="HH:mm:ss"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="时段长度">
                <el-input-number v-model="form.slotDuration" :min="15" :max="1440" :step="15" /> 分钟
              </el-form-item>
              <el-form-item label="最大人数">
                <el-input-number :model-value="maxPatients" disabled :min="1" :max="999" /> 人
                <span class="calc-hint">= {{ totalMinutes }}分钟 ÷ {{ form.slotDuration }}分钟/时段</span>
              </el-form-item>
              <el-form-item label="备注">
                <el-input v-model="form.remark" placeholder="备注信息（选填）" maxlength="200" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="saveLoading" @click="handleSave" round>
                  创建排班
                </el-button>
                <el-button @click="resetForm">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-col>

      <!-- 右侧：排班查询 -->
      <el-col :span="12">
        <div class="cb-card">
          <div class="cb-card-header">
            <el-icon class="header-icon"><Search /></el-icon>
            <span>排班查询</span>
          </div>
          <div class="cb-card-body">
            <div class="query-bar">
              <el-select v-model="queryDoctorId" placeholder="选择医生" filterable clearable style="width: 220px">
                <el-option
                  v-for="doc in allDoctors"
                  :key="doc.doctorId"
                  :label="(doc.title || '医师') + ' - ' + doc.doctorId"
                  :value="doc.doctorId"
                />
              </el-select>
              <el-button type="primary" @click="handleQuery" :loading="queryLoading">查询</el-button>
            </div>

            <el-table
              v-if="schedules.length > 0"
              :data="schedules"
              border
              stripe
              class="cb-table"
              height="400"
            >
              <el-table-column prop="scheduleDate" label="日期" width="110" />
              <el-table-column label="班次" width="70">
                <template #default="{ row }">
                  <el-tag :type="shiftTag(row.workShift)" size="small">
                    {{ shiftLabel(row.workShift) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="时间" width="130">
                <template #default="{ row }">
                  {{ row.startTime }} - {{ row.endTime }}
                </template>
              </el-table-column>
              <el-table-column prop="availableSlots" label="余号" width="60" align="center" />
              <el-table-column prop="status" label="状态" width="70">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                    {{ row.status === 1 ? '启用' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>

            <el-empty v-if="!queryLoading && schedules.length === 0" description="暂无排班数据" :image-size="80" />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit, Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { listDepartmentsApi, listDoctorsApi, setScheduleApi, queryScheduleApi } from '@/api/appointment'

interface Department {
  departmentId: string
  name: string
  category: string
}

interface Doctor {
  doctorId: string
  userId: string
  departmentId: string
  realName: string
  title: string
}

interface Schedule {
  scheduleId: string
  doctorId: string
  departmentId: string
  scheduleDate: string
  workShift: number
  startTime: string
  endTime: string
  slotDuration: number
  maxPatients: number
  availableSlots: number
  status: number
}

const formRef = ref<FormInstance>()
const saveLoading = ref(false)
const queryLoading = ref(false)

// Form state
const form = reactive({
  departmentId: '',
  doctorId: '',
  scheduleDate: '',
  workShift: 0,
  startTime: '',
  endTime: '',
  slotDuration: 30,
  maxPatients: 20,
  remark: ''
})

// 总分钟数（结束时间 - 开始时间）
const totalMinutes = computed(() => {
  if (!form.startTime || !form.endTime) return 0
  const [sh, sm] = form.startTime.split(':').map(Number)
  const [eh, em] = form.endTime.split(':').map(Number)
  return Math.max(0, (eh * 60 + em) - (sh * 60 + sm))
})

// 最大人数 = 总分钟 ÷ 时段长度（只读展示）
const maxPatients = computed(() => {
  if (!totalMinutes.value || !form.slotDuration) return 0
  return Math.floor(totalMinutes.value / form.slotDuration)
})

// startTime/endTime 变化时，slotDuration 默认设为总时长
watch(
  [() => form.startTime, () => form.endTime],
  () => {
    if (totalMinutes.value > 0) {
      form.slotDuration = totalMinutes.value
    }
  }
)

const rules: FormRules = {
  departmentId: [{ required: true, message: '请选择科室', trigger: 'change' }],
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  scheduleDate: [{ required: true, message: '请选择排班日期', trigger: 'change' }],
  workShift: [{ required: true, message: '请选择班次', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

// Query state
const queryDoctorId = ref('')

const departments = ref<Department[]>([])
const allDoctors = ref<Doctor[]>([])
const filteredDoctors = ref<Doctor[]>([])
const schedules = ref<Schedule[]>([])

onMounted(async () => {
  await Promise.all([loadDepartments(), loadAllDoctors()])
})

async function loadDepartments() {
  try {
    const res = await listDepartmentsApi()
    departments.value = (res.data as Department[]) || []
  } catch {
    departments.value = []
  }
}

async function loadAllDoctors() {
  try {
    const res = await listDoctorsApi()
    allDoctors.value = (res.data as Doctor[]) || []
  } catch {
    allDoctors.value = []
  }
}

function handleDeptChange(deptId: string) {
  form.doctorId = ''
  filteredDoctors.value = allDoctors.value.filter(d => d.departmentId === deptId)
}

function disabledPastDate(time: Date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return time.getTime() < today.getTime()
}

function shiftLabel(shift: number): string {
  return ['上午', '下午', '晚班'][shift] || '其他'
}

function shiftTag(shift: number): string {
  return ['warning', 'primary', 'info'][shift] || ''
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saveLoading.value = true
  try {
    await setScheduleApi({
      doctorId: form.doctorId,
      departmentId: form.departmentId,
      scheduleDate: form.scheduleDate,
      workShift: form.workShift,
      startTime: form.startTime,
      endTime: form.endTime,
      slotDuration: form.slotDuration,
      maxPatients: form.maxPatients
    })
    ElMessage.success('排班创建成功')
    resetForm()
  } catch {
    // handled by interceptor
  } finally {
    saveLoading.value = false
  }
}

function resetForm() {
  formRef.value?.resetFields()
  form.slotDuration = 30
  form.maxPatients = 20
  form.remark = ''
  filteredDoctors.value = []
}

async function handleQuery() {
  if (!queryDoctorId.value) {
    ElMessage.warning('请选择医生')
    return
  }

  queryLoading.value = true
  try {
    const res = await queryScheduleApi(queryDoctorId.value)
    schedules.value = (res.data as Schedule[]) || []
  } catch {
    schedules.value = []
  } finally {
    queryLoading.value = false
  }
}
</script>

<style scoped>
.schedule-page {
  max-width: 1200px;
}

.header-icon {
  color: var(--cb-primary);
  font-size: 18px;
}

.query-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.calc-hint {
  font-size: var(--cb-font-xs);
  color: var(--cb-text-placeholder);
  margin-left: 8px;
}
</style>
