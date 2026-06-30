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
  const isDoctor = computed(() => userInfo.value?.userType === 0)
  const isPatient = computed(() => userInfo.value?.userType === 2)
  const isRadiologist = computed(() => userInfo.value?.userType === 3)

  /** 登录，保存 token 和用户信息，患者同步 patientId */
  async function login(userName: string, password: string, userType?: number) {
    const res = await loginApi({ userName, password, userType })
    const data = res.data as LoginResponse
    token.value = data.token
    userInfo.value = data.userInfo
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(data.userInfo))
    if (data.userInfo.userType === 2 && data.userInfo.patientId) {
      localStorage.setItem('patientId', data.userInfo.patientId)
    }
    return data
  }

  /** 退出登录 */
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('patientId')
  }

  /** 获取最新用户信息，患者同步 patientId */
  async function fetchUserInfo() {
    const res = await getUserInfoApi()
    userInfo.value = res.data as UserInfoVO
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    if (userInfo.value.userType === 2 && userInfo.value.patientId) {
      localStorage.setItem('patientId', userInfo.value.patientId)
    }
  }

  return { token, userInfo, isLoggedIn, role, isAdmin, isDoctor, isPatient, isRadiologist, login, logout, fetchUserInfo }
})
