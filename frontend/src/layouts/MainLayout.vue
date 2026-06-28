<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '240px'" class="layout-aside">
      <div class="logo-area">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z" stroke-linejoin="round"/>
            <path d="M2 17l10 5 10-5" stroke-linejoin="round"/>
            <path d="M2 12l10 5 10-5" stroke-linejoin="round"/>
          </svg>
        </div>
        <transition name="fade">
          <span v-if="!isCollapse" class="logo-text">云脑诊疗平台</span>
        </transition>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :router="true"
        class="el-menu--dark"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <template #title>个人信息</template>
        </el-menu-item>

        <!-- ===== 管理员菜单 ===== -->
        <template v-if="userStore.isAdmin">
          <el-menu-item index="/admin/departments">
            <el-icon><HomeFilled /></el-icon>
            <template #title>科室管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/doctors">
            <el-icon><FirstAidKit /></el-icon>
            <template #title>医生管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/appointments">
            <el-icon><Calendar /></el-icon>
            <template #title>预约管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/medical-records">
            <el-icon><Document /></el-icon>
            <template #title>病历管理</template>
          </el-menu-item>

          <el-sub-menu index="patient">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span>患者档案</span>
            </template>
            <el-menu-item index="/patient/list">
              <el-icon><List /></el-icon>
              <template #title>患者列表</template>
            </el-menu-item>
            <el-menu-item index="/patient/create">
              <el-icon><Plus /></el-icon>
              <template #title>新建档案</template>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="admin">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/admin/schedule">
              <el-icon><Calendar /></el-icon>
              <template #title>排班管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/users">
              <el-icon><UserFilled /></el-icon>
              <template #title>用户管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/role">
              <el-icon><User /></el-icon>
              <template #title>角色管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/permission">
              <el-icon><Key /></el-icon>
              <template #title>权限管理</template>
            </el-menu-item>
            <el-sub-menu index="ai-config" class="nested-submenu">
              <template #title>
                <el-icon><MagicStick /></el-icon>
                <span>AI 配置</span>
              </template>
              <el-menu-item index="/admin/ai/prompts">
                <el-icon><Document /></el-icon>
                <template #title>Prompt 模板</template>
              </el-menu-item>
              <el-menu-item index="/admin/ai/disease-kb">
                <el-icon><Reading /></el-icon>
                <template #title>疾病知识库</template>
              </el-menu-item>
              <el-menu-item index="/admin/ai/monitor">
                <el-icon><DataAnalysis /></el-icon>
                <template #title>AI 调用监控</template>
              </el-menu-item>
            </el-sub-menu>
          </el-sub-menu>
        </template>

        <!-- ===== 医生菜单 ===== -->
        <template v-if="userStore.isDoctor">
          <el-sub-menu index="patient">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span>患者档案</span>
            </template>
            <el-menu-item index="/patient/list">
              <el-icon><List /></el-icon>
              <template #title>患者列表</template>
            </el-menu-item>
            <el-menu-item index="/patient/create">
              <el-icon><Plus /></el-icon>
              <template #title>新建档案</template>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="doctor-sub">
            <template #title>
              <el-icon><FirstAidKit /></el-icon>
              <span>诊疗中心</span>
            </template>
            <el-menu-item index="/doctor/waiting">
              <el-icon><List /></el-icon>
              <template #title>待诊列表</template>
            </el-menu-item>
            <el-menu-item index="/doctor/history">
              <el-icon><Clock /></el-icon>
              <template #title>已诊列表</template>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="ai-tools">
            <template #title>
              <el-icon><MagicStick /></el-icon>
              <span>AI 辅助工具</span>
            </template>
            <el-menu-item index="/patient/list">
              <el-icon><UserFilled /></el-icon>
              <template #title>选择患者</template>
            </el-menu-item>
            <el-menu-item index="/doctor/ai-tools">
              <el-icon><MagicStick /></el-icon>
              <template #title>AI 功能面板</template>
            </el-menu-item>
          </el-sub-menu>
        </template>

        <!-- ===== 患者菜单 ===== -->
        <template v-if="userStore.isPatient">
          <el-sub-menu index="appointment">
            <template #title>
              <el-icon><Calendar /></el-icon>
              <span>预约挂号</span>
            </template>
            <el-menu-item index="/appointment/dept">
              <el-icon><HomeFilled /></el-icon>
              <template #title>科室选择</template>
            </el-menu-item>
            <el-menu-item index="/appointment/records">
              <el-icon><List /></el-icon>
              <template #title>挂号记录</template>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="medical-sub">
            <template #title>
              <el-icon><FirstAidKit /></el-icon>
              <span>我的诊疗</span>
            </template>
            <el-menu-item index="/patient/records">
              <el-icon><Document /></el-icon>
              <template #title>我的病历</template>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="ai-services">
            <template #title>
              <el-icon><MagicStick /></el-icon>
              <span>AI 智能服务</span>
            </template>
            <el-menu-item index="/ai/triage">
              <el-icon><Search /></el-icon>
              <template #title>智能分诊</template>
            </el-menu-item>
            <el-menu-item index="/ai/triage-history">
              <el-icon><Clock /></el-icon>
              <template #title>分诊历史</template>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>

    <!-- 主内容 -->
    <el-container>
      <!-- 顶栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="›" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title as string }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="user-greeting">你好，{{ userStore.userInfo?.realName || userStore.userInfo?.userName }}</span>
          <el-dropdown trigger="click">
            <span class="user-avatar-wrapper">
              <el-avatar :size="34" icon="UserFilled" class="user-avatar" />
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/profile')">
                  <el-icon><User /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Odometer, User, Setting, UserFilled, Key,
  Fold, Expand, ArrowDown, SwitchButton,
  Calendar, HomeFilled, List, FirstAidKit, Document, Plus, Clock,
  MagicStick, Search, Reading, DataAnalysis
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isCollapse = ref(false)
const activeMenu = computed(() => route.path)

function handleLogout() {
  ElMessageBox.confirm('确定退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    userStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.layout-container {
  height: 100%;
}

/* ===== 侧边栏 ===== */
.layout-aside {
  background: var(--cb-sidebar-bg);
  overflow-y: auto;
  overflow-x: hidden;
  transition: width 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  z-index: 10;
  display: flex;
  flex-direction: column;
}

.logo-area {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  font-weight: 700;
  font-size: 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
  padding: 0 16px;
  overflow: hidden;
}

.logo-icon {
  color: var(--cb-primary-light);
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.logo-text {
  font-size: 17px;
  white-space: nowrap;
  letter-spacing: 1px;
}

.layout-aside .el-menu {
  border-right: none;
  flex: 1;
}

.layout-aside .el-menu .el-menu-item,
.layout-aside .el-menu .el-sub-menu__title {
  transition: all 0.2s ease;
}

.layout-aside .el-menu .el-menu-item.is-active {
  background: var(--cb-primary) !important;
}

/* ===== 顶栏 ===== */
/* 三级嵌套菜单缩进 */
.layout-aside .nested-submenu .el-menu-item {
  padding-left: 64px !important;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--cb-white);
  border-bottom: 1px solid var(--cb-border);
  padding: 0 24px;
  height: 64px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: var(--cb-text-secondary);
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
}
.collapse-btn:hover {
  color: var(--cb-primary);
  background: var(--cb-primary-lighter);
}

.breadcrumb {
  font-size: var(--cb-font-base);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-greeting {
  font-size: var(--cb-font-sm);
  color: var(--cb-text-secondary);
}

.user-avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 2px 8px 2px 2px;
  border-radius: 20px;
  transition: background 0.2s;
}
.user-avatar-wrapper:hover {
  background: var(--cb-primary-lighter);
}

.user-avatar {
  background: linear-gradient(135deg, var(--cb-primary), var(--cb-primary-light)) !important;
}

.dropdown-icon {
  font-size: 12px;
  color: var(--cb-text-secondary);
}

/* ===== 内容区 ===== */
.layout-main {
  background: var(--cb-background);
  padding: 24px;
  overflow-y: auto;
}

/* 页面切换动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.25s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(12px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}
</style>
