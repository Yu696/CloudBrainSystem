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

    <!-- 快捷操作 + 信息 -->
    <el-row :gutter="20">
      <el-col :span="16">
        <div class="cb-card">
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
        <div class="cb-card">
          <div class="cb-card-header">
            <span>快捷导航</span>
          </div>
          <div class="cb-card-body">
            <div class="quick-links">
              <el-button text class="quick-link-item" @click="router.push('/profile')">
                <el-icon><User /></el-icon>
                <span>个人信息</span>
              </el-button>
              <el-button v-if="userStore.isAdmin" text class="quick-link-item" @click="router.push('/admin/role')">
                <el-icon><UserFilled /></el-icon>
                <span>角色管理</span>
              </el-button>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, UserFilled, Document, TrendCharts } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

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

const stats = [
  { label: '今日挂号', value: 0, icon: Document },
  { label: '待诊人数', value: 0, icon: User },
  { label: '本月新患', value: 0, icon: TrendCharts },
  { label: '待处理', value: 0, icon: UserFilled }
]
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

/* 平台信息 */
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
