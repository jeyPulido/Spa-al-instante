import { defineRouter } from '#q-app/wrappers'
import {
  createRouter,
  createMemoryHistory,
  createWebHistory,
  createWebHashHistory,
} from 'vue-router'
import routes from './routes'
import { useAuthStore } from 'stores/auth'

export default defineRouter(function () {
  const createHistory = process.env.SERVER
    ? createMemoryHistory
    : process.env.VUE_ROUTER_MODE === 'history'
      ? createWebHistory
      : createWebHashHistory

  const Router = createRouter({
    scrollBehavior: () => ({ left: 0, top: 0 }),
    routes,
    history: createHistory(process.env.VUE_ROUTER_BASE),
  })

  // 🔐 GUARD GLOBAL
  Router.beforeEach((to, from, next) => {
    const auth = useAuthStore()

    // 🔹 Ruta pública
    if (to.meta.public) {
      return next()
    }

    // 🔹 Requiere sesión
    if (to.meta.auth && !auth.token) {
      return next('/')
    }

    // 🔹 Requiere rol específico
    if (to.meta.role && auth.user?.rol !== to.meta.role) {
      return next('/')
    }

    next()
  })

  return Router
})
