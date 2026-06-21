import request from '@/utils/request'
import type { LoginResponse, UserInfoVO, Role, Permission } from '@/types/user'

/** 登录 */
export function loginApi(data: { userName: string; password: string }) {
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

/** 更新个人信息 */
export function updateUserApi(data: { realName?: string; phone?: string; email?: string }) {
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
export function assignRoleApi(data: { userId: string; roleId: string; departmentId?: string; title?: string }) {
  return request.post<string>('/role/assign', data)
}

/** 查询角色权限 */
export function getPermissionsApi(roleId?: string) {
  return request.get<Permission[]>('/role/permissions', { params: { roleId } })
}

/** 查询用户角色 */
export function getUserRoleApi(userId: string) {
  return request.get<Role>('/role/user-role', { params: { userId } })
}

/** 获取所有用户列表（管理员） */
export function listAllUsersApi() {
  return request.get<UserInfoVO[]>('/user/list-all')
}

/** 更新角色权限 */
export function updatePermissionApi(data: { roleId: string; permissionIds: string[] }) {
  return request.put<string>('/role/update-permission', data)
}
