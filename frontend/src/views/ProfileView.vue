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
      <!-- 左栏：基本信息 + 患者档案 -->
      <el-col :span="16">
        <!-- 编辑个人信息 -->
        <div class="cb-card">
          <div class="cb-card-header">
            <el-icon class="header-icon"><Edit /></el-icon>
            <span>编辑个人信息</span>
          </div>
          <div class="cb-card-body">
            <el-form ref="infoFormRef" :model="infoForm" :rules="infoRules" label-width="90px" class="form-section">
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="infoForm.realName" placeholder="请输入真实姓名" />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="infoForm.phone" maxlength="11" placeholder="请输入手机号" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="infoForm.email" placeholder="请输入邮箱地址" />
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- 患者档案（仅患者可见） -->
        <div v-if="userStore.isPatient" class="cb-card" style="margin-top: 20px">
          <div class="cb-card-header">
            <el-icon class="header-icon"><Document /></el-icon>
            <span>患者档案</span>
            <el-tag v-if="userStore.userInfo?.patientId" size="small" type="success" style="margin-left: 12px">
              病历号：{{ userStore.userInfo.medicalRecordNo }}
            </el-tag>
          </div>
          <div class="cb-card-body">
            <el-form ref="patientFormRef" :model="patientForm" :rules="patientRules" label-width="100px" class="form-section">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="姓名" prop="name">
                    <el-input v-model="patientForm.name" placeholder="请输入姓名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="性别" prop="gender">
                    <el-radio-group v-model="patientForm.gender">
                      <el-radio :value="1">男</el-radio>
                      <el-radio :value="0">女</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="身份证号" prop="idCard">
                    <el-input v-model="patientForm.idCard" maxlength="18" placeholder="请输入身份证号" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="出生日期" prop="birthDate">
                    <el-date-picker
                      v-model="patientForm.birthDate"
                      type="date"
                      placeholder="选择出生日期"
                      style="width: 100%"
                      value-format="YYYY-MM-DD"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="手机号" prop="phone">
                    <el-input v-model="patientForm.phone" maxlength="11" placeholder="请输入手机号" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="紧急联系人">
                    <el-input v-model="patientForm.emergencyPhone" maxlength="11" placeholder="紧急联系人手机号" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="地址">
                <el-input v-model="patientForm.address" placeholder="请输入居住地址" />
              </el-form-item>
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="血型">
                    <el-select v-model="patientForm.bloodType" placeholder="请选择血型" clearable style="width: 100%">
                      <el-option label="A 型" value="A" />
                      <el-option label="B 型" value="B" />
                      <el-option label="AB 型" value="AB" />
                      <el-option label="O 型" value="O" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="过敏史">
                <el-input v-model="patientForm.allergyHistory" type="textarea" :rows="2" placeholder="请输入过敏史，如无则填'无'" />
              </el-form-item>
              <el-form-item label="遗传病史">
                <el-input v-model="patientForm.geneticDiseases" type="textarea" :rows="2" placeholder="请输入遗传病史，如无则填'无'" />
              </el-form-item>
              <el-form-item label="既往病史">
                <el-input v-model="patientForm.medicalHistory" type="textarea" :rows="2" placeholder="请输入既往病史，如无则填'无'" />
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- 保存按钮 -->
        <div style="margin-top: 20px; text-align: right">
          <el-button type="primary" :loading="saveLoading" @click="handleUpdate">
            保存全部修改
          </el-button>
        </div>
      </el-col>

      <!-- 右栏：修改密码 -->
      <el-col :span="8">
        <div class="cb-card">
          <div class="cb-card-header">
            <el-icon class="header-icon"><Lock /></el-icon>
            <span>修改密码</span>
          </div>
          <div class="cb-card-body">
            <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px" class="form-section">
              <el-form-item label="旧密码" prop="oldPassword">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="输入旧密码" />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="输入新密码" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="pwdLoading" @click="handleResetPwd">
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
import { Edit, Lock, Document } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateUserApi, resetPasswordApi } from '@/api/user'

const userStore = useUserStore()

const userTypeLabel = computed(() => {
  const map: Record<number, string> = { 0: '医生', 1: '管理员', 2: '患者', 3: '医生' }
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

// 患者档案表单
const patientFormRef = ref<FormInstance>()
const patientForm = reactive({
  name: '',
  idCard: '',
  gender: undefined as number | undefined,
  birthDate: '' as string,
  phone: '',
  emergencyPhone: '',
  address: '',
  bloodType: '' as string,
  allergyHistory: '',
  geneticDiseases: '',
  medicalHistory: ''
})

const patientRules: FormRules = {
  idCard: [{ pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '身份证号格式不正确', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  emergencyPhone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

onMounted(async () => {
  if (userStore.isLoggedIn) {
    await userStore.fetchUserInfo()
  }
  const info = userStore.userInfo
  if (info) {
    infoForm.realName = info.realName || ''
    infoForm.phone = info.phone || ''
    infoForm.email = info.email || ''

    if (userStore.isPatient) {
      patientForm.name = info.name || info.realName || ''
      patientForm.idCard = info.idCard || ''
      patientForm.gender = info.gender
      patientForm.birthDate = info.birthDate || ''
      patientForm.phone = info.phone || ''
      patientForm.emergencyPhone = info.emergencyPhone || ''
      patientForm.address = info.address || ''
      patientForm.bloodType = info.bloodType || ''
      patientForm.allergyHistory = info.allergyHistory || ''
      patientForm.geneticDiseases = info.geneticDiseases || ''
      patientForm.medicalHistory = info.medicalHistory || ''
    }
  }
})

async function handleUpdate() {
  const valid = await infoFormRef.value?.validate().catch(() => false)
  if (!valid) return

  // 患者档案不需要强制验证，但提交时如果填了身份证号就校验格式
  if (userStore.isPatient && patientForm.idCard) {
    const pValid = await patientFormRef.value?.validateField('idCard').catch(() => false)
    if (!pValid) return
  }

  saveLoading.value = true
  try {
    const payload: Record<string, unknown> = {
      realName: infoForm.realName,
      phone: infoForm.phone,
      email: infoForm.email
    }
    if (userStore.isPatient) {
      Object.assign(payload, {
        name: patientForm.name || undefined,
        idCard: patientForm.idCard || undefined,
        gender: patientForm.gender,
        birthDate: patientForm.birthDate || undefined,
        emergencyPhone: patientForm.emergencyPhone || undefined,
        address: patientForm.address || undefined,
        bloodType: patientForm.bloodType || undefined,
        allergyHistory: patientForm.allergyHistory || undefined,
        geneticDiseases: patientForm.geneticDiseases || undefined,
        medicalHistory: patientForm.medicalHistory || undefined
      })
    }
    await updateUserApi(payload)
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
  max-width: 1100px;
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
</style>
