<template>
  <div class="role-page">
    <el-row :gutter="20">
      <!-- 左侧：用户列表 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">用户列表</span>
          </template>
          <el-table :data="users" border stripe v-loading="userLoading" @row-click="handleUserClick">
            <el-table-column prop="userName" label="用户名" />
            <el-table-column prop="realName" label="姓名" />
            <el-table-column label="当前角色" width="140">
              <template #default="{ row }">
                <el-tag v-if="row.role" size="small">{{ row.role }}</el-tag>
                <el-tag v-else type="info" size="small">未分配</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：分配角色 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">分配角色</span>
          </template>
          <el-form v-if="selectedUser" label-width="80px">
            <el-form-item label="用户">
              <el-input :model-value="selectedUser.realName || selectedUser.userName" disabled />
            </el-form-item>
            <el-form-item label="角色">
              <el-select v-model="selectedRoleId" placeholder="请选择角色" style="width: 100%">
                <el-option
                  v-for="role in roles"
                  :key="role.roleId"
                  :label="role.roleName"
                  :value="role.roleId"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="assignLoading" @click="handleAssign">
                确认分配
              </el-button>
            </el-form-item>
          </el-form>
          <el-empty v-else description="请从左侧选择用户" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { assignRoleApi, getUserRoleApi, listAllRolesApi } from '@/api/user'
import type { UserInfoVO } from '@/types/user'

interface UserRow extends UserInfoVO {
  role: string | null
}

const users = ref<UserRow[]>([])
const roles = ref<any[]>([])
const userLoading = ref(false)
const selectedUser = ref<UserRow | null>(null)
const selectedRoleId = ref('')
const assignLoading = ref(false)

onMounted(async () => {
  await loadRoles()
  await loadUsers()
})

async function loadRoles() {
  try {
    const res = await listAllRolesApi()
    roles.value = res.data as any[]
  } catch {
    roles.value = []
  }
}

async function loadUsers() {
  userLoading.value = true
  try {
    // 从后端API获取用户列表
    const { listAllUsersApi } = await import('@/api/user')
    const res = await listAllUsersApi()
    users.value = (res.data as any[]) || []
  } catch {
    users.value = []
  } finally {
    userLoading.value = false
  }
}

async function handleUserClick(row: UserRow) {
  selectedUser.value = row
  selectedRoleId.value = ''

  try {
    const res = await getUserRoleApi(row.userId)
    const roleData = res.data as any
    selectedRoleId.value = roleData.roleId || ''
  } catch {
    selectedRoleId.value = ''
  }
}

async function handleAssign() {
  if (!selectedUser.value || !selectedRoleId.value) {
    ElMessage.warning('请选择用户和角色')
    return
  }

  assignLoading.value = true
  try {
    await assignRoleApi({
      userId: selectedUser.value.userId,
      roleId: selectedRoleId.value
    })
    ElMessage.success('分配成功')
    const role = roles.value.find(r => r.roleId === selectedRoleId.value)
    if (role) {
      selectedUser.value.role = role.roleName
    }
  } catch {
    // 已处理
  } finally {
    assignLoading.value = false
  }
}
</script>

<style scoped>
.card-title {
  font-size: 16px;
  font-weight: 600;
}
</style>
