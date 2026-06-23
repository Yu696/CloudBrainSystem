<template>
  <div class="register-wrapper">
    <div class="register-bg">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
    </div>
    <div class="register-card">
      <div class="register-header">
        <h2 class="register-title">创建账号</h2>
        <p class="register-subtitle">注册云脑诊疗平台账号</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large" class="register-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item prop="userName">
              <el-input v-model="form.userName" placeholder="用户名" :prefix-icon="User" class="register-input" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="realName">
              <el-input v-model="form.realName" placeholder="真实姓名" class="register-input" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password class="register-input" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" show-password class="register-input" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" maxlength="11" class="register-input">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="register-btn" @click="handleRegister" round>
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login" class="login-link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Tools } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { registerApi } from '@/api/user'

const router = useRouter()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  userName: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  userType: 2   // 仅允许注册患者
})

const validatePass = (_rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await registerApi({
      userName: form.userName,
      password: form.password,
      realName: form.realName,
      phone: form.phone,
      userType: form.userType
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-wrapper {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0b1a2e 0%, #1a3a5c 50%, #0b1a2e 100%);
  padding: 40px 0;
}

.register-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.bg-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
}
.shape-1 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #4a9fd9, #1a7fbf);
  top: -150px;
  right: -100px;
  animation: float 14s ease-in-out infinite;
}
.shape-2 {
  width: 350px;
  height: 350px;
  background: linear-gradient(135deg, #52c41a, #1a7fbf);
  bottom: -80px;
  left: -80px;
  animation: float 12s ease-in-out infinite reverse;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.05); }
  66% { transform: translate(-20px, 20px) scale(0.95); }
}

.register-card {
  width: 520px;
  padding: 40px 36px 32px;
  background: rgba(255, 255, 255, 0.97);
  border-radius: var(--cb-radius-xl);
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
  backdrop-filter: blur(20px);
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--cb-text-primary);
  margin: 0 0 6px;
  letter-spacing: 2px;
}

.register-subtitle {
  font-size: var(--cb-font-sm);
  color: var(--cb-text-secondary);
  margin: 0;
}

.register-form {
  margin-bottom: 4px;
}

.register-input {
  width: 100%;
}
.register-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 4px 16px;
  box-shadow: 0 0 0 1px var(--cb-border) inset;
  transition: box-shadow 0.25s;
}
.register-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--cb-primary-light) inset;
}
.register-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--cb-primary) inset;
}
.register-input :deep(.el-input__inner) {
  height: 44px;
}
.register-input :deep(.el-select__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px var(--cb-border) inset;
  min-height: 44px;
}
.register-input :deep(.el-select__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--cb-primary) inset;
}

.register-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
  margin-top: 4px;
  background: linear-gradient(135deg, var(--cb-primary), var(--cb-primary-dark));
  border: none;
  transition: all 0.3s;
}
.register-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(26, 127, 191, 0.4);
}
.register-btn:active {
  transform: translateY(0);
}

.register-footer {
  text-align: center;
  font-size: var(--cb-font-base);
  color: var(--cb-text-secondary);
}

.login-link {
  color: var(--cb-primary);
  font-weight: 500;
  margin-left: 4px;
  text-decoration: none;
  transition: color 0.2s;
}
.login-link:hover {
  color: var(--cb-primary-dark);
  text-decoration: underline;
}
</style>
