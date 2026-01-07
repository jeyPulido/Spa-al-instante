const routes = [
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        component: () => import('pages/IndexPage.vue'),
        meta: { public: true },
      },
      {
        path: 'carrito',
        component: () => import('pages/CarritoPage.vue'),
        meta: { public: true },
      },
      {
        path: 'historial',
        component: () => import('pages/HistorialCitas.vue'),
        meta: { auth: true },
      },
      {
        path: 'perfil',
        component: () => import('pages/PerfilPage.vue'),
        meta: { auth: true },
      },
    ],
  },

  {
    path: '/admin',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      {
        path: 'login',
        component: () => import('pages/admin/LoginPage.vue'),
        meta: { public: true },
      },
      {
        path: 'register',
        component: () => import('pages/admin/RegisterPage.vue'),
        meta: { public: true },
      },
      {
        path: 'dashboard',
        component: () => import('pages/admin/AdminDashboard.vue'),
        meta: { auth: true, role: 'ADMIN' },
      },
      {
        path: 'citas',
        component: () => import('pages/admin/AdminCitas.vue'),
        meta: { auth: true, role: 'ADMIN' },
      },
      {
        path: 'servicios',
        component: () => import('pages/admin/ServiciosPage.vue'),
        meta: { auth: true, role: 'ADMIN' },
      },
    ],
  },

  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue'),
  },
]

export default routes
