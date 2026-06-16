import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { title: '注册', noAuth: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/ProfileView.vue'),
        meta: { title: '个人信息' }
      },
      {
        path: 'admin/role',
        name: 'RoleManage',
        component: () => import('@/views/admin/RoleManageView.vue'),
        meta: { title: '角色管理', admin: true }
      },
      {
        path: 'admin/permission',
        name: 'Permission',
        component: () => import('@/views/admin/PermissionView.vue'),
        meta: { title: '权限管理', admin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录跳转登录页，管理员页面校验角色
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  document.title = `${to.meta.title} - 云脑诊疗平台`

  // 无需认证的页面（登录、注册）
  if (to.meta.noAuth) {
    if (to.path === '/login' && userStore.isLoggedIn) {
      next('/dashboard')
    } else {
      next()
    }
    return
  }

  // 需要认证的页面
  if (!userStore.isLoggedIn) {
    next('/login')
    return
  }

  // 管理员页面权限校验
  if (to.meta.admin && !userStore.isAdmin) {
    next('/dashboard')
    return
  }

  next()
})

export default router
