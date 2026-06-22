<template>
  <div class="dept-manage">
    <div class="cb-card">
      <div class="cb-card-header">
        <span>科室管理</span>
        <el-button type="primary" size="small" @click="openCreate">新增科室</el-button>
      </div>
      <div class="cb-card-body">
        <el-table :data="departments" border stripe v-loading="loading">
          <el-table-column prop="departmentId" label="科室ID" width="120" />
          <el-table-column prop="name" label="科室名称" width="140" />
          <el-table-column prop="category" label="类别" width="100" />
          <el-table-column prop="parentId" label="上级科室" width="120">
            <template #default="{ row }">{{ row.parentId || '-' }}</template>
          </el-table-column>
          <el-table-column prop="description" label="简介" min-width="180" show-overflow-tooltip />
          <el-table-column prop="location" label="位置" width="120" />
          <el-table-column prop="phone" label="电话" width="130" />
          <el-table-column prop="sortOrder" label="排序" width="70" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
              <el-popconfirm title="确定删除该科室？" @confirm="handleDelete(row.departmentId)">
                <template #reference>
                  <el-button link type="danger" size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑科室' : '新增科室'" width="520px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="科室名称" required>
          <el-input v-model="form.name" placeholder="请输入科室名称" />
        </el-form-item>
        <el-form-item label="类别">
          <el-input v-model="form.category" placeholder="如：内科、外科" />
        </el-form-item>
        <el-form-item label="上级科室">
          <el-input v-model="form.parentId" placeholder="上级科室ID（选填）" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="科室简介" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" placeholder="科室位置" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="联系电话" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listDepartmentsApi, createDepartmentApi, updateDepartmentApi, deleteDepartmentApi } from '@/api/appointment'

interface Department {
  departmentId: string
  name: string
  parentId: string
  category: string
  description: string
  location: string
  phone: string
  sortOrder: number
}

const departments = ref<Department[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref('')
const form = ref({ name: '', category: '', parentId: '', description: '', location: '', phone: '', sortOrder: 0 })


async function loadData() {
  loading.value = true
  try {
    const res = await listDepartmentsApi()
    departments.value = res.data as Department[]
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  editingId.value = ''
  form.value = { name: '', category: '', parentId: '', description: '', location: '', phone: '', sortOrder: 0 }
  dialogVisible.value = true
}

function openEdit(row: Department) {
  isEdit.value = true
  editingId.value = row.departmentId
  form.value = {
    name: row.name,
    category: row.category || '',
    parentId: row.parentId || '',
    description: row.description || '',
    location: row.location || '',
    phone: row.phone || '',
    sortOrder: row.sortOrder || 0
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.name) {
    ElMessage.warning('请输入科室名称')
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await updateDepartmentApi(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createDepartmentApi(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function handleDelete(departmentId: string) {
  try {
    await deleteDepartmentApi(departmentId)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // error handled by interceptor
  }
}

onMounted(loadData)
</script>
