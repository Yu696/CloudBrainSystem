<template>
  <div class="user-add-page">
    <div class="page-title">
      <span>新增系统用户</span>
    </div>

    <div class="cb-card">
      <div class="cb-card-body">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="80px"
          class="form-section"
          size="large"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" maxlength="50" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              show-password
              maxlength="50"
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              show-password
              maxlength="50"
            />
          </el-form-item>
          <el-form-item label="角色" prop="roleId">
            <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%">
              <el-option
                v-for="role in roles"
                :key="role.roleId"
                :label="role.roleName"
                :value="role.roleId"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" round @click="handleSubmit">
              提交
            </el-button>
            <el-button round @click="router.push('/admin/users')">返回</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { addSystemUserApi } from '@/api/system'
import { listAllRolesApi } from '@/api/user'

interface RoleItem {
  roleId: string
  roleName: string
  roleCode: string
}

const router = useRouter()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const roles = ref<RoleItem[]>([])

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  roleId: ''
})

const validateConfirm = (_rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度在 2-50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ],
  roleId: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

onMounted(async () => {
  try {
    const res = await listAllRolesApi()
    roles.value = res.data as any[] || []
  } catch {
    roles.value = []
  }
})

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await addSystemUserApi({
      username: form.username,
      password: form.password,
      roleId: form.roleId
    })
    ElMessage.success('创建成功')
    router.push('/admin/users')
  } catch {
    // 已处理
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.user-add-page {
  max-width: 700px;
}
</style>
