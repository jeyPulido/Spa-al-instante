import { defineStore } from 'pinia'
import { jwtDecode } from 'jwt-decode'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token'),
    user: null,
    ready: false, // 👈 IMPORTANTE
  }),

  actions: {
    init() {
      if (this.token) {
        try {
          this.setUserFromToken(this.token)
        } catch {
          // token corrupto o expirado
          this.logout()
        }
      }

      // ✅ SIEMPRE marcar como listo
      this.ready = true
    },

    login(token) {
      this.token = token
      localStorage.setItem('token', token)
      this.setUserFromToken(token)
      this.ready = true
    },

    setUserFromToken(token) {
      const decoded = jwtDecode(token)

      this.user = {
        id: decoded.id,
        nombre: decoded.nombre,
        apellidos: decoded.apellidos,
        correo: decoded.sub,
        telefono: decoded.telefono,
        rol: decoded.rol,
      }
    },

    logout() {
      localStorage.removeItem('token')
      this.token = null
      this.user = null
      this.ready = true
    },
  },
})
