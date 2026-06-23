/** 用户信息 VO */
export interface UserInfoVO {
  userId: string
  userName: string
  realName: string
  phone: string
  email: string
  avatarUrl: string
  userType: number
  role: string | null
  status?: number
  createTime?: string
}

/** 登录响应 */
export interface LoginResponse {
  token: string
  userInfo: UserInfoVO
}

/** 角色 */
export interface Role {
  roleId: string
  roleName: string
  roleCode: string
  description: string
  status: number
}

/** 用户角色 VO（含医生专属字段） */
export interface UserRoleVO {
  roleId: string
  roleName: string
  roleCode: string
  description: string
  status: number
  departmentId?: string
  title?: string
  consultationFee?: number
  specialty?: string
  introduction?: string
}

/** 权限 */
export interface Permission {
  permissionId: string
  permissionName: string
  permissionCode: string
  parentId: string
  type: string
  path: string
  sortOrder: number
}
