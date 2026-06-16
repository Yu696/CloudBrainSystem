<template>
  <div class="permission-page">
    <div class="page-title">权限管理</div>
    <el-row :gutter="24">
      <!-- 左侧：角色列表 -->
      <el-col :span="7">
        <div class="cb-card">
          <div class="cb-card-header">
            <el-icon class="header-icon"><UserFilled /></el-icon>
            <span>角色列表</span>
          </div>
          <div class="cb-card-body" style="padding: 0">
            <el-table
              :data="roles"
              border
              stripe
              v-loading="roleLoading"
              @row-click="handleRoleClick"
              :row-class-name="activeRowClass"
              class="cb-table"
              height="460"
            >
              <el-table-column prop="roleName" label="角色名称" min-width="100" />
              <el-table-column prop="roleCode" label="编码" width="90" />
            </el-table>
          </div>
        </div>
      </el-col>

      <!-- 右侧：权限配置 -->
      <el-col :span="17">
        <div class="cb-card">
          <div class="cb-card-header">
            <el-icon class="header-icon"><Key /></el-icon>
            <span>权限配置 — {{ currentRole?.roleName || '未选择' }}</span>
            <el-tag v-if="currentRole" size="small" effect="plain">{{ currentRole.roleCode }}</el-tag>
          </div>
          <div class="cb-card-body">
            <div v-if="currentRole" class="permission-content">
              <div class="permission-hint">
                <el-alert
                  :title="`为角色「${currentRole.roleName}」配置可访问的权限`"
                  type="info"
                  :closable="false"
                  show-icon
                />
              </div>
              <el-divider />
              <el-tree
                ref="treeRef"
                :data="permissionTree"
                show-checkbox
                node-key="permissionId"
                default-expand-all
                :props="{ label: 'permissionName', children: 'children' }"
                class="permission-tree"
              />
              <el-divider />
              <div class="permission-actions">
                <el-button type="primary" :loading="saveLoading" @click="handleSave" round>
                  保存权限
                </el-button>
                <el-button @click="handleRefresh" :disabled="!currentRole">
                  重置
                </el-button>
                <el-button text @click="handleCheckAll" v-if="!allChecked">全选</el-button>
                <el-button text @click="handleUncheckAll" v-if="allChecked">取消全选</el-button>
              </div>
            </div>
            <el-empty v-else description="请从左侧选择角色" :image-size="80" />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { ElTree } from 'element-plus'
import { UserFilled, Key } from '@element-plus/icons-vue'
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
const allChecked = ref(false)

function activeRowClass({ row }: { row: RoleItem }) {
  return currentRole.value?.roleId === row.roleId ? 'active-row' : ''
}

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
  allChecked.value = false

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

function handleCheckAll() {
  const allIds = getAllNodeIds(permissionTree.value)
  treeRef.value?.setCheckedKeys(allIds)
  allChecked.value = true
}

function handleUncheckAll() {
  treeRef.value?.setCheckedKeys([])
  allChecked.value = false
}

function getAllNodeIds(nodes: PermissionNode[]): string[] {
  const ids: string[] = []
  for (const node of nodes) {
    ids.push(node.permissionId)
    if (node.children?.length) {
      ids.push(...getAllNodeIds(node.children))
    }
  }
  return ids
}
</script>

<style scoped>
.permission-page {
  max-width: 1200px;
}

.header-icon {
  color: var(--cb-primary);
  font-size: 18px;
}

.permission-content {
  padding: 0 4px;
}

.permission-hint {
  margin-bottom: 0;
}

.permission-tree {
  padding: 8px 0;
}

.permission-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 表格选中行高亮 */
:deep(.active-row) {
  background-color: var(--cb-primary-lighter) !important;
}

:deep(.el-tree) {
  background: transparent;
}

:deep(.el-tree-node__content) {
  height: 36px;
  border-radius: 4px;
  transition: background 0.2s;
}

:deep(.el-tree-node__content:hover) {
  background: var(--cb-primary-lighter);
}

:deep(.el-divider) {
  margin: 16px 0;
}
</style>
