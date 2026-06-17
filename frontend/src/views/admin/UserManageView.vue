<template>
  <div class="user-manage-page">
    <div class="page-title">
      <span>系统用户管理</span>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchText"
        placeholder="搜索用户名..."
        :prefix-icon="Search"
        clearable
        @input="handleSearch"
      />
      <el-button type="primary" round @click="router.push('/admin/users/add')">
        <el-icon><Plus /></el-icon>新增用户
      </el-button>
    </div>

    <!-- 用户表格 -->
    <div class="cb-card">
      <div class="cb-card-body" style="padding: 0">
        <el-table
          :data="filteredUsers"
          border
          stripe
          v-loading="loading"
          class="cb-table"
          height="520"
        >
          <el-table-column type="index" label="#" width="50" align="center" />
          <el-table-column prop="username" label="用户名" min-width="120" />
          <el-table-column prop="realName" label="姓名" min-width="120" />
          <el-table-column label="角色" width="140">
            <template #default="{ row }">
              <el-tag effect="plain" size="small">{{ row.roleName || '未分配' }}</el-tag>
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
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <el-popconfirm
                title="确定删除该用户？"
                confirm-button-text="删除"
                confirm-button-type="danger"
                @confirm="handleDelete(row)"
              >
                <template #reference>
                  <el-button text type="danger" size="small" :loading="row._deleteLoading">
                    删除
                  </el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { listSystemUsersApi, toggleSystemUserStatusApi, deleteSystemUserApi } from '@/api/system'
import type { SystemUserVO } from '@/types/system'

interface UserRow extends SystemUserVO {
  _statusLoading?: boolean
  _deleteLoading?: boolean
}

const router = useRouter()

const allUsers = ref<UserRow[]>([])
const searchText = ref('')
const loading = ref(false)

const filteredUsers = computed(() => {
  if (!searchText.value) return allUsers.value
  const keyword = searchText.value.toLowerCase()
  return allUsers.value.filter(
    u => u.username.toLowerCase().includes(keyword) ||
         (u.realName && u.realName.toLowerCase().includes(keyword))
  )
})

onMounted(() => {
  loadUsers()
})

async function loadUsers() {
  loading.value = true
  try {
    const res = await listSystemUsersApi()
    allUsers.value = (res.data as any[])?.map(u => ({ ...u, _statusLoading: false, _deleteLoading: false })) || []
  } catch {
    allUsers.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  // computed handles filtering
}

async function handleStatusToggle(row: UserRow, newStatus: boolean) {
  const targetStatus = newStatus ? 1 : 0
  const actionText = targetStatus === 1 ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(
      `确定${actionText}用户「${row.username}」？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }

  row._statusLoading = true
  try {
    await toggleSystemUserStatusApi({ userId: row.userId, status: targetStatus })
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
    await deleteSystemUserApi(row.userId)
    ElMessage.success('删除成功')
    allUsers.value = allUsers.value.filter(u => u.userId !== row.userId)
  } catch {
    // 已处理
  } finally {
    row._deleteLoading = false
  }
}
</script>

<style scoped>
.user-manage-page {
  max-width: 1200px;
}
</style>
