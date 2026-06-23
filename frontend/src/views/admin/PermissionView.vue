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
                  v-if="isSuperAdmin"
                  title="超级管理员拥有全部权限，不可修改"
                  type="warning"
                  :closable="false"
                  show-icon
                />
                <el-alert
                  v-else
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
                :props="{ label: 'permissionName', children: 'children', disabled: 'disabled' }"
                class="permission-tree"
              />
              <el-divider />
              <div class="permission-actions">
                <el-button type="primary" :loading="saveLoading" :disabled="isSuperAdmin" @click="handleSave" round>
                  保存权限
                </el-button>
                <el-button @click="handleRefresh" :disabled="!currentRole || isSuperAdmin">
                  重置
                </el-button>
                <el-button v-if="!isSuperAdmin" text @click="handleCheckAll" v-show="!allChecked">全选</el-button>
                <el-button v-if="!isSuperAdmin" text @click="handleUncheckAll" v-show="allChecked">取消全选</el-button>
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { ElTree } from 'element-plus'
import { UserFilled, Key } from '@element-plus/icons-vue'
import { getPermissionsApi, updatePermissionApi, listAllRolesApi, getPermissionTreeApi } from '@/api/user'
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

const isSuperAdmin = computed(() => currentRole.value?.roleCode === 'admin')

function activeRowClass({ row }: { row: RoleItem }) {
  return currentRole.value?.roleId === row.roleId ? 'active-row' : ''
}

const allPermissions = ref<Permission[]>([])  // 平铺的原始权限数据
const permLoading = ref(false)

/** 将平铺权限列表转换为树形结构 */
function buildTree(flat: Permission[]): PermissionNode[] {
  const map = new Map<string, PermissionNode>()
  const roots: PermissionNode[] = []

  // 先创建所有节点
  for (const p of flat) {
    map.set(p.permissionId, { ...p, children: [] })
  }

  // 构建父子关系
  for (const p of flat) {
    const node = map.get(p.permissionId)!
    if (p.parentId === '0' || !map.has(p.parentId)) {
      roots.push(node)
    } else {
      map.get(p.parentId)?.children.push(node)
    }
  }

  return roots
}

onMounted(async () => {
  await Promise.all([loadPermissions(), loadRoles()])
})

async function loadPermissions() {
  permLoading.value = true
  try {
    const res = await getPermissionTreeApi()
    allPermissions.value = res.data as Permission[]
    permissionTree.value = buildTree(allPermissions.value)
  } catch {
    allPermissions.value = []
    permissionTree.value = []
  } finally {
    permLoading.value = false
  }
}

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

  const treeData = allPermissions.value.length > 0
    ? buildTree(allPermissions.value)
    : []

  // 超级管理员：禁用所有节点，默认全选
  if (role.roleCode === 'admin') {
    permissionTree.value = addDisabledToTree(treeData, true)
    const allIds = getAllNodeIds(treeData)
    setTimeout(() => {
      treeRef.value?.setCheckedKeys(allIds)
    }, 50)
    allChecked.value = true
    return
  }

  // 普通角色：取消禁用，加载已分配的权限
  permissionTree.value = addDisabledToTree(treeData, false)

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

function addDisabledToTree(nodes: PermissionNode[], disabled: boolean): PermissionNode[] {
  return nodes.map(node => ({
    ...node,
    disabled,
    children: node.children ? addDisabledToTree(node.children, disabled) : []
  }))
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
