import request from '@/utils/request'

/** 获取系统用户列表 */
export function listSystemUsersApi() {
  return request.get<any[]>('/system/user/list')
}

/** 获取系统用户详情 */
export function getSystemUserDetailApi(userId: string) {
  return request.get<any>('/system/user/detail', { params: { userId } })
}

/** 新增系统用户 */
export function addSystemUserApi(data: { username: string; password: string; roleId: string }) {
  return request.post<string>('/system/user/add', data)
}

/** 更新系统用户 */
export function updateSystemUserApi(data: { userId: string; username: string; roleId: string }) {
  return request.put<string>('/system/user/update', data)
}

/** 启用/禁用系统用户 */
export function toggleSystemUserStatusApi(data: { userId: string; status: number }) {
  return request.put<string>('/system/user/status', data)
}

/** 删除系统用户 */
export function deleteSystemUserApi(userId: string) {
  return request.delete<string>('/system/user/delete', { params: { userId } })
}
