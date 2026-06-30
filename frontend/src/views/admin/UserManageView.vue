<template>
  <div class="user-manage-page">
    <div class="page-title">用户管理</div>

    <div class="search-bar">
      <el-radio-group v-model="activeType" size="small">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="2">患者</el-radio-button>
        <el-radio-button value="0">医生</el-radio-button>
        <el-radio-button value="1">管理员</el-radio-button>
        <el-radio-button value="3">检查医生</el-radio-button>
      </el-radio-group>
      <el-input
        v-model="searchText"
        placeholder="搜索用户名/姓名..."
        :prefix-icon="Search"
        clearable
      />
      <el-button type="primary" round @click="dialogAddVisible = true">
        <el-icon><Plus /></el-icon>新建用户
      </el-button>
    </div>

    <div class="cb-card">
      <div class="cb-card-body" style="padding: 0">
        <el-table
          :data="filteredUsers"
          border
          stripe
          v-loading="loading"
          class="cb-table"
          height="560"
          @row-click="handleRowClick"
        >
          <el-table-column type="index" label="#" width="50" align="center" />
          <el-table-column prop="userName" label="用户名" min-width="120" />
          <el-table-column prop="realName" label="姓名" min-width="120" />
          <el-table-column label="角色" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.role" size="small" effect="plain">{{ row.role }}</el-tag>
              <el-tag v-else type="info" size="small" effect="plain">未分配</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-switch
                :model-value="row.status === 1"
                :loading="row._statusLoading"
                @change="(val: boolean) => handleStatusToggle(row, val)"
              />
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="180">
            <template #default="{ row }">
              {{ row.createTime ? formatTime(row.createTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center" fixed="right">
            <template #default="{ row }">
              <el-button text type="primary" size="small" @click.stop="handleRowClick(row)">详情</el-button>
              <el-popconfirm
                title="确定删除该用户？"
                confirm-button-text="删除"
                confirm-button-type="danger"
                @confirm="handleDelete(row)"
              >
                <template #reference>
                  <el-button text type="danger" size="small" :loading="row._deleteLoading" @click.stop>
                    删除
                  </el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 新建用户对话框 -->
    <el-dialog v-model="dialogAddVisible" title="新建用户" width="480px">
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="80px" size="large">
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="addForm.userName" placeholder="请输入用户名" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="addForm.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="addForm.confirmPassword" type="password" placeholder="请确认密码" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="addForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户类型" prop="userType">
              <el-select v-model="addForm.userType" placeholder="请选择" style="width: 100%">
                <el-option :value="2" label="患者" />
                <el-option :value="0" label="医生" />
                <el-option :value="1" label="管理员" />
                <el-option :value="3" label="检查医生" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogAddVisible = false">取消</el-button>
        <el-button type="primary" :loading="addLoading" @click="handleAddUser">确定</el-button>
      </template>
    </el-dialog>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="dialogVisible" :title="detailUser?.realName || detailUser?.userName" width="500px">
      <el-descriptions :column="2" border v-if="detailUser">
        <el-descriptions-item label="用户名" span="2">{{ detailUser.userName }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ detailUser.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag v-if="detailUser.role" size="small">{{ detailUser.role }}</el-tag>
          <el-tag v-else type="info" size="small">未分配</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="手机号" span="2">{{ detailUser.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱" span="2">{{ detailUser.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态" span="2">
          <el-tag v-if="detailUser.status === 1" type="success" size="small">正常</el-tag>
          <el-tag v-else type="info" size="small">已禁用</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" span="2">{{ detailUser.createTime ? formatTime(detailUser.createTime) : '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { listAllUsersApi, updateUserStatusApi, deleteUserApi, registerApi } from '@/api/user'
import type { UserInfoVO } from '@/types/user'

interface UserRow extends UserInfoVO {
  _statusLoading?: boolean
  _deleteLoading?: boolean
}

// ---- 用户列表 ----
const allUsers = ref<UserRow[]>([])
const activeType = ref<string>('all')
const searchText = ref('')
const loading = ref(false)

const dialogVisible = ref(false)
const detailUser = ref<UserInfoVO | null>(null)

/** 角色名与筛选值的对应关系 */
const filterRoleMap: Record<string, string> = {
  '0': '医生',
  '1': '超级管理员',
  '2': '患者',
  '3': '检查医生',
}

const filteredUsers = computed(() => {
  let list = allUsers.value

  // 基于角色名过滤，与"角色"列的显示保持一致
  if (activeType.value !== 'all') {
    const targetRole = filterRoleMap[activeType.value]
    list = list.filter(u => u.role === targetRole)
  }

  if (searchText.value) {
    const kw = searchText.value.toLowerCase()
    list = list.filter(u =>
      u.userName.toLowerCase().includes(kw) ||
      (u.realName && u.realName.toLowerCase().includes(kw))
    )
  }

  return list
})

function formatTime(t: string) {
  return t.replace('T', ' ').substring(0, 19)
}

onMounted(() => loadUsers())

async function loadUsers() {
  loading.value = true
  try {
    const res = await listAllUsersApi()
    allUsers.value = ((res.data as UserInfoVO[]) || []).map(u => ({ ...u, _statusLoading: false, _deleteLoading: false }))
  } catch {
    allUsers.value = []
  } finally {
    loading.value = false
  }
}

async function handleStatusToggle(row: UserRow, newStatus: boolean) {
  const targetStatus = newStatus ? 1 : 0
  const actionText = targetStatus === 1 ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(
      `确定${actionText}用户「${row.realName || row.userName}」？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }

  row._statusLoading = true
  try {
    await updateUserStatusApi({ userId: row.userId, status: targetStatus })
    ElMessage.success(`${actionText}成功`)
    row.status = targetStatus
  } catch {
    // 已处理
  } finally {
    row._statusLoading = false
  }
}

async function handleDelete(row: UserRow) {
  row._deleteLoading = true
  try {
    await deleteUserApi(row.userId)
    ElMessage.success('删除成功')
    allUsers.value = allUsers.value.filter(u => u.userId !== row.userId)
  } catch {
    // 已处理
  } finally {
    row._deleteLoading = false
  }
}

function handleRowClick(row: UserInfoVO) {
  detailUser.value = row
  dialogVisible.value = true
}

// ---- 新建用户 ----
const addFormRef = ref<FormInstance>()
const dialogAddVisible = ref(false)
const addLoading = ref(false)

const addForm = ref({
  userName: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  userType: 2
})

const validatePass = (_rule: any, value: string, callback: any) => {
  if (value !== addForm.value.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const addRules: FormRules = {
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
  ],
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }]
}

async function handleAddUser() {
  const valid = await addFormRef.value?.validate().catch(() => false)
  if (!valid) return

  addLoading.value = true
  try {
    await registerApi({
      userName: addForm.value.userName,
      password: addForm.value.password,
      realName: addForm.value.realName,
      phone: addForm.value.phone,
      userType: addForm.value.userType
    })
    ElMessage.success('新建用户成功')
    dialogAddVisible.value = false
    addFormRef.value?.resetFields()
    loadUsers()
  } catch {
    // 已处理
  } finally {
    addLoading.value = false
  }
}
</script>

<style scoped>
.user-manage-page {
  max-width: 1200px;
}
</style>
