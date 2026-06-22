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
  // 患者档案字段（仅 userType=2 时有值）
  patientId?: string
  medicalRecordNo?: string
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
