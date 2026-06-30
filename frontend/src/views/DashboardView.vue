<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h2 class="welcome-title">欢迎回来，{{ userStore.userInfo?.realName || userStore.userInfo?.userName }}</h2>
        <p class="welcome-desc">{{ today }} · {{ roleText }}</p>
      </div>
      <div class="welcome-icon">
        <svg viewBox="0 0 24 24" width="64" height="64" fill="none" stroke="currentColor" stroke-width="1.2">
          <path d="M12 2L2 7l10 5 10-5-10-5z" stroke-linejoin="round"/>
          <path d="M2 17l10 5 10-5" stroke-linejoin="round"/>
          <path d="M2 12l10 5 10-5" stroke-linejoin="round"/>
          <circle cx="12" cy="12" r="3" fill="rgba(255,255,255,0.2)" stroke="none"/>
        </svg>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="12" :md="6" v-for="(stat, index) in stats" :key="stat.label">
        <div class="stat-card" :style="{ '--card-index': index }">
          <div class="stat-icon" :class="'stat-icon-' + index">
            <el-icon :size="28">
              <component :is="stat.icon" />
            </el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- AI 智能服务（患者端）/ 平台简介（管理员/医生端） + 快捷导航 -->
    <el-row :gutter="20" class="ai-nav-row">
      <el-col :span="16">
        <!-- 患者端：AI 智能服务卡片 -->
        <div v-if="userStore.isPatient" class="ai-card">
          <div class="ai-card-bg">
            <div class="ai-glow"></div>
            <div class="ai-particles">
              <span class="particle p1"></span>
              <span class="particle p2"></span>
              <span class="particle p3"></span>
              <span class="particle p4"></span>
            </div>
          </div>
          <div class="ai-card-header">
            <div class="ai-badge">
              <el-icon :size="18"><MagicStick /></el-icon>
              <span>AI</span>
            </div>
            <h3 class="ai-title">AI 智能服务</h3>
            <p class="ai-subtitle">基于深度求索大模型，为您提供智能分诊辅助</p>
          </div>
          <div class="ai-services">
            <div class="ai-service-item" @click="router.push('/ai/triage')">
              <div class="ai-service-icon triage-icon">
                <el-icon :size="28"><Search /></el-icon>
              </div>
              <div class="ai-service-info">
                <div class="ai-service-name">智能分诊</div>
                <div class="ai-service-desc">输入症状，AI 分析并推荐科室和医生</div>
              </div>
              <el-icon class="ai-arrow" :size="18"><ArrowRight /></el-icon>
            </div>
            <div class="ai-service-item" @click="router.push('/ai/triage-history')">
              <div class="ai-service-icon history-icon">
                <el-icon :size="28"><Clock /></el-icon>
              </div>
              <div class="ai-service-info">
                <div class="ai-service-name">分诊历史</div>
                <div class="ai-service-desc">查看过往的智能分诊记录和结果</div>
              </div>
              <el-icon class="ai-arrow" :size="18"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>

        <!-- 管理员/医生端：平台简介 -->
        <div v-else class="cb-card platform-card">
          <div class="cb-card-header">
            <span>平台简介</span>
          </div>
          <div class="cb-card-body">
            <div class="platform-info">
              <p>云脑诊疗平台是一套面向医疗机构的智慧诊疗管理系统，支持患者管理、预约挂号、诊疗记录、处方开具等全流程业务。</p>
              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label">当前用户</span>
                  <span class="info-value">{{ userStore.userInfo?.userName }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">角色</span>
                  <el-tag size="small" :type="userStore.isAdmin ? 'danger' : 'primary'">
                    {{ roleText }}
                  </el-tag>
                </div>
                <div class="info-item">
                  <span class="info-label">系统版本</span>
                  <span class="info-value">v1.0.0</span>
                </div>
                <div class="info-item">
                  <span class="info-label">运行状态</span>
                  <el-tag size="small" type="success">正常运行</el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="cb-card nav-card">
          <div class="cb-card-header">
            <span>快捷导航</span>
          </div>
          <div class="cb-card-body">
            <div class="quick-links">
              <el-button text class="quick-link-item" @click="router.push('/profile')">
                <el-icon><User /></el-icon>
                <span>个人信息</span>
              </el-button>

              <!-- 管理员快捷导航 -->
              <template v-if="userStore.isAdmin">
                <el-button text class="quick-link-item" @click="router.push('/admin/departments')">
                  <el-icon><HomeFilled /></el-icon>
                  <span>科室管理</span>
                </el-button>
                <el-button text class="quick-link-item" @click="router.push('/admin/doctors')">
                  <el-icon><FirstAidKit /></el-icon>
                  <span>医生管理</span>
                </el-button>
                <el-button text class="quick-link-item" @click="router.push('/admin/appointments')">
                  <el-icon><Calendar /></el-icon>
                  <span>预约管理</span>
                </el-button>
                <el-button text class="quick-link-item" @click="router.push('/admin/medical-records')">
                  <el-icon><Document /></el-icon>
                  <span>病历管理</span>
                </el-button>
                <el-button text class="quick-link-item" @click="router.push('/admin/users')">
                  <el-icon><UserFilled /></el-icon>
                  <span>用户管理</span>
                </el-button>
              </template>

              <!-- 医生快捷导航 -->
              <template v-if="userStore.isDoctor">
                <el-button text class="quick-link-item" @click="router.push('/doctor/waiting')">
                  <el-icon><List /></el-icon>
                  <span>待诊列表</span>
                </el-button>
                <el-button text class="quick-link-item" @click="router.push('/patient/list')">
                  <el-icon><UserFilled /></el-icon>
                  <span>患者列表</span>
                </el-button>
              </template>

              <!-- 患者快捷导航 -->
              <template v-if="userStore.isPatient">
                <el-button text class="quick-link-item" @click="router.push('/appointment/dept')">
                  <el-icon><Calendar /></el-icon>
                  <span>预约挂号</span>
                </el-button>
                <el-button text class="quick-link-item" @click="router.push('/appointment/records')">
                  <el-icon><List /></el-icon>
                  <span>挂号记录</span>
                </el-button>
                <el-button text class="quick-link-item" @click="router.push('/patient/records')">
                  <el-icon><Document /></el-icon>
                  <span>我的病历</span>
                </el-button>
              </template>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Document, TrendCharts, Calendar, List, HomeFilled, FirstAidKit, CircleCheck, CreditCard, MagicStick, Search, Clock, ArrowRight, Picture } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getDashboardStatsApi } from '@/api/appointment'

const router = useRouter()
const userStore = useUserStore()

const today = computed(() => {
  const d = new Date()
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${weekdays[d.getDay()]}`
})

const roleText = computed(() => {
  if (userStore.isAdmin) return '系统管理员'
  if (userStore.userInfo?.userType === 3) return '医师'
  if (userStore.userInfo?.userType === 0) return '医生'
  return '患者'
})

const statsData = ref<any>({})

const stats = computed(() => {
  const d = statsData.value
  if (userStore.isAdmin) {
    return [
      { label: '今日挂号', value: d.todayAppointments ?? 0, icon: Document },
      { label: '待诊人数', value: d.waitingCount ?? 0, icon: User },
      { label: '本月新患', value: d.monthNewPatients ?? 0, icon: TrendCharts },
      { label: '今日已诊', value: d.todayCompleted ?? 0, icon: CircleCheck }
    ]
  }
  if (userStore.isDoctor) {
    return [
      { label: '今日待诊', value: d.doctorTodayWaiting ?? 0, icon: User },
      { label: '今日已诊', value: d.doctorTodayCompleted ?? 0, icon: CircleCheck },
      { label: '本月接诊', value: d.doctorMonthCompleted ?? 0, icon: TrendCharts },
      { label: '累计患者', value: d.doctorTotalPatients ?? 0, icon: HomeFilled }
    ]
  }
  if (userStore.isRadiologist) {
    return [
      { label: '待上传影像', value: d.radiologistPendingUpload ?? 0, icon: Picture },
      { label: '待初诊', value: d.radiologistPendingReport ?? 0, icon: Document },
      { label: '本月已报', value: d.radiologistMonthReported ?? 0, icon: TrendCharts },
      { label: '累计报告', value: d.radiologistTotalReported ?? 0, icon: CircleCheck }
    ]
  }
  // patient
  return [
    { label: '累计挂号', value: d.patientTotalAppointments ?? 0, icon: Document },
    { label: '本月挂号', value: d.patientMonthAppointments ?? 0, icon: Calendar },
    { label: '我的病历', value: d.patientTotalRecords ?? 0, icon: List },
    { label: '待支付', value: d.patientUnpaidCount ?? 0, icon: CreditCard }
  ]
})

onMounted(async () => {
  try {
    const res = await getDashboardStatsApi()
    statsData.value = res.data
  } catch {
    // keep default zeros
  }
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, var(--cb-primary) 0%, var(--cb-primary-dark) 100%);
  border-radius: var(--cb-radius-lg);
  padding: 32px 36px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
  box-shadow: 0 8px 24px rgba(26, 127, 191, 0.25);
}

.welcome-title {
  font-size: var(--cb-font-2xl);
  font-weight: 700;
  margin: 0 0 6px;
}

.welcome-desc {
  font-size: var(--cb-font-base);
  opacity: 0.85;
  margin: 0;
}

.welcome-icon {
  opacity: 0.3;
  flex-shrink: 0;
}

/* 统计卡片 */
.stat-row {
  margin-bottom: 24px;
}

.stat-card {
  background: var(--cb-white);
  border-radius: var(--cb-radius-lg);
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: var(--cb-shadow-sm);
  transition: all 0.3s ease;
  cursor: pointer;
}
.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--cb-shadow-lg);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon-0 { background: linear-gradient(135deg, #e8f4fd, #b3d8f5); color: var(--cb-primary); }
.stat-icon-1 { background: linear-gradient(135deg, #fef0ef, #fcd5d0); color: #e74c3c; }
.stat-icon-2 { background: linear-gradient(135deg, #e8f8e8, #b8e6b8); color: var(--cb-success); }
.stat-icon-3 { background: linear-gradient(135deg, #fdf6ec, #fae6c8); color: var(--cb-warning); }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: var(--cb-font-3xl);
  font-weight: 700;
  color: var(--cb-text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: var(--cb-font-sm);
  color: var(--cb-text-secondary);
  margin-top: 4px;
}

/* ===== AI 智能服务卡片 ===== */
.ai-nav-row {
  align-items: stretch;
}

.ai-nav-row .el-col {
  display: flex;
}

.ai-nav-row .el-col > div {
  flex: 1;
}

.ai-card {
  position: relative;
  border-radius: var(--cb-radius-lg);
  overflow: hidden;
  background: linear-gradient(135deg, #f8f6ff 0%, #f0edff 30%, #e8f0fe 100%);
  color: var(--cb-text-primary);
  padding: 32px 36px;
  border: 1px solid rgba(139, 92, 246, 0.12);
  box-shadow: 0 4px 24px rgba(139, 92, 246, 0.08);
  display: flex;
  flex-direction: column;
}

.ai-card-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.ai-glow {
  position: absolute;
  top: -80px;
  right: -60px;
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.12) 0%, transparent 70%);
  border-radius: 50%;
  animation: ai-glow-pulse 3s ease-in-out infinite;
}

@keyframes ai-glow-pulse {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.1); }
}

.particle {
  position: absolute;
  border-radius: 50%;
  background: rgba(139, 92, 246, 0.25);
}
.p1 { width: 6px; height: 6px; top: 18%; right: 15%; animation: float-up 4s ease-in-out infinite; }
.p2 { width: 3px; height: 3px; top: 35%; left: 10%; animation: float-up 3.5s ease-in-out 0.6s infinite; }
.p3 { width: 5px; height: 5px; top: 50%; right: 25%; animation: float-up 5s ease-in-out 1.2s infinite; }
.p4 { width: 4px; height: 4px; bottom: 25%; left: 20%; animation: float-up 4.5s ease-in-out 0.3s infinite; }

@keyframes float-up {
  0%, 100% { transform: translateY(0) scale(1); opacity: 0.3; }
  50% { transform: translateY(-14px) scale(1.5); opacity: 0.8; }
}

.ai-card-header {
  position: relative;
  z-index: 1;
  margin-bottom: 24px;
}

.ai-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
  color: #fff;
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 1px;
  margin-bottom: 14px;
  box-shadow: 0 2px 10px rgba(139, 92, 246, 0.3);
}

.ai-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 6px;
  color: #1e1b4b;
  letter-spacing: 0.5px;
}

.ai-subtitle {
  font-size: var(--cb-font-sm);
  color: #6b7280;
  margin: 0;
}

.ai-services {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
}

.ai-service-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(139, 92, 246, 0.1);
  border-radius: var(--cb-radius-md);
  cursor: pointer;
  transition: all 0.25s ease;
}

.ai-service-item:hover {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(139, 92, 246, 0.35);
  box-shadow: 0 4px 16px rgba(139, 92, 246, 0.12);
  transform: translateX(4px);
}

.ai-service-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.triage-icon {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(59, 130, 246, 0.06));
  color: #3b82f6;
}

.history-icon {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.15), rgba(139, 92, 246, 0.06));
  color: #8b5cf6;
}

.ai-service-info {
  flex: 1;
}

.ai-service-name {
  font-size: var(--cb-font-base);
  font-weight: 600;
  margin-bottom: 2px;
}

.ai-service-desc {
  font-size: var(--cb-font-xs);
  color: #9ca3af;
}

.ai-arrow {
  opacity: 0.25;
  color: #8b5cf6;
  transition: opacity 0.25s;
}

.ai-service-item:hover .ai-arrow {
  opacity: 1;
}

/* 平台信息 */
.platform-card {
  display: flex;
  flex-direction: column;
}

.platform-card .cb-card-body {
  flex: 1;
}

.platform-info p {
  color: var(--cb-text-secondary);
  line-height: 1.8;
  margin: 0 0 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: var(--cb-font-xs);
  color: var(--cb-text-placeholder);
}

.info-value {
  font-size: var(--cb-font-base);
  color: var(--cb-text-primary);
  font-weight: 500;
}

/* 快捷导航 */
.nav-card {
  display: flex;
  flex-direction: column;
}

.nav-card .cb-card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.quick-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quick-link-item {
  display: flex !important;
  align-items: center;
  gap: 10px;
  padding: 12px 16px !important;
  border-radius: var(--cb-radius-md) !important;
  transition: all 0.2s;
  font-size: var(--cb-font-base) !important;
}

.quick-link-item:hover {
  background: var(--cb-primary-lighter) !important;
  color: var(--cb-primary) !important;
}
</style>
