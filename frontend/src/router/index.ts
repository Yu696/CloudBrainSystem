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
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/AdminLoginView.vue'),
    meta: { title: '管理员登录', noAuth: true }
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
      },
      {
        path: 'admin/users',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManageView.vue'),
        meta: { title: '用户管理', admin: true }
      },
      {
        path: 'admin/users/add',
        name: 'UserAdd',
        component: () => import('@/views/admin/UserAddView.vue'),
        meta: { title: '新增用户', admin: true }
      },
      // M02 患者档案（仅医生和管理员）
      {
        path: 'patient/create',
        name: 'PatientCreate',
        component: () => import('@/views/patient/PatientCreateView.vue'),
        meta: { title: '新建档案', roles: ['doctor', 'admin'] }
      },
      {
        path: 'patient/list',
        name: 'PatientList',
        component: () => import('@/views/patient/PatientListView.vue'),
        meta: { title: '患者列表', roles: ['doctor', 'admin'] }
      },
      {
        path: 'patient/detail/:id',
        name: 'PatientDetail',
        component: () => import('@/views/patient/PatientDetailView.vue'),
        meta: { title: '档案详情', roles: ['doctor', 'admin'] }
      },
      // M06 预约管理（患者和管理员）
      {
        path: 'appointment/dept',
        name: 'DeptSelect',
        component: () => import('@/views/appointment/DeptSelectView.vue'),
        meta: { title: '科室选择', roles: ['patient', 'admin'] }
      },
      {
        path: 'appointment/doctor',
        name: 'DoctorSelect',
        component: () => import('@/views/appointment/DoctorSelectView.vue'),
        meta: { title: '医生选择', roles: ['patient', 'admin'] }
      },
      {
        path: 'appointment/confirm',
        name: 'AppointmentConfirm',
        component: () => import('@/views/appointment/AppointmentConfirmView.vue'),
        meta: { title: '预约确认', roles: ['patient', 'admin'] }
      },
      {
        path: 'appointment/pay/:id',
        name: 'Payment',
        component: () => import('@/views/appointment/PaymentView.vue'),
        meta: { title: '支付', roles: ['patient', 'admin'] }
      },
      {
        path: 'appointment/records',
        name: 'AppointmentList',
        component: () => import('@/views/appointment/AppointmentListView.vue'),
        meta: { title: '挂号记录', roles: ['patient', 'admin'] }
      },
      {
        path: 'admin/schedule',
        name: 'ScheduleManage',
        component: () => import('@/views/appointment/ScheduleManageView.vue'),
        meta: { title: '排班管理', admin: true }
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

  // 角色页面权限校验
  const routeRoles = to.meta.roles as string[] | undefined
  if (routeRoles && routeRoles.length > 0 && !userStore.hasRole(...routeRoles)) {
    next('/dashboard')
    return
  }

  next()
})

export default router
