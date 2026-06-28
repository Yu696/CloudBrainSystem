<template>
  <div class="doctor-page">
    <div class="page-title">
      <el-button text @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <span>{{ deptName }} — 选择医生</span>
    </div>

    <el-row :gutter="20">
      <!-- 左侧：医生列表 -->
      <el-col :span="deptId && selectedDoctor ? 14 : 24">
        <div v-loading="doctorLoading" class="doctor-grid">
          <div
            v-for="doc in doctors"
            :key="doc.doctorId"
            class="doctor-card"
            :class="{ active: selectedDoctor?.doctorId === doc.doctorId }"
            @click="selectDoctor(doc)"
          >
            <div class="doc-avatar-wrapper">
              <el-avatar :size="56" class="doc-avatar">{{ doc.title?.[0] || '医' }}</el-avatar>
            </div>
            <div class="doc-info">
              <div class="doc-name">{{ doc.realName || doc.title || '医生' }}</div>
              <div class="doc-title-row">
                <el-tag size="small" effect="plain">{{ doc.title || '医师' }}</el-tag>
                <span class="doc-fee">{{ doc.consultationFee ? '¥' + doc.consultationFee : '免费' }}</span>
              </div>
              <div class="doc-specialty">{{ doc.specialty || '暂无介绍' }}</div>
            </div>
          </div>

          <el-empty v-if="!doctorLoading && doctors.length === 0" description="暂无医生数据" :image-size="80" />
        </div>
      </el-col>

      <!-- 右侧：时段选择 -->
      <el-col v-if="deptId && selectedDoctor" :span="10">
        <div class="cb-card slot-card">
          <div class="cb-card-header">
            <el-icon class="header-icon"><Timer /></el-icon>
            <span>选择预约时间</span>
          </div>
          <div class="cb-card-body">
            <div class="slot-date-picker">
              <label class="slot-label">就诊日期</label>
              <el-date-picker
                v-model="selectedDate"
                type="date"
                placeholder="选择日期"
                :disabled-date="disabledDate"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                @change="handleDateChange"
              />
            </div>

            <!-- 排班信息 -->
            <div v-if="scheduleInfo" class="schedule-info">
              <div class="schedule-shift">
                <el-tag :type="shiftTagType(scheduleInfo.workShift)" size="small">
                  {{ shiftLabel(scheduleInfo.workShift) }}
                </el-tag>
                <span class="schedule-time">{{ formatScheduleTime(scheduleInfo.startTime) }} - {{ formatScheduleTime(scheduleInfo.endTime) }}</span>
              </div>
              <div class="schedule-slots-remain">
                剩余可预约：<strong>{{ scheduleInfo.availableSlots }}</strong> 个
              </div>
            </div>

            <!-- 可用时段 -->
            <div v-if="availableSlots.length > 0" class="slot-list">
              <label class="slot-label">可用时段</label>
              <div class="slot-grid">
                <div
                  v-for="slot in availableSlots"
                  :key="slot.slotId"
                  class="slot-item"
                  :class="{ selected: selectedSlot?.slotId === slot.slotId }"
                  @click="selectSlot(slot)"
                >
                  <span class="slot-time">{{ formatSlotTime(slot) }}</span>
                  <span class="slot-status">可预约</span>
                </div>
              </div>
            </div>

            <div v-else-if="selectedDate && !slotLoading" class="no-slots">
              <el-empty description="该日期暂无可用时段" :image-size="60" />
            </div>

            <div v-loading="slotLoading" class="slot-loading" />

            <!-- 确认按钮 -->
            <div v-if="selectedSlot" class="slot-confirm">
              <el-divider />
              <div class="selected-info">
                <div class="selected-row">
                  <span class="selected-label">医生</span>
                  <span class="selected-value">{{ selectedDoctor.title ? selectedDoctor.title + ' - ' + selectedDoctor.realName : (selectedDoctor.realName || '医生') }}</span>
                </div>
                <div class="selected-row">
                  <span class="selected-label">日期</span>
                  <span class="selected-value">{{ selectedDate }}</span>
                </div>
                <div class="selected-row">
                  <span class="selected-label">时段</span>
                  <span class="selected-value">{{ formatSlotTime(selectedSlot) }}</span>
                </div>
              </div>
              <el-button type="primary" class="confirm-btn" @click="goToConfirm" round>
                下一步
              </el-button>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Timer } from '@element-plus/icons-vue'
import { listDoctorsApi, queryScheduleApi, getAvailableSlotsApi } from '@/api/appointment'

interface Doctor {
  doctorId: string
  userId: string
  departmentId: string
  realName: string
  title: string
  specialty: string
  introduction: string
  consultationFee: number
  maxDailyPatients: number
  available: number
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

interface TimeSlot {
  slotId: string
  scheduleId: string
  doctorId: string
  startTime: string
  endTime: string
  status: number
}

const router = useRouter()
const route = useRoute()

const deptId = computed(() => route.query.deptId as string)
const deptName = computed(() => route.query.deptName as string || '科室')
const fromTriage = computed(() => route.query.fromTriage === 'true')
const targetDoctorId = computed(() => route.query.doctorId as string)

const doctors = ref<Doctor[]>([])
const doctorLoading = ref(false)
const selectedDoctor = ref<Doctor | null>(null)
const selectedDate = ref('')
const scheduleInfo = ref<Schedule | null>(null)
const availableSlots = ref<TimeSlot[]>([])
const selectedSlot = ref<TimeSlot | null>(null)
const slotLoading = ref(false)

onMounted(async () => {
  if (!deptId.value) {
    ElMessage.warning('请先选择科室')
    router.push('/appointment/dept')
    return
  }
  await loadDoctors()
})

function goBack() {
  router.push('/appointment/dept')
}

async function loadDoctors() {
  doctorLoading.value = true
  try {
    const res = await listDoctorsApi(deptId.value)
    doctors.value = (res.data as Doctor[]) || []

    // 从智能分诊跳转过来时，自动选中目标医生
    if (fromTriage.value && targetDoctorId.value) {
      const target = doctors.value.find(d => d.doctorId === targetDoctorId.value)
      if (target) {
        selectedDoctor.value = target
      }
    }
  } catch {
    doctors.value = []
  } finally {
    doctorLoading.value = false
  }
}

async function selectDoctor(doc: Doctor) {
  selectedDoctor.value = doc
  selectedDate.value = ''
  scheduleInfo.value = null
  availableSlots.value = []
  selectedSlot.value = null
}

function disabledDate(time: Date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const maxDate = new Date()
  maxDate.setDate(maxDate.getDate() + 30)
  return time.getTime() < today.getTime() || time.getTime() > maxDate.getTime()
}

async function handleDateChange(date: string) {
  if (!date || !selectedDoctor.value) return
  selectedDate.value = date
  selectedSlot.value = null
  slotLoading.value = true

  try {
    // 先查排班
    const scheduleRes = await queryScheduleApi(selectedDoctor.value.doctorId, date, date)
    const schedules = (scheduleRes.data as Schedule[]) || []
    if (schedules.length > 0) {
      scheduleInfo.value = schedules[0]
    } else {
      scheduleInfo.value = null
    }

    // 再查可用时段
    const slotRes = await getAvailableSlotsApi(selectedDoctor.value.doctorId, date)
    availableSlots.value = (slotRes.data as TimeSlot[]) || []
  } catch {
    scheduleInfo.value = null
    availableSlots.value = []
  } finally {
    slotLoading.value = false
  }
}

function selectSlot(slot: TimeSlot) {
  selectedSlot.value = slot
}

function formatScheduleTime(time: string): string {
  if (!time) return ''
  return time.length >= 5 ? time.substring(0, 5) : time
}

function formatSlotTime(slot: TimeSlot): string {
  if (!slot.startTime) return ''
  const start = slot.startTime.substring(11, 16)
  const end = slot.endTime.substring(11, 16)
  return `${start} - ${end}`
}

function shiftLabel(shift: number): string {
  const map: Record<number, string> = { 0: '上午', 1: '下午', 2: '晚班' }
  return map[shift] || '其他'
}

function shiftTagType(shift: number): string {
  const map: Record<number, string> = { 0: 'warning', 1: 'primary', 2: 'info' }
  return map[shift] || ''
}

function goToConfirm() {
  if (!selectedDoctor.value || !selectedSlot.value || !selectedDate.value) return
  router.push({
    path: '/appointment/confirm',
    query: {
      doctorId: selectedDoctor.value.doctorId,
      doctorName: (selectedDoctor.value.title || '医生') + ' ' + selectedDoctor.value.doctorId,
      deptId: deptId.value,
      deptName: deptName.value,
      slotId: selectedSlot.value.slotId,
      slotTime: formatSlotTime(selectedSlot.value),
      date: selectedDate.value,
      fee: String(selectedDoctor.value.consultationFee || 0),
      doctorTitle: selectedDoctor.value.title || ''
    }
  })
}
</script>

<style scoped>
.doctor-page {
  max-width: 1200px;
}

.back-btn {
  font-size: 18px;
  padding: 4px 8px;
  margin-right: 4px;
}

.doctor-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.doctor-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--cb-white);
  border-radius: var(--cb-radius-lg);
  box-shadow: var(--cb-shadow-sm);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.doctor-card:hover {
  box-shadow: var(--cb-shadow-md);
  border-color: var(--cb-primary-lighter);
}

.doctor-card.active {
  border-color: var(--cb-primary);
  background: var(--cb-primary-lighter);
}

.doc-avatar-wrapper {
  flex-shrink: 0;
}

.doc-avatar {
  background: linear-gradient(135deg, var(--cb-primary), var(--cb-primary-light)) !important;
  font-size: 20px;
  font-weight: 600;
}

.doc-info {
  flex: 1;
  min-width: 0;
}

.doc-name {
  font-size: var(--cb-font-lg);
  font-weight: 600;
  color: var(--cb-text-primary);
  margin-bottom: 6px;
}

.doc-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.doc-fee {
  font-size: var(--cb-font-sm);
  color: var(--cb-danger);
  font-weight: 600;
}

.doc-specialty {
  font-size: var(--cb-font-xs);
  color: var(--cb-text-secondary);
  line-height: 1.5;
}

/* 右侧卡片 */
.slot-card {
  position: sticky;
  top: 24px;
}

.header-icon {
  color: var(--cb-primary);
  font-size: 18px;
}

.slot-label {
  display: block;
  font-size: var(--cb-font-sm);
  font-weight: 600;
  color: var(--cb-text-primary);
  margin-bottom: 8px;
}

.slot-date-picker {
  margin-bottom: 16px;
}

.schedule-info {
  padding: 12px 16px;
  background: var(--cb-primary-lighter);
  border-radius: var(--cb-radius-md);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.schedule-shift {
  display: flex;
  align-items: center;
  gap: 8px;
}

.schedule-time {
  font-size: var(--cb-font-sm);
  color: var(--cb-text-secondary);
}

.schedule-slots-remain {
  font-size: var(--cb-font-xs);
  color: var(--cb-text-secondary);
}

.schedule-slots-remain strong {
  color: var(--cb-primary);
  font-size: var(--cb-font-base);
}

.slot-list {
  margin-bottom: 16px;
}

.slot-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.slot-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 8px;
  border: 1px solid var(--cb-border);
  border-radius: var(--cb-radius-md);
  cursor: pointer;
  transition: all 0.2s;
}

.slot-item:hover {
  border-color: var(--cb-primary-light);
  background: var(--cb-primary-lighter);
}

.slot-item.selected {
  border-color: var(--cb-primary);
  background: var(--cb-primary);
  color: #fff;
}

.slot-item.selected .slot-status {
  color: rgba(255, 255, 255, 0.85);
}

.slot-time {
  font-size: var(--cb-font-base);
  font-weight: 600;
}

.slot-status {
  font-size: var(--cb-font-xs);
  color: var(--cb-success);
}

.no-slots {
  padding: 20px 0;
}

.slot-loading {
  min-height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.slot-confirm {
  margin-top: 8px;
}

.selected-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.selected-row {
  display: flex;
  justify-content: space-between;
  font-size: var(--cb-font-base);
}

.selected-label {
  color: var(--cb-text-secondary);
}

.selected-value {
  color: var(--cb-text-primary);
  font-weight: 500;
}

.confirm-btn {
  width: 100%;
  height: 42px;
  font-size: var(--cb-font-lg);
  letter-spacing: 2px;
}
</style>
