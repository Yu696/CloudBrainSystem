<template>
  <div class="profile-page">
    <!-- 用户信息头部 -->
    <div class="profile-header cb-card">
      <div class="cb-card-body profile-header-body">
        <div class="profile-avatar-section">
          <el-avatar :size="80" icon="UserFilled" class="profile-avatar" />
          <div class="profile-basic">
            <h3>{{ userStore.userInfo?.realName || userStore.userInfo?.userName }}</h3>
            <p>
              <el-tag size="small" :type="userStore.isAdmin ? 'danger' : 'primary'">
                {{ userTypeLabel }}
              </el-tag>
              <el-tag size="small" style="margin-left: 8px">
                {{ userStore.role || '未分配' }}
              </el-tag>
            </p>
          </div>
        </div>
        <div class="profile-meta">
          <div class="meta-item">
            <span class="meta-label">用户名</span>
            <span class="meta-value">{{ userStore.userInfo?.userName }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">手机号</span>
            <span class="meta-value">{{ userStore.userInfo?.phone || '未设置' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">邮箱</span>
            <span class="meta-value">{{ userStore.userInfo?.email || '未设置' }}</span>
          </div>
        </div>
      </div>
    </div>

    <el-row :gutter="24">
      <!-- 个人信息编辑 -->
      <el-col :span="isDoctor ? 8 : 14">
        <div class="cb-card">
          <div class="cb-card-header">
            <el-icon class="header-icon"><Edit /></el-icon>
            <span>编辑个人信息</span>
          </div>
          <div class="cb-card-body">
            <el-form ref="infoFormRef" :model="infoForm" :rules="infoRules" label-width="80px" class="form-section">
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="infoForm.realName" placeholder="请输入真实姓名" />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="infoForm.phone" maxlength="11" placeholder="请输入手机号" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="infoForm.email" placeholder="请输入邮箱地址" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="saveLoading" @click="handleUpdate" style="width: 100%">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-col>

      <!-- 医生信息编辑 -->
      <el-col v-if="isDoctor" :span="8">
        <div class="cb-card">
          <div class="cb-card-header">
            <el-icon class="header-icon"><UserFilled /></el-icon>
            <span>医生信息</span>
          </div>
          <div class="cb-card-body">
            <el-form :model="docForm" label-width="0" class="form-section">
              <el-form-item label="" class="compact-item">
                <label class="doc-field-label">专长</label>
                <el-input v-model="docForm.specialty" placeholder="如：心血管疾病、呼吸系统疾病" maxlength="200" />
              </el-form-item>
              <el-form-item label="" class="compact-item">
                <label class="doc-field-label">个人简介</label>
                <el-input
                  v-model="docForm.introduction"
                  type="textarea"
                  :rows="5"
                  placeholder="输入个人简介"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="docSaveLoading" @click="handleUpdateDoctor" style="width: 100%">
                  保存医生信息
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-col>

      <!-- 修改密码 -->
      <el-col :span="isDoctor ? 8 : 10">
        <div class="cb-card">
          <div class="cb-card-header">
            <el-icon class="header-icon"><Lock /></el-icon>
            <span>修改密码</span>
          </div>
          <div class="cb-card-body">
            <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="0" class="form-section">
              <el-form-item prop="oldPassword">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="旧密码" />
              </el-form-item>
              <el-form-item prop="newPassword">
                <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="新密码" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="pwdLoading" @click="handleResetPwd" style="width: 100%">
                  确认修改
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit, Lock, UserFilled } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateUserApi, resetPasswordApi } from '@/api/user'
import { getDoctorByUserIdApi, updateDoctorApi } from '@/api/doctor'

const userStore = useUserStore()

const userTypeLabel = computed(() => {
  const map: Record<number, string> = { 0: '医生', 1: '管理员', 2: '患者', 3: '医生' }
  return map[userStore.userInfo?.userType ?? 2] || '未知'
})

const isDoctor = computed(() => {
  const type = userStore.userInfo?.userType
  return type === 0 || type === 3
})

// 医生信息表单
const docSaveLoading = ref(false)
const docForm = reactive({
  specialty: '',
  introduction: ''
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
  if (userStore.isLoggedIn) {
    await userStore.fetchUserInfo()
  }
  if (userStore.userInfo) {
    infoForm.realName = userStore.userInfo.realName || ''
    infoForm.phone = userStore.userInfo.phone || ''
    infoForm.email = userStore.userInfo.email || ''
  }
  if (isDoctor.value && userStore.userInfo?.userId) {
    try {
      const res = await getDoctorByUserIdApi(userStore.userInfo.userId)
      const doc = res.data
      if (doc) {
        docForm.specialty = doc.specialty || ''
        docForm.introduction = doc.introduction || ''
      }
    } catch {
      // 医生可能还未创建对应的 doctor 记录
    }
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

async function handleUpdateDoctor() {
  docSaveLoading.value = true
  try {
    await updateDoctorApi({
      userId: userStore.userInfo?.userId,
      specialty: docForm.specialty || undefined,
      introduction: docForm.introduction || undefined
    })
    ElMessage.success('医生信息更新成功')
  } catch {
    // 已处理
  } finally {
    docSaveLoading.value = false
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
  max-width: 1000px;
}

/* 用户信息头部 */
.profile-header {
  margin-bottom: 24px;
}

.profile-header-body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
}

.profile-avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-avatar {
  background: linear-gradient(135deg, var(--cb-primary), var(--cb-primary-light)) !important;
  flex-shrink: 0;
}

.profile-basic h3 {
  font-size: var(--cb-font-xl);
  color: var(--cb-text-primary);
  margin: 0 0 8px;
}

.profile-basic p {
  margin: 0;
}

.profile-meta {
  display: flex;
  gap: 32px;
  flex-shrink: 0;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  text-align: center;
}

.meta-label {
  font-size: var(--cb-font-xs);
  color: var(--cb-text-placeholder);
}

.meta-value {
  font-size: var(--cb-font-base);
  color: var(--cb-text-primary);
  font-weight: 500;
}

.header-icon {
  color: var(--cb-primary);
  font-size: 18px;
}

.form-section {
  max-width: 100%;
}

.doc-field-label {
  display: block;
  font-size: var(--cb-font-xs);
  color: var(--cb-text-placeholder);
  margin-bottom: 6px;
  font-weight: 500;
}

.compact-item {
  margin-bottom: 18px;
}

.compact-item:last-of-type {
  margin-bottom: 0;
}

/* 密码框无标签时的占位调整 */
.form-section :deep(.el-form-item__content) {
  width: 100%;
}
</style>
