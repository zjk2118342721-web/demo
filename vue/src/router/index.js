import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../layout/AdminLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('../views/DashboardView.vue'),
        meta: { title: '数据概览' }
      },
      {
        path: 'categories',
        name: 'categories',
        component: () => import('../views/CategoryView.vue'),
        meta: { title: '菜品分类' }
      },
      {
        path: 'dishes',
        name: 'dishes',
        component: () => import('../views/DishView.vue'),
        meta: { title: '菜品管理' }
      },
      {
        path: 'orders',
        name: 'orders',
        component: () => import('../views/OrderView.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'reviews',
        name: 'reviews',
        component: () => import('../views/ReviewView.vue'),
        meta: { title: '评价管理' }
      },
      {
        path: 'users',
        name: 'users',
        component: () => import('../views/UserView.vue'),
        meta: { title: '顾客管理' }
      },
      {
        path: 'tables',
        name: 'tables',
        component: () => import('../views/TableView.vue'),
        meta: { title: '餐桌管理' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('wsx_admin_token')
  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }
  if (to.path === '/login' && token) {
    next('/dashboard')
    return
  }
  document.title = to.meta.title ? `${to.meta.title} · 味食香管理后台` : '味食香管理后台'
  next()
})

export default router
