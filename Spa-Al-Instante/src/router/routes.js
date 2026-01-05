const routes = [
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    children: [{ path: '', component: () => import('pages/IndexPage.vue') }],
  },
  {
    path: '/carrito',
    component: () => import('layouts/MainLayout.vue'),
    children: [{ path: '/carrito', component: () => import('pages/CarritoPage.vue') }],
  },
  {
    path: '/admin/citas',
    component: () => import('layouts/MainLayout.vue'),
    children: [{ path: '/admin/citas', component: () => import('pages/admin/AdminCitas.vue') }],
  },
  {
    path: '/admin/dashboard',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      { path: '/admin/dashboard', component: () => import('pages/admin/AdminDashboard.vue') },
    ],
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue'),
  },
]

export default routes
