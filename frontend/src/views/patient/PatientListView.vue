<template>
  <div class="list-page">
    <div class="page-title">患者列表</div>

    <!-- 搜索栏 -->
    <div class="cb-card search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="患者姓名" clearable style="width: 160px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="手机号码" clearable style="width: 160px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="病历号">
          <el-input v-model="searchForm.medicalRecordNo" placeholder="病历号" clearable style="width: 180px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
          <el-button type="success" @click="router.push('/patient/create')">
            <el-icon><Plus /></el-icon>新建档案
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格 -->
    <div class="cb-card">
      <el-table :data="patients" v-loading="loading" stripe style="width: 100%" @row-click="handleRowClick" row-class-name="clickable-row">
        <el-table-column prop="medicalRecordNo" label="病历号" width="180" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="idCard" label="身份证号" width="180" />
        <el-table-column prop="age" label="年龄" width="70" align="center" />
        <el-table-column prop="bloodType" label="血型" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.bloodType" size="small">{{ row.bloodType }}型</el-tag>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="100" align="center">
          <template #default="{ row }">
            {{ row.source === 1 ? '医院建档' : row.source === 2 ? '在线注册' : row.source === 3 ? '转诊' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '归档' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="建档时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click.stop="router.push(`/patient/detail/${row.patientId}`)">
              详情
            </el-button>
            <el-button type="warning" link size="small" @click.stop="router.push(`/ai/diagnosis/${row.patientId}`)">
              AI诊断
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && patients.length === 0" description="暂无患者数据" :image-size="80" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { listPatientsApi, adminListPatientsApi, doctorListPatientsApi } from '@/api/patient'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const patients = ref<any[]>([])
const loading = ref(false)

const searchForm = reactive({
  name: '',
  phone: '',
  medicalRecordNo: ''
})

onMounted(() => {
  loadPatients()
})

async function loadPatients() {
  loading.value = true
  try {
    const params: any = {}
    if (searchForm.name) params.name = searchForm.name
    if (searchForm.phone) params.phone = searchForm.phone
    if (searchForm.medicalRecordNo) params.medicalRecordNo = searchForm.medicalRecordNo

    let res
    if (userStore.isAdmin) {
      res = await adminListPatientsApi(params)
    } else if (userStore.isDoctor) {
      res = await doctorListPatientsApi(params)
    } else {
      res = await listPatientsApi(params)
    }
    patients.value = (res.data as any[]) || []
  } catch {
    patients.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadPatients()
}

function handleReset() {
  searchForm.name = ''
  searchForm.phone = ''
  searchForm.medicalRecordNo = ''
  loadPatients()
}

function handleRowClick(row: any) {
  router.push(`/patient/detail/${row.patientId}`)
}
</script>

<style scoped>
.search-card {
  margin-bottom: 16px;
}

.search-card .el-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.search-card .el-form-item {
  margin-bottom: 0;
}

.clickable-row {
  cursor: pointer;
}

.no-data {
  color: var(--cb-text-placeholder);
}
</style>
