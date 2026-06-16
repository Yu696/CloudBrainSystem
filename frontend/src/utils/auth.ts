/** 从 localStorage 获取 Token */
export function getToken(): string | null {
  return localStorage.getItem('token')
}

/** 检查是否已登录 */
export function isLoggedIn(): boolean {
  return !!getToken()
}
