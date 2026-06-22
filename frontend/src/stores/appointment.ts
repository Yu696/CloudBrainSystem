import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useUserStore } from './user'

export const useAppointmentStore = defineStore('appointment', () => {
  const userStore = useUserStore()

  const manualPatientId = ref(localStorage.getItem('patientId') || '')

  /** 患者本人登录时用 userInfo.patientId，否则用手动设置的 */
  const patientId = computed(() => {
    if (userStore.isPatient && userStore.userInfo?.patientId) {
      return userStore.userInfo.patientId
    }
    return manualPatientId.value
  })

  function setPatientId(id: string) {
    manualPatientId.value = id
    localStorage.setItem('patientId', id)
  }

  return { patientId, setPatientId }
})
