<template>
  <div class="login-wrapper">
    <div class="login-bg">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>
    <div class="login-card">
      <div class="login-header">
        <div class="login-logo">
          <svg viewBox="0 0 24 24" width="40" height="40" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2L2 7l10 5 10-5-10-5z" stroke-linejoin="round"/>
            <path d="M2 17l10 5 10-5" stroke-linejoin="round"/>
            <path d="M2 12l10 5 10-5" stroke-linejoin="round"/>
          </svg>
        </div>
        <h2 class="login-title">云脑诊疗平台</h2>
        <p class="login-subtitle">CloudBrain Medical System</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large" class="login-form">
        <el-form-item prop="userName">
          <el-input
            v-model="form.userName"
            placeholder="用户名"
            :prefix-icon="User"
            class="login-input"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            show-password
            class="login-input"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin" round>
            <span v-if="!loading">登 录</span>
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/register" class="register-link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  userName: '',
  password: '',
})

const rules: FormRules = {
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(form.userName, form.password)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0b1a2e 0%, #1a3a5c 50%, #0b1a2e 100%);
}

/* 背景装饰 */
.login-bg {
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
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, #4a9fd9, #1a7fbf);
  top: -200px;
  right: -150px;
  animation: float 12s ease-in-out infinite;
}
.shape-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #52c41a, #1a7fbf);
  bottom: -100px;
  left: -100px;
  animation: float 15s ease-in-out infinite reverse;
}
.shape-3 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #1890ff, #4a9fd9);
  top: 30%;
  left: 15%;
  animation: float 10s ease-in-out infinite 2s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.05); }
  66% { transform: translate(-20px, 20px) scale(0.95); }
}

.login-card {
  width: 420px;
  padding: 48px 40px 36px;
  background: rgba(255, 255, 255, 0.97);
  border-radius: var(--cb-radius-xl);
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
  backdrop-filter: blur(20px);
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.login-logo {
  width: 72px;
  height: 72px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, var(--cb-primary), var(--cb-primary-light));
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 8px 24px rgba(26, 127, 191, 0.3);
}

.login-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--cb-text-primary);
  margin: 0 0 6px;
  letter-spacing: 2px;
}

.login-subtitle {
  font-size: var(--cb-font-xs);
  color: var(--cb-text-placeholder);
  margin: 0;
  letter-spacing: 4px;
  text-transform: uppercase;
}

.login-form {
  margin-bottom: 8px;
}

.login-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 4px 16px;
  box-shadow: 0 0 0 1px var(--cb-border) inset;
  transition: box-shadow 0.25s;
}
.login-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--cb-primary-light) inset;
}
.login-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--cb-primary) inset;
}
.login-input :deep(.el-input__inner) {
  height: 44px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
  margin-top: 4px;
  background: linear-gradient(135deg, var(--cb-primary), var(--cb-primary-dark));
  border: none;
  transition: all 0.3s;
}
.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(26, 127, 191, 0.4);
}
.login-btn:active {
  transform: translateY(0);
}

.login-footer {
  text-align: center;
  font-size: var(--cb-font-base);
  color: var(--cb-text-secondary);
}

.register-link {
  color: var(--cb-primary);
  font-weight: 500;
  margin-left: 4px;
  text-decoration: none;
  transition: color 0.2s;
}
.register-link:hover {
  color: var(--cb-primary-dark);
  text-decoration: underline;
}
</style>
