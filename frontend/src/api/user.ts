import request from '@/utils/request'
import type { LoginResponse, UserInfoVO, Role, Permission, UserRoleVO } from '@/types/user'

/** 登录 */
export function loginApi(data: { userName: string; password: string; userType?: number }) {
  return request.post<LoginResponse>('/user/login', data)
}

/** 注册 */
export function registerApi(data: {
  userName: string
  password: string
  realName: string
  phone: string
  email?: string
  userType?: number
}) {
  return request.post<{ userId: string }>('/user/register', data)
}

/** 获取当前用户信息 */
export function getUserInfoApi() {
  return request.get<UserInfoVO>('/user/info')
}

/** 更新个人信息（含患者档案字段） */
export function updateUserApi(data: {
  realName?: string
  phone?: string
  email?: string
  name?: string
  idCard?: string
  gender?: number
  birthDate?: string
  emergencyPhone?: string
  address?: string
  bloodType?: string
  allergyHistory?: string
  geneticDiseases?: string
  medicalHistory?: string
}) {
  return request.put<string>('/user/update', data)
}

/** 重置密码 */
export function resetPasswordApi(data: { oldPassword: string; newPassword: string }) {
  return request.put<string>('/user/reset-password', data)
}

/** 获取所有角色列表 */
export function listAllRolesApi() {
  return request.get<Role[]>('/role/list')
}

/** 分配角色 */
export function assignRoleApi(data: {
  userId: string
  roleId: string
  departmentId?: string
  title?: string
  consultationFee?: number
  specialty?: string
  introduction?: string
}) {
  return request.post<string>('/role/assign', data)
}

/** 查询角色权限 */
export function getPermissionsApi(roleId?: string) {
  return request.get<Permission[]>('/role/permissions', { params: { roleId } })
}

/** 查询用户角色（若为医生则附带科室和职位） */
export function getUserRoleApi(userId: string) {
  return request.get<UserRoleVO>('/role/user-role', { params: { userId } })
}

/** 获取所有用户列表（管理员），可按用户类型筛选 */
export function listAllUsersApi(userType?: number) {
  return request.get<UserInfoVO[]>('/user/list-all', { params: { userType } })
}

/** 获取完整权限树列表 */
export function getPermissionTreeApi() {
  return request.get<Permission[]>('/role/permission-tree')
}

/** 启用/禁用用户（管理员） */
export function updateUserStatusApi(data: { userId: string; status: number }) {
  return request.put<string>('/user/status', data)
}

/** 删除用户（管理员） */
export function deleteUserApi(userId: string) {
  return request.delete<string>('/user/delete', { params: { userId } })
}

/** 更新角色权限 */
export function updatePermissionApi(data: { roleId: string; permissionIds: string[] }) {
  return request.put<string>('/role/update-permission', data)
}
