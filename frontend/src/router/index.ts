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
      // 管理员 - 科室/医生/预约/病历管理
      {
        path: 'admin/departments',
        name: 'DepartmentManage',
        component: () => import('@/views/admin/DepartmentManageView.vue'),
        meta: { title: '科室管理', admin: true }
      },
      {
        path: 'admin/doctors',
        name: 'DoctorManage',
        component: () => import('@/views/admin/DoctorManageView.vue'),
        meta: { title: '医生管理', admin: true }
      },
      {
        path: 'admin/appointments',
        name: 'AdminAppointments',
        component: () => import('@/views/admin/AdminAppointmentView.vue'),
        meta: { title: '预约管理', admin: true }
      },
      {
        path: 'admin/medical-records',
        name: 'AdminMedicalRecords',
        component: () => import('@/views/admin/AdminMedicalRecordView.vue'),
        meta: { title: '病历管理', admin: true }
      },
      // M02 患者档案
      {
        path: 'patient/create',
        name: 'PatientCreate',
        component: () => import('@/views/patient/PatientCreateView.vue'),
        meta: { title: '新建档案' }
      },
      {
        path: 'patient/list',
        name: 'PatientList',
        component: () => import('@/views/patient/PatientListView.vue'),
        meta: { title: '患者列表' }
      },
      {
        path: 'patient/detail/:id',
        name: 'PatientDetail',
        component: () => import('@/views/patient/PatientDetailView.vue'),
        meta: { title: '档案详情' }
      },
      // M06 预约管理
      {
        path: 'appointment/dept',
        name: 'DeptSelect',
        component: () => import('@/views/appointment/DeptSelectView.vue'),
        meta: { title: '科室选择' }
      },
      {
        path: 'appointment/doctor',
        name: 'DoctorSelect',
        component: () => import('@/views/appointment/DoctorSelectView.vue'),
        meta: { title: '医生选择' }
      },
      {
        path: 'appointment/confirm',
        name: 'AppointmentConfirm',
        component: () => import('@/views/appointment/AppointmentConfirmView.vue'),
        meta: { title: '预约确认' }
      },
      {
        path: 'appointment/pay/:id',
        name: 'Payment',
        component: () => import('@/views/appointment/PaymentView.vue'),
        meta: { title: '支付' }
      },
      {
        path: 'appointment/records',
        name: 'AppointmentList',
        component: () => import('@/views/appointment/AppointmentListView.vue'),
        meta: { title: '挂号记录' }
      },
      {
        path: 'admin/schedule',
        name: 'ScheduleManage',
        component: () => import('@/views/appointment/ScheduleManageView.vue'),
        meta: { title: '排班管理', admin: true }
      },
      // M03 诊疗记录 - 医生端
      {
        path: 'doctor/waiting',
        name: 'DoctorWaiting',
        component: () => import('@/views/doctor/WaitingListView.vue'),
        meta: { title: '待诊列表' }
      },
      {
        path: 'doctor/history',
        name: 'DoctorHistory',
        component: () => import('@/views/doctor/HistoryListView.vue'),
        meta: { title: '已诊列表' }
      },
      {
        path: 'doctor/record-detail/:recordId',
        name: 'RecordDetail',
        component: () => import('@/views/doctor/RecordDetailView.vue'),
        meta: { title: '病历详情' }
      },
      {
        path: 'doctor/record/:appointmentId',
        name: 'MedicalRecord',
        component: () => import('@/views/doctor/MedicalRecordView.vue'),
        meta: { title: '病历编辑' }
      },
      {
        path: 'doctor/prescription/:recordId',
        name: 'Prescription',
        component: () => import('@/views/doctor/PrescriptionView.vue'),
        meta: { title: '处方开具' }
      },
      {
        path: 'doctor/prescription-edit/:recordId/:prescriptionId',
        name: 'PrescriptionEdit',
        component: () => import('@/views/doctor/PrescriptionView.vue'),
        meta: { title: '修改处方' }
      },
      {
        path: 'doctor/exam/:recordId',
        name: 'ExamOrder',
        component: () => import('@/views/doctor/ExamOrderView.vue'),
        meta: { title: '检查单开单' }
      },
      {
        path: 'doctor/exam-edit/:recordId/:orderId',
        name: 'ExamOrderEdit',
        component: () => import('@/views/doctor/ExamOrderView.vue'),
        meta: { title: '修改检查单' }
      },
      {
        path: 'doctor/exam-result/:orderId',
        name: 'ExamResult',
        component: () => import('@/views/doctor/ExamResultView.vue'),
        meta: { title: '检查结果' }
      },
      // M03 诊疗记录 - 患者端
      {
        path: 'patient/records',
        name: 'PatientRecords',
        component: () => import('@/views/patient/PatientRecordView.vue'),
        meta: { title: '我的病历' }
      },
      // 患者端 - 检查费支付
      {
        path: 'exam/pay/:orderId',
        name: 'ExamPay',
        component: () => import('@/views/exam/ExamPayView.vue'),
        meta: { title: '检查费支付' }
      },
      // 患者端 - 处方药费支付
      {
        path: 'prescription/pay/:prescriptionId',
        name: 'PrescriptionPay',
        component: () => import('@/views/prescription/PrescriptionPayView.vue'),
        meta: { title: '处方支付' }
      },
      // 患者端 - 钱包
      {
        path: 'wallet',
        name: 'Wallet',
        component: () => import('@/views/wallet/WalletView.vue'),
        meta: { title: '我的钱包' }
      },
      // M04 AI 辅助诊断 - 患者端
      {
        path: 'ai/triage',
        name: 'AiTriage',
        component: () => import('@/views/ai/TriageView.vue'),
        meta: { title: '智能分诊' }
      },
      {
        path: 'ai/triage-history',
        name: 'AiTriageHistory',
        component: () => import('@/views/ai/TriageHistoryView.vue'),
        meta: { title: '分诊历史' }
      },
      // M04 AI 辅助诊断 - 医生端
      {
        path: 'ai/diagnosis/:patientId',
        name: 'AiDiagnosis',
        component: () => import('@/views/ai/DiagnosisView.vue'),
        meta: { title: 'AI 辅助诊断' }
      },
      {
        path: 'ai/diagnosis-report/:diagnosisId',
        name: 'AiDiagnosisReport',
        component: () => import('@/views/ai/DiagnosisReportView.vue'),
        meta: { title: '诊断报告' }
      },
      // M04 AI 辅助诊断 - 管理端
      {
        path: 'admin/ai/prompts',
        name: 'AiPromptManage',
        component: () => import('@/views/admin/AiPromptManageView.vue'),
        meta: { title: 'Prompt 模板', admin: true }
      },
      {
        path: 'admin/ai/disease-kb',
        name: 'DiseaseKbManage',
        component: () => import('@/views/admin/DiseaseKbView.vue'),
        meta: { title: '疾病知识库', admin: true }
      },
      {
        path: 'admin/ai/monitor',
        name: 'AiMonitor',
        component: () => import('@/views/admin/AiMonitorView.vue'),
        meta: { title: 'AI 调用监控', admin: true }
      },
      // M05 影像管理
      { path: 'image/upload/:orderId?', name: 'ImageUpload', component: () => import('@/views/image/ImageUploadView.vue'), meta: { title: '影像上传' } },
      { path: 'image/list', name: 'ImageList', component: () => import('@/views/image/ImageListView.vue'), meta: { title: '影像列表' } },
      { path: 'image/viewer/:imageId', name: 'ImageViewer', component: () => import('@/views/image/ImageViewerView.vue'), meta: { title: '影像预览' } },
      { path: 'image/compare', name: 'ImageCompare', component: () => import('@/views/image/ImageCompareView.vue'), meta: { title: '影像对比' } },
      // 医生影像检查单
      { path: 'doctor/exam-images', name: 'ExamImageList', component: () => import('@/views/doctor/ExamImageList.vue'), meta: { title: '全部检查单' } },

      // M07 药库管理
      { path: 'pharmacy/drugs', name: 'DrugList', component: () => import('@/views/pharmacy/DrugListView.vue'), meta: { title: '药品管理' } },
      { path: 'pharmacy/drug/:drugId', name: 'DrugForm', component: () => import('@/views/pharmacy/DrugFormView.vue'), meta: { title: '药品详情' } },
      { path: 'pharmacy/inventory', name: 'Inventory', component: () => import('@/views/pharmacy/InventoryView.vue'), meta: { title: '库存管理' } },
      { path: 'pharmacy/dispense', name: 'Dispense', component: () => import('@/views/pharmacy/DispenseView.vue'), meta: { title: '发药管理' } },
      { path: 'pharmacy/warehouse', name: 'Warehouse', component: () => import('@/views/pharmacy/WarehouseView.vue'), meta: { title: '仓库管理' } },
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
