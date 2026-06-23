<template>
  <div class="role-page">
    <div class="page-title">角色管理</div>
    <el-row :gutter="24">
      <!-- 左侧：用户列表 -->
      <el-col :span="12">
        <div class="cb-card">
          <div class="cb-card-header">
            <el-icon class="header-icon"><UserFilled /></el-icon>
            <span>用户列表</span>
            <el-tag size="small" type="info">{{ users.length }} 人</el-tag>
          </div>
          <div class="cb-card-body" style="padding: 0">
            <el-table
              :data="users"
              border
              stripe
              v-loading="userLoading"
              @row-click="handleUserClick"
              :row-class-name="userRowClassName"
              class="cb-table"
              height="400"
            >
              <el-table-column prop="userName" label="用户名" min-width="100" />
              <el-table-column prop="realName" label="姓名" min-width="100" />
              <el-table-column label="当前角色" width="130">
                <template #default="{ row }">
                  <el-tag v-if="row.role" size="small" effect="plain">{{ row.role }}</el-tag>
                  <el-tag v-else type="info" size="small" effect="plain">未分配</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-col>

      <!-- 右侧：分配角色 -->
      <el-col :span="12" class="right-col">
        <div class="section-header">
          <el-icon class="header-icon"><Edit /></el-icon>
          <span>分配角色</span>
        </div>
        <div class="cb-card right-card">
          <div class="cb-card-body">
            <div v-if="selectedUser" class="assign-form">
              <div class="assign-target">
                <el-avatar :size="48" icon="UserFilled" class="assign-avatar" />
                <div class="assign-user-info">
                  <span class="assign-username">{{ selectedUser.realName || selectedUser.userName }}</span>
                  <span class="assign-usertag">@{{ selectedUser.userName }}</span>
                </div>
              </div>
              <el-divider />
              <el-form label-width="60px">
                <el-form-item label="角色">
                  <el-select v-model="selectedRoleId" placeholder="请选择角色" style="width: 100%">
                    <el-option
                      v-for="role in roles"
                      :key="role.roleId"
                      :label="role.roleName"
                      :value="role.roleId"
                    >
                      <span>{{ role.roleName }}</span>
                      <span class="role-code">[{{ role.roleCode }}]</span>
                    </el-option>
                  </el-select>
                </el-form-item>
                <el-form-item v-if="isDoctorRole" label="科室">
                  <el-select v-model="selectedDepartmentId" placeholder="请选择科室" clearable style="width: 100%">
                    <el-option
                      v-for="dept in departments"
                      :key="dept.departmentId"
                      :label="dept.name"
                      :value="dept.departmentId"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item v-if="isDoctorRole" label="职位">
                  <el-select v-model="selectedTitle" placeholder="请选择职位" style="width: 100%">
                    <el-option
                      v-for="t in titles"
                      :key="t"
                      :label="t"
                      :value="t"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item v-if="isDoctorRole" label="挂号费">
                  <el-input
                    v-model="selectedConsultationFee"
                    type="number"
                    placeholder="未分配"
                    :min="0"
                    :step="0.01"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item v-if="isDoctorRole" label="专长">
                  <el-input
                    v-model="selectedSpecialty"
                    placeholder="如：心血管疾病、呼吸系统疾病"
                    maxlength="200"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item v-if="isDoctorRole" label="简介">
                  <el-input
                    v-model="selectedIntroduction"
                    type="textarea"
                    :rows="3"
                    placeholder="医生简介"
                    maxlength="500"
                    show-word-limit
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="assignLoading" @click="handleAssign" round>
                    确认分配
                  </el-button>
                  <el-button @click="resetSelection">取消</el-button>
                </el-form-item>
              </el-form>
            </div>
            <el-empty v-else description="请从左侧选择用户" :image-size="80" />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled, Edit } from '@element-plus/icons-vue'
import { assignRoleApi, getUserRoleApi, listAllRolesApi } from '@/api/user'
import { listDepartmentsApi } from '@/api/appointment'
import type { UserInfoVO } from '@/types/user'

interface UserRow extends UserInfoVO {
  role: string | null
}

const users = ref<UserRow[]>([])
const roles = ref<any[]>([])
const departments = ref<{ departmentId: string; name: string }[]>([])
const userLoading = ref(false)
const selectedUser = ref<UserRow | null>(null)
const selectedRoleId = ref('')
const selectedDepartmentId = ref('')
const selectedTitle = ref('')
const selectedConsultationFee = ref<number | undefined>(undefined)
const selectedSpecialty = ref('')
const selectedIntroduction = ref('')
const assignLoading = ref(false)

const titles = ['主任医师', '副主任医师', '主治医师', '住院医师', '主任药师', '副主任药师']

const isDoctorRole = computed(() => {
  const role = roles.value.find(r => r.roleId === selectedRoleId.value)
  return role?.roleCode === 'doctor'
})

onMounted(async () => {
  await Promise.all([loadRoles(), loadUsers(), loadDepartments()])
})

async function loadDepartments() {
  try {
    const res = await listDepartmentsApi()
    departments.value = (res.data as any[]) || []
  } catch {
    departments.value = []
  }
}

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
    const { listAllUsersApi } = await import('@/api/user')
    const res = await listAllUsersApi()
    users.value = (res.data as any[]) || []
  } catch {
    users.value = []
  } finally {
    userLoading.value = false
  }
}

function userRowClassName({ row }: { row: UserRow }) {
  return selectedUser.value?.userId === row.userId ? 'active-row' : ''
}

async function handleUserClick(row: UserRow) {
  selectedUser.value = row
  selectedRoleId.value = ''
  selectedDepartmentId.value = ''
  selectedTitle.value = ''
  selectedConsultationFee.value = undefined
  selectedSpecialty.value = ''
  selectedIntroduction.value = ''

  try {
    const res = await getUserRoleApi(row.userId)
    const roleData = res.data
    selectedRoleId.value = roleData.roleId || ''
    selectedDepartmentId.value = roleData.departmentId || ''
    selectedTitle.value = roleData.title || ''
    selectedConsultationFee.value = roleData.consultationFee ?? undefined
    selectedSpecialty.value = roleData.specialty || ''
    selectedIntroduction.value = roleData.introduction || ''
  } catch {
    selectedRoleId.value = ''
  }
}

function resetSelection() {
  selectedUser.value = null
  selectedRoleId.value = ''
  selectedDepartmentId.value = ''
  selectedTitle.value = ''
  selectedConsultationFee.value = undefined
  selectedSpecialty.value = ''
  selectedIntroduction.value = ''
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
      roleId: selectedRoleId.value,
      departmentId: isDoctorRole.value && selectedDepartmentId.value ? selectedDepartmentId.value : undefined,
      title: isDoctorRole.value && selectedTitle.value ? selectedTitle.value : undefined,
      consultationFee: isDoctorRole.value ? selectedConsultationFee.value : undefined,
      specialty: isDoctorRole.value && selectedSpecialty.value ? selectedSpecialty.value : undefined,
      introduction: isDoctorRole.value && selectedIntroduction.value ? selectedIntroduction.value : undefined
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
.role-page {
  max-width: 1100px;
}

.header-icon {
  color: var(--cb-primary);
  font-size: 18px;
}

.right-col {
  display: flex;
  flex-direction: column;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--cb-font-lg);
  font-weight: 600;
  color: var(--cb-text-primary);
  margin-bottom: 16px;
  padding-left: 2px;
}

.right-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.right-card .cb-card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

/* 分配角色表单 */
.assign-form {
  padding: 0 4px;
}

.assign-target {
  display: flex;
  align-items: center;
  gap: 16px;
}

.assign-avatar {
  background: linear-gradient(135deg, var(--cb-primary), var(--cb-primary-light)) !important;
  flex-shrink: 0;
}

.assign-user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.assign-username {
  font-size: var(--cb-font-lg);
  font-weight: 600;
  color: var(--cb-text-primary);
}

.assign-usertag {
  font-size: var(--cb-font-xs);
  color: var(--cb-text-placeholder);
}

.role-code {
  color: var(--cb-text-placeholder);
  font-size: var(--cb-font-xs);
  margin-left: 6px;
}

/* 表格选中行高亮 */
:deep(.active-row) {
  background-color: var(--cb-primary-lighter) !important;
}

:deep(.el-divider) {
  margin: 20px 0;
}
</style>
