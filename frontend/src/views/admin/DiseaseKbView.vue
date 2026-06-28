<template>
  <div class="admin-page">
    <div class="page-title">疾病知识库</div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索疾病名称/ICD编码" clearable style="width:280px" @keyup.enter="search" />
      <el-button type="primary" @click="search">
        <el-icon><Search /></el-icon>搜索
      </el-button>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>新增疾病
      </el-button>
    </div>

    <div class="cb-card">
      <el-table :data="diseases" v-loading="loading" stripe style="width:100%">
        <el-table-column prop="diseaseName" label="疾病名称" min-width="160" />
        <el-table-column prop="icdCode" label="ICD编码" width="110" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column label="症状" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="s in (row.symptoms || [])" :key="s" size="small" style="margin:1px 3px">{{ s }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="diagnosisBasis" label="诊断依据" min-width="200" show-overflow-tooltip />
        <el-table-column prop="treatmentPlan" label="治疗方案" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && diseases.length === 0" style="text-align:center;padding:40px;color:#8E8E93">
        <el-empty description="暂无数据" :image-size="60" />
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑疾病' : '新增疾病'" width="700px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="疾病名称" prop="diseaseName">
          <el-input v-model="form.diseaseName" placeholder="输入疾病名称" maxlength="100" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="ICD编码" prop="icdCode">
              <el-input v-model="form.icdCode" placeholder="如 J06.9" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-input v-model="form.category" placeholder="如 呼吸系统" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="常见症状">
          <el-select v-model="form.symptoms" multiple filterable allow-create default-first-option placeholder="输入症状后回车" style="width:100%">
            <el-option label="发热" value="发热" />
            <el-option label="咳嗽" value="咳嗽" />
            <el-option label="头痛" value="头痛" />
            <el-option label="胸闷" value="胸闷" />
            <el-option label="腹痛" value="腹痛" />
            <el-option label="乏力" value="乏力" />
          </el-select>
        </el-form-item>
        <el-form-item label="诊断依据" prop="diagnosisBasis">
          <el-input v-model="form.diagnosisBasis" type="textarea" :rows="2" placeholder="诊断依据" maxlength="500" />
        </el-form-item>
        <el-form-item label="治疗方案">
          <el-input v-model="form.treatmentPlan" type="textarea" :rows="2" placeholder="治疗方案" maxlength="500" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { searchDiseaseKbApi, createDiseaseKbApi, updateDiseaseKbApi, deleteDiseaseKbApi } from '@/api/ai'
import type { FormInstance } from 'element-plus'

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const diseases = ref<any[]>([])
const keyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref('')

const form = reactive({
  diseaseName: '',
  icdCode: '',
  category: '',
  symptoms: [] as string[],
  diagnosisBasis: '',
  treatmentPlan: ''
})

const rules = {
  diseaseName: [{ required: true, message: '请输入疾病名称', trigger: 'blur' }],
  icdCode: [{ required: true, message: '请输入ICD编码', trigger: 'blur' }],
  category: [{ required: true, message: '请输入分类', trigger: 'blur' }],
  diagnosisBasis: [{ required: true, message: '请输入诊断依据', trigger: 'blur' }]
}

onMounted(() => { search() })

async function search() {
  loading.value = true
  try {
    const res = await searchDiseaseKbApi(keyword.value || undefined)
    diseases.value = (res.data as any[]) || []
  } catch { diseases.value = [] }
  finally { loading.value = false }
}

function openCreate() {
  isEdit.value = false
  editId.value = ''
  form.diseaseName = ''
  form.icdCode = ''
  form.category = ''
  form.symptoms = []
  form.diagnosisBasis = ''
  form.treatmentPlan = ''
  dialogVisible.value = true
}

function openEdit(row: any) {
  isEdit.value = true
  editId.value = row.diseaseId
  form.diseaseName = row.diseaseName
  form.icdCode = row.icdCode
  form.category = row.category
  form.symptoms = row.symptoms || []
  form.diagnosisBasis = row.diagnosisBasis
  form.treatmentPlan = row.treatmentPlan
  dialogVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }

  saving.value = true
  try {
    if (isEdit.value) {
      await updateDiseaseKbApi({ diseaseId: editId.value, ...form })
      ElMessage.success('疾病条目已更新')
    } else {
      await createDiseaseKbApi(form)
      ElMessage.success('疾病条目已创建')
    }
    dialogVisible.value = false
    search()
  } catch { /* handled */ }
  finally { saving.value = false }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.diseaseName}」吗？`, '确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
    await deleteDiseaseKbApi(row.diseaseId)
    ElMessage.success('已删除')
    search()
  } catch { /* cancelled */ }
}
</script>
