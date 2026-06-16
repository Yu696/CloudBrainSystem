import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppointmentStore = defineStore('appointment', () => {
  const patientId = ref(localStorage.getItem('patientId') || '')

  function setPatientId(id: string) {
    patientId.value = id
    localStorage.setItem('patientId', id)
  }

  return { patientId, setPatientId }
})
