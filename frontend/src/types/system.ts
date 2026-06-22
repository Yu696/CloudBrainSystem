/** 系统用户 VO */
export interface SystemUserVO {
  userId: string
  username: string
  realName: string
  phone: string
  email: string
  userType: number
  adminType: number
  roleId: string
  roleName: string
  status: number
  createTime: string
}
