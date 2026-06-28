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
                <el-radio-group v-model="form.workShift" @change="handleShiftChange">
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
                  :disabled-hours="disabledStartHours"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="结束时间" prop="endTime">
                <el-time-picker
                  v-model="form.endTime"
                  placeholder="结束时间"
                  format="HH:mm"
                  value-format="HH:mm:ss"
                  :disabled-hours="disabledEndHours"
                  :disabled-minutes="disabledEndMinutes"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="时段长度">
                <el-input-number
                  v-model="form.slotDuration"
                  :min="5"
                  :max="maxSlotDuration"
                  :step="5"
                  :disabled="!form.startTime || !form.endTime"
                /> 分钟
                <span class="calc-hint">（最少 5 分钟，最多 {{ maxSlotDuration }} 分钟）</span>
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
                  :label="(doc.title || '医师') + ' - ' + (doc.realName || doc.doctorId)"
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
              style="width: 100%"
            >
              <el-table-column prop="scheduleDate" label="日期" width="100" />
              <el-table-column label="班次" width="65">
                <template #default="{ row }">
                  <el-tag :type="shiftTag(row.workShift)" size="small">
                    {{ shiftLabel(row.workShift) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="时间" min-width="120">
                <template #default="{ row }">
                  {{ row.startTime }} - {{ row.endTime }}
                </template>
              </el-table-column>
              <el-table-column prop="slotDuration" label="时段长度" width="75" align="center" />
              <el-table-column prop="availableSlots" label="余号" width="65" align="center" />
              <el-table-column prop="status" label="状态" width="65">
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

// 各班次的默认时间范围
const SHIFT_DEFAULTS: Record<number, { start: string; end: string; label: string }> = {
  0: { start: '08:00:00', end: '12:00:00', label: '上午' },
  1: { start: '14:00:00', end: '17:00:00', label: '下午' },
  2: { start: '18:00:00', end: '21:00:00', label: '晚班' }
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
  startTime: '08:00:00',
  endTime: '12:00:00',
  slotDuration: 30,
  maxPatients: 20,
  remark: ''
})

// 获取当前班次的小时范围 [minHour, maxHour]
function getShiftHourRange(shift: number): [number, number] {
  switch (shift) {
    case 0: return [8, 12]    // 上午 08:00-12:00
    case 1: return [14, 17]   // 下午 14:00-17:00
    case 2: return [18, 21]   // 晚班 18:00-21:00
    default: return [0, 23]
  }
}

// 班次小时范围（响应式）
const shiftHourRange = computed(() => getShiftHourRange(form.workShift))

// 开始时间禁用的小时（班次范围外不可选）
const disabledStartHours = computed(() => {
  return () => {
    const [min, max] = shiftHourRange.value
    return Array.from({ length: 24 }, (_, i) => i).filter(h => h < min || h >= max)
  }
})

// 结束时间禁用的小时（班次范围外不可选）
const disabledEndHours = computed(() => {
  return () => {
    const [min, max] = shiftHourRange.value
    return Array.from({ length: 24 }, (_, i) => i).filter(h => h < min || h > max)
  }
})

// 结束时间禁用的分钟（防止结束小于开始 + 班次边界分钟限制）
const disabledEndMinutes = computed(() => {
  return (hour: number) => {
    if (!form.startTime) return []
    const [sh, sm] = form.startTime.split(':').map(Number)
    const [, maxHour] = shiftHourRange.value

    const disabled: Set<number> = new Set()

    // hour < 开始小时：整个小时禁用
    if (hour < sh) return Array.from({ length: 60 }, (_, i) => i)

    // hour === 开始小时：禁用 ≤ 开始分钟的分钟
    if (hour === sh) {
      for (let m = 0; m <= sm; m++) disabled.add(m)
    }

    // hour === 班次最大小时：只允许 :00
    if (hour === maxHour) {
      for (let m = 1; m < 60; m++) disabled.add(m)
    }

    return Array.from(disabled)
  }
})

// 班次切换时自动填充默认时间
function handleShiftChange(shift: number) {
  const def = SHIFT_DEFAULTS[shift]
  if (def) {
    form.startTime = def.start
    form.endTime = def.end
  }
}

// 总分钟数
const totalMinutes = computed(() => {
  if (!form.startTime || !form.endTime) return 0
  const [sh, sm] = form.startTime.split(':').map(Number)
  const [eh, em] = form.endTime.split(':').map(Number)
  return Math.max(0, (eh * 60 + em) - (sh * 60 + sm))
})

// 时段长度最大值
const maxSlotDuration = computed(() => {
  return Math.max(totalMinutes.value, 5)
})

// 最大人数 = 总分钟 ÷ 时段长度
const maxPatients = computed(() => {
  if (!totalMinutes.value || !form.slotDuration) return 0
  return Math.floor(totalMinutes.value / form.slotDuration)
})

// startTime/endTime 变化时，如果 slotDuration 超过新的总时长则自动回调
watch(
  [() => form.startTime, () => form.endTime],
  () => {
    if (totalMinutes.value > 0 && form.slotDuration > totalMinutes.value) {
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
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (form.startTime && value) {
          const [sh, sm] = form.startTime.split(':').map(Number)
          const [eh, em] = value.split(':').map(Number)
          const startMin = sh * 60 + sm
          const endMin = eh * 60 + em
          if (eh < sh || (eh === sh && em <= sm)) {
            callback(new Error('结束时间必须晚于开始时间'))
          } else if (endMin - startMin < 5) {
            callback(new Error('结束时间与开始时间至少相差 5 分钟'))
          }
        }
        callback()
      },
      trigger: 'change'
    }
  ]
}

// Query state
const queryDoctorId = ref('')

const departments = ref<Department[]>([])
const allDoctors = ref<Doctor[]>([])
const filteredDoctors = ref<Doctor[]>([])
const schedules = ref<Schedule[]>([])

async function handleQuery() {
  if (!queryDoctorId.value) {
    ElMessage.warning('请先选择医生')
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
  // 恢复上午默认时间
  handleShiftChange(0)
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
