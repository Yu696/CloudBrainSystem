<template>
  <div class="profile-page">
    <el-card shadow="never">
      <template #header>
        <span class="card-title">个人信息</span>
      </template>
      <el-form ref="infoFormRef" :model="infoForm" :rules="infoRules" label-width="100px" style="max-width: 480px">
        <el-form-item label="用户名">
          <el-input :model-value="userStore.userInfo?.userName" disabled />
        </el-form-item>
        <el-form-item label="用户类型">
          <el-tag :type="userStore.isAdmin ? 'danger' : 'success'">
            {{ userTypeLabel }}
          </el-tag>
        </el-form-item>
        <el-form-item label="角色">
          <el-tag>{{ userStore.role || '未分配' }}</el-tag>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="infoForm.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="infoForm.phone" maxlength="11" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="infoForm.email" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saveLoading" @click="handleUpdate">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 24px">
      <template #header>
        <span class="card-title">修改密码</span>
      </template>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" style="max-width: 480px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="pwdLoading" @click="handleResetPwd">确认修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateUserApi, resetPasswordApi } from '@/api/user'

const userStore = useUserStore()

const userTypeLabel = computed(() => {
  const map: Record<number, string> = { 0: '医生', 1: '管理员', 2: '患者' }
  return map[userStore.userInfo?.userType ?? 2] || '未知'
})

// 个人信息表单
const infoFormRef = ref<FormInstance>()
const saveLoading = ref(false)
const infoForm = reactive({
  realName: '',
  phone: '',
  email: ''
})

const infoRules: FormRules = {
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

onMounted(async () => {
  // 从后端拉取最新用户信息（确保角色等数据已更新）
  if (userStore.isLoggedIn) {
    await userStore.fetchUserInfo()
  }
  if (userStore.userInfo) {
    infoForm.realName = userStore.userInfo.realName || ''
    infoForm.phone = userStore.userInfo.phone || ''
    infoForm.email = userStore.userInfo.email || ''
  }
})

async function handleUpdate() {
  const valid = await infoFormRef.value?.validate().catch(() => false)
  if (!valid) return

  saveLoading.value = true
  try {
    await updateUserApi({
      realName: infoForm.realName,
      phone: infoForm.phone,
      email: infoForm.email
    })
    ElMessage.success('修改成功')
    await userStore.fetchUserInfo()
  } catch {
    // 已处理
  } finally {
    saveLoading.value = false
  }
}

// 修改密码
const pwdFormRef = ref<FormInstance>()
const pwdLoading = ref(false)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: ''
})

const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

async function handleResetPwd() {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return

  pwdLoading.value = true
  try {
    await resetPasswordApi({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码重置成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
  } catch {
    // 已处理
  } finally {
    pwdLoading.value = false
  }
}
</script>

<style scoped>
.profile-page {
  max-width: 720px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}
</style>
