<template>
  <div class="permission-page">
    <el-row :gutter="20">
      <!-- 左侧：角色列表 -->
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">角色列表</span>
          </template>
          <el-table :data="roles" border stripe v-loading="roleLoading" @row-click="handleRoleClick"
            :row-class-name="rowClassName">
            <el-table-column prop="roleName" label="角色名称" />
            <el-table-column prop="roleCode" label="编码" width="100" />
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：权限配置 -->
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">权限配置</span>
          </template>
          <div v-if="currentRole">
            <p style="margin-bottom: 16px; color: #606266;">
              为角色 <strong>{{ currentRole.roleName }}</strong> 配置权限
            </p>
            <el-tree
              ref="treeRef"
              :data="permissionTree"
              show-checkbox
              node-key="permissionId"
              default-expand-all
              :props="{ label: 'permissionName', children: 'children' }"
            />
            <div style="margin-top: 20px">
              <el-button type="primary" :loading="saveLoading" @click="handleSave">
                保存权限
              </el-button>
              <el-button @click="handleRefresh">刷新</el-button>
            </div>
          </div>
          <el-empty v-else description="请从左侧选择角色" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { ElTree } from 'element-plus'
import { getPermissionsApi, updatePermissionApi, listAllRolesApi } from '@/api/user'
import type { Permission } from '@/types/user'

interface RoleItem {
  roleId: string
  roleName: string
  roleCode: string
}

interface PermissionNode extends Permission {
  children: PermissionNode[]
}

const roles = ref<RoleItem[]>([])

const permissionTree = ref<PermissionNode[]>([])
const roleLoading = ref(false)
const saveLoading = ref(false)
const currentRole = ref<RoleItem | null>(null)
const treeRef = ref<InstanceType<typeof ElTree>>()
const currentRoleId = ref('')

function rowClassName({ row }: { row: RoleItem }) {
  return currentRole.value?.roleId === row.roleId ? 'active-row' : ''
}

// 权限树结构
const mockPermissions: PermissionNode[] = [
  {
    permissionId: '1', permissionName: '用户管理', permissionCode: 'user:manage',
    parentId: '0', type: 'menu', path: '', sortOrder: 1,
    children: [
      { permissionId: '11', permissionName: '用户列表', permissionCode: 'user:list', parentId: '1', type: 'button', path: '', sortOrder: 1, children: [] },
      { permissionId: '12', permissionName: '分配角色', permissionCode: 'user:assign', parentId: '1', type: 'button', path: '', sortOrder: 2, children: [] },
      { permissionId: '13', permissionName: '权限配置', permissionCode: 'user:permission', parentId: '1', type: 'button', path: '', sortOrder: 3, children: [] }
    ]
  },
  {
    permissionId: '2', permissionName: '预约管理', permissionCode: 'appointment:manage',
    parentId: '0', type: 'menu', path: '', sortOrder: 2,
    children: [
      { permissionId: '21', permissionName: '预约挂号', permissionCode: 'appointment:book', parentId: '2', type: 'button', path: '', sortOrder: 1, children: [] },
      { permissionId: '22', permissionName: '排班管理', permissionCode: 'schedule:manage', parentId: '2', type: 'button', path: '', sortOrder: 2, children: [] },
      { permissionId: '23', permissionName: '挂号记录', permissionCode: 'appointment:list', parentId: '2', type: 'button', path: '', sortOrder: 3, children: [] },
      { permissionId: '24', permissionName: '取消预约', permissionCode: 'appointment:cancel', parentId: '2', type: 'button', path: '', sortOrder: 4, children: [] }
    ]
  },
  {
    permissionId: '3', permissionName: '诊疗管理', permissionCode: 'medical:manage',
    parentId: '0', type: 'menu', path: '', sortOrder: 3,
    children: [
      { permissionId: '31', permissionName: '病历管理', permissionCode: 'medical:record', parentId: '3', type: 'button', path: '', sortOrder: 1, children: [] },
      { permissionId: '32', permissionName: '处方管理', permissionCode: 'medical:prescription', parentId: '3', type: 'button', path: '', sortOrder: 2, children: [] },
      { permissionId: '33', permissionName: '检查管理', permissionCode: 'medical:exam', parentId: '3', type: 'button', path: '', sortOrder: 3, children: [] }
    ]
  },
  {
    permissionId: '4', permissionName: '患者管理', permissionCode: 'patient:manage',
    parentId: '0', type: 'menu', path: '', sortOrder: 4,
    children: [
      { permissionId: '41', permissionName: '新建档案', permissionCode: 'patient:create', parentId: '4', type: 'button', path: '', sortOrder: 1, children: [] },
      { permissionId: '42', permissionName: '患者列表', permissionCode: 'patient:list', parentId: '4', type: 'button', path: '', sortOrder: 2, children: [] },
      { permissionId: '43', permissionName: '编辑档案', permissionCode: 'patient:edit', parentId: '4', type: 'button', path: '', sortOrder: 3, children: [] }
    ]
  },
  {
    permissionId: '5', permissionName: '系统管理', permissionCode: 'system:manage',
    parentId: '0', type: 'menu', path: '', sortOrder: 5,
    children: [
      { permissionId: '51', permissionName: '系统用户', permissionCode: 'system:user', parentId: '5', type: 'button', path: '', sortOrder: 1, children: [] },
      { permissionId: '52', permissionName: '系统配置', permissionCode: 'system:config', parentId: '5', type: 'button', path: '', sortOrder: 2, children: [] }
    ]
  }
]

onMounted(async () => {
  permissionTree.value = mockPermissions
  await loadRoles()
})

async function loadRoles() {
  roleLoading.value = true
  try {
    const res = await listAllRolesApi()
    roles.value = res.data as any[]
    if (roles.value.length > 0) {
      handleRoleClick(roles.value[0])
    }
  } catch {
    roles.value = []
  } finally {
    roleLoading.value = false
  }
}

async function handleRoleClick(role: RoleItem) {
  currentRole.value = role
  currentRoleId.value = role.roleId

  try {
    const res = await getPermissionsApi(role.roleId)
    const perms = res.data as Permission[]
    const checkedIds = perms.map(p => p.permissionId)
    setTimeout(() => {
      treeRef.value?.setCheckedKeys(checkedIds)
    }, 50)
  } catch {
    treeRef.value?.setCheckedKeys([])
  }
}

async function handleSave() {
  if (!currentRole.value) return
  // 只获取叶子节点(按钮级权限)，父节点状态由子节点自动推导
  const checkedKeys = treeRef.value?.getCheckedKeys(true) as string[]

  saveLoading.value = true
  try {
    await updatePermissionApi({
      roleId: currentRole.value.roleId,
      permissionIds: checkedKeys
    })
    ElMessage.success('权限更新成功')
  } catch {
    // 已处理
  } finally {
    saveLoading.value = false
  }
}

function handleRefresh() {
  if (currentRole.value) {
    handleRoleClick(currentRole.value)
  }
}
</script>

<style scoped>
.card-title {
  font-size: 16px;
  font-weight: 600;
}

:deep(.active-row) {
  background-color: #ecf5ff !important;
}
</style>
