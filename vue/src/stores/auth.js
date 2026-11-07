import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('wsx_admin_token') || '',
    admin: JSON.parse(localStorage.getItem('wsx_admin_info') || 'null')
  }),
  actions: {
    setLogin(token, admin) {
      this.token = token
      this.admin = admin
      localStorage.setItem('wsx_admin_token', token)
      localStorage.setItem('wsx_admin_info', JSON.stringify(admin))
    },
    logout() {
      this.token = ''
      this.admin = null
      localStorage.removeItem('wsx_admin_token')
      localStorage.removeItem('wsx_admin_info')
    }
  }
})
