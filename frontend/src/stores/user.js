import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token'))

  const login = async (username, password) => {
    const res = await api.login({ username, password })
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', res.data.token)
    return res
  }

  const logout = () => {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
  }

  const fetchUser = async () => {
    if (!token.value) return
    try {
      const res = await api.getCurrentUser()
      user.value = res.data
    } catch {
      logout()
    }
  }

  const isAdmin = () => user.value?.role === 'ADMIN'

  return { user, token, login, logout, fetchUser, isAdmin }
})
