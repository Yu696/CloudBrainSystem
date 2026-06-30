<template>
  <div class="list-page">
    <div class="page-title">仓库管理</div>

    <!-- 操作栏 -->
    <div class="cb-card" style="margin-bottom: 16px; text-align: right">
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>新增仓库
      </el-button>
    </div>

    <!-- 表格 -->
    <div class="cb-card">
      <el-table :data="warehouseList" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="name" label="仓库名称" min-width="160" />
        <el-table-column prop="location" label="仓库位置" min-width="200" />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 0 ? 'primary' : 'success'" size="small">
              {{ row.type === 0 ? '药库' : '药房' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && warehouseList.length === 0" description="暂无仓库数据" :image-size="80" />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingWarehouse ? '编辑仓库' : '新增仓库'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="仓库名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入仓库名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="仓库位置">
          <el-input v-model="form.location" placeholder="请输入仓库位置" maxlength="200" />
        </el-form-item>
        <el-form-item label="管理员 ID">
          <el-input v-model="form.adminId" placeholder="请输入管理员 ID" />
        </el-form-item>
        <el-form-item label="仓库类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择仓库类型" style="width: 100%">
            <el-option label="药库" :value="0" />
            <el-option label="药房" :value="1" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { warehouseListApi, addWarehouseApi, updateWarehouseApi, deleteWarehouseApi } from '@/api/pharmacy'

const loading = ref(false)
const warehouseList = ref<any[]>([])

const dialogVisible = ref(false)
const submitting = ref(false)
const editingWarehouse = ref<any>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  name: '',
  location: '',
  adminId: '',
  type: undefined as number | undefined
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择仓库类型', trigger: 'change' }]
}

onMounted(() => {
  loadWarehouseList()
})

async function loadWarehouseList() {
  loading.value = true
  try {
    const res = await warehouseListApi()
    const data = res.data as any
    if (data) {
      warehouseList.value = Array.isArray(data) ? data : data.records || data.list || data.items || []
    } else {
      warehouseList.value = []
    }
  } catch {
    warehouseList.value = []
  } finally {
    loading.value = false
  }
}

function openAddDialog() {
  editingWarehouse.value = null
  form.name = ''
  form.location = ''
  form.adminId = ''
  form.type = undefined
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

function openEditDialog(row: any) {
  editingWarehouse.value = row
  form.name = row.name || ''
  form.location = row.location || ''
  form.adminId = row.adminId || ''
  form.type = row.type
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (editingWarehouse.value) {
      await updateWarehouseApi(editingWarehouse.value.warehouseId, {
        name: form.name,
        location: form.location || undefined,
        adminId: form.adminId || undefined,
        type: form.type!
      })
      ElMessage.success('更新仓库成功')
    } else {
      await addWarehouseApi({
        name: form.name,
        location: form.location || undefined,
        adminId: form.adminId || undefined,
        type: form.type!
      })
      ElMessage.success('新增仓库成功')
    }
    dialogVisible.value = false
    loadWarehouseList()
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定要删除仓库「${row.name}」吗？删除后不可恢复。`, '确认删除', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteWarehouseApi(row.warehouseId)
    ElMessage.success('删除成功')
    loadWarehouseList()
  } catch (e: any) {
    if (e !== 'cancel' && e?.toString() !== 'cancel') {
      // handled by interceptor or cancel
    }
  }
}
</script>

<style scoped>
.page-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
}
</style>
