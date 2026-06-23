import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfoVO } from '@/types/user'
import { loginApi, getUserInfoApi } from '@/api/user'
import type { LoginResponse } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfoVO | null>(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => userInfo.value?.role || '')
  const isAdmin = computed(() => userInfo.value?.userType === 1)

  /** 根据 userType 映射角色名称：0=doctor, 1=admin, 2=patient */
  const roleName = computed(() => {
    const t = userInfo.value?.userType
    if (t === 0) return 'doctor'
    if (t === 1) return 'admin'
    if (t === 2) return 'patient'
    return ''
  })

  /** 判断当前用户是否拥有指定角色 */
  function hasRole(...roles: string[]) {
    return roles.includes(roleName.value)
  }

  /** 登录，保存 token 和用户信息 */
  async function login(userName: string, password: string, userType?: number) {
    const res = await loginApi({ userName, password, userType })
    const data = res.data as LoginResponse
    token.value = data.token
    userInfo.value = data.userInfo
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(data.userInfo))
    return data
  }

  /** 退出登录 */
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  /** 获取最新用户信息 */
  async function fetchUserInfo() {
    const res = await getUserInfoApi()
    userInfo.value = res.data as UserInfoVO
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  return { token, userInfo, isLoggedIn, role, isAdmin, roleName, hasRole, login, logout, fetchUserInfo }
})
