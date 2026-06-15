<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card shadow="never" style="margin-top: 24px">
      <template #header>
        <span class="card-title">欢迎使用云脑诊疗平台</span>
      </template>
      <p style="color: #606266; line-height: 1.8">
        当前登录用户：<strong>{{ userStore.userInfo?.realName || userStore.userInfo?.userName }}</strong>
        &nbsp;|&nbsp; 角色：<el-tag size="small">{{ userStore.role || '未分配' }}</el-tag>
        &nbsp;|&nbsp; 类型：<el-tag :type="userStore.isAdmin ? 'danger' : 'success'" size="small">
          {{ userStore.isAdmin ? '管理员' : userStore.userInfo?.userType === 0 ? '医生' : '患者' }}
        </el-tag>
      </p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const stats = [
  { label: '今日挂号', value: 0 },
  { label: '待诊人数', value: 0 },
  { label: '本月新患', value: 0 },
  { label: '待处理', value: 0 }
]
</script>

<style scoped>
.stat-card {
  text-align: center;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  color: #409eff;
}

.stat-label {
  margin-top: 8px;
  font-size: 14px;
  color: #909399;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}
</style>
