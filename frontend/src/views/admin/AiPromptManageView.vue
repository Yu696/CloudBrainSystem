<template>
  <div class="admin-page">
    <div class="page-title">Prompt 模板管理</div>

    <div class="search-bar">
      <el-select v-model="filterType" placeholder="模板类型" clearable style="width:160px" @change="loadTemplates">
        <el-option label="分诊模板" :value="0" />
        <el-option label="病历生成模板" :value="1" />
        <el-option label="处方审核模板" :value="2" />
      </el-select>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>新增模板
      </el-button>
    </div>

    <div class="cb-card">
      <el-table :data="templates" v-loading="loading" stripe style="width:100%">
        <el-table-column prop="templateId" label="ID" width="200" show-overflow-tooltip />
        <el-table-column prop="templateName" label="模板名称" min-width="160" />
        <el-table-column label="类型" width="130">
          <template #default="{ row }">
            <el-tag :type="row.templateType === 0 ? 'primary' : row.templateType === 1 ? 'success' : 'warning'" size="small">
              {{ row.templateType === 0 ? '分诊' : row.templateType === 1 ? '病历生成' : '处方审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容预览" min-width="300" show-overflow-tooltip />
        <el-table-column prop="variables" label="变量" width="240">
          <template #default="{ row }">
            <span v-if="row.variables?.length">
              <el-tag v-for="v in row.variables" :key="v" size="small" style="margin:1px 2px">{{ formatVarLabel(v) }}</el-tag>
            </span>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="toggleStatus(row)" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑模板' : '新增模板'" width="700px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="form.templateName" placeholder="输入模板名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="模板类型" prop="templateType">
          <el-select v-model="form.templateType" placeholder="选择类型" style="width:100%" @change="onTypeChange">
            <el-option label="分诊模板" :value="0" />
            <el-option label="病历生成模板" :value="1" />
            <el-option label="处方审核模板" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板内容" prop="content">
          <el-input ref="contentInputRef" v-model="form.content" type="textarea" :rows="8" placeholder="使用 {{变量名}} 作为占位符" maxlength="5000" show-word-limit @blur="onContentBlur" @mouseup="onContentCursorChange" @keyup="onContentCursorChange" />
        </el-form-item>
        <el-form-item label="变量">
          <el-select v-model="form.variables" multiple filterable allow-create default-first-option placeholder="输入变量名后回车" style="width:100%">
            <el-option v-for="v in presetVars" :key="v" :label="formatVarLabel(v)" :value="v" />
          </el-select>
          <div class="form-tip">在模板内容中用 <code>\{\{变量名\}\}</code> 引用，输入的变量名不带大括号</div>
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { listPromptTemplatesApi, createPromptTemplateApi, updatePromptTemplateApi, deletePromptTemplateApi, togglePromptTemplateStatusApi } from '@/api/ai'
import type { FormInstance } from 'element-plus'

const formRef = ref<FormInstance>()
const contentInputRef = ref<any>(null)  // 模板内容文本框引用，用于获取光标位置
const savedCursorPos = ref(-1)          // 保存文本框失焦前时光标位置，-1 表示末尾
const loading = ref(false)
const saving = ref(false)
const templates = ref<any[]>([])
const filterType = ref<number | undefined>(undefined)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref('')

/** 变量元数据：key → { label, types (适用的模板类型) } */
const varMetaList: { key: string; label: string; types: number[] }[] = [
  // 通用变量（PatientContext 系统自动注入）
  { key: 'age',                label: '年龄',       types: [0, 1] },
  { key: 'gender',             label: '性别',       types: [0, 1] },
  { key: 'allergy_history',    label: '过敏史',     types: [0, 1, 2] },
  { key: 'medical_history',    label: '既往病史',   types: [0, 1, 2] },
  { key: 'current_medications',label: '当前用药',   types: [0, 1, 2] },
  // 分诊(type=0) 专用
  { key: 'chief_complaint',    label: '主诉',       types: [0, 1] },
  { key: 'dept_hint',          label: '可选科室列表', types: [0] },
  // 诊断(type=1) 专用
  { key: 'present_illness',    label: '现病史',     types: [1] },
  { key: 'physical_exam',      label: '体格检查',   types: [1] },
  { key: 'auxiliary_exam',     label: '辅助检查',   types: [1] },
  { key: 'disease_knowledge_ref', label: '疾病知识库参考', types: [1] },
  // 处方审核(type=2) 专用
  { key: 'prescription_items', label: '待审核处方药品', types: [2] },
  // 病历生成(type=1) 专用
  { key: 'dialogue',           label: '医患对话内容', types: [1] },
  { key: 'patient_info',       label: '患者综合信息', types: [1, 2] },
]

/** key → label 快速查找 */
const varLabelMap: Record<string, string> = {}
for (const v of varMetaList) {
  varLabelMap[v.key] = v.label
}

/** 格式化变量显示：中文名：{{变量名}} */
function formatVarLabel(v: string): string {
  const cn = varLabelMap[v]
  return cn ? `${cn}：{{${v}}}` : `{{${v}}}`
}

/** 按模板类型获取预设 JSON Schema */
function getDefaultContentForType(type: number): string {
  switch (type) {
    case 0: // 分诊
      return '你是一位资深全科医生。请根据以下患者主诉进行智能分诊分析。\n请严格以 JSON 格式返回结果。\n\n' +
        '患者主诉：{{chief_complaint}}\n' +
        '年龄：{{age}}，性别：{{gender}}\n' +
        '过敏史：{{allergy_history}}\n既往病史：{{medical_history}}\n当前用药：{{current_medications}}\n\n' +
        '请返回 JSON：\n' +
        '{\n  "recommendedDepartment": {"departmentName": "", "confidence": 0.0},\n  ' +
        '"alternativeDepartments": [{"departmentName": "", "confidence": 0.0}],\n  ' +
        '"diseaseMatches": [{"diseaseName": "", "icdCode": "", "confidence": 0.0, "matchedSymptoms": []}],\n  ' +
        '"confidenceScore": 0.0,\n  "analysisDetail": ""\n}'
    case 1: // 病历生成
      return '你是一位医疗文书专家。请将以下医患对话转换为结构化病历。\n请严格以 JSON 格式返回结果。\n\n' +
        '对话内容：{{dialogue}}\n患者信息：{{patient_info}}\n\n' +
        '请返回 JSON：\n' +
        '{\n  "recordPreview": {\n    "chiefComplaint": "",\n    "presentIllness": "",\n    ' +
        '"pastHistory": "",\n    "physicalExam": "",\n    "preliminaryDiagnosis": "",\n    ' +
        '"treatmentPlan": ""\n  }\n}'
    case 2: // 处方审核
      return '你是一位临床药学专家。请审核以下处方。\n请严格以 JSON 格式返回结果。\n\n' +
        '患者信息：{{patient_info}}\n处方药品：{{prescription_items}}\n\n' +
        '请返回 JSON：\n' +
        '{\n  "overallResult": "PASS",\n  ' +
        '"items": [{"drugName": "", "result": "PASS", "checks": [' +
        '{"checkType": "", "result": "PASS", "detail": ""}]}],\n  ' +
        '"summary": "",\n  "confidenceScore": 0.0\n}'
    default: return ''
  }
}

const form = reactive({
  templateName: '',
  templateType: 0,
  content: '',
  variables: [] as string[]
})

const rules = {
  templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  templateType: [{ required: true, message: '请选择模板类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入模板内容', trigger: 'blur' }]
}

/** 按当前模板类型过滤可用变量列表 */
const presetVars = computed(() =>
  varMetaList
    .filter(v => v.types.includes(form.templateType))
    .map(v => v.key)
)

onMounted(() => { loadTemplates() })

const textareaEl = () => contentInputRef.value?.$el?.querySelector('textarea') as HTMLTextAreaElement | null

/** 鼠标/键盘操作时记录光标位置 */
function onContentCursorChange() {
  const el = textareaEl()
  if (el) savedCursorPos.value = el.selectionStart
}

/** 文本框失焦时记录光标位置（点击变量框前最后一刻） */
function onContentBlur() {
  const el = textareaEl()
  if (el) savedCursorPos.value = el.selectionStart
}

// 变量列表变化：新增自动在光标处插入"中文名：{{变量}}"，删除只移除该项并回退光标
watch(() => [...form.variables], (newVars, oldVars) => {
  const old = oldVars || []
  // 新增的变量 → 在光标位置插入"中文名：{{变量}}"
  const added = newVars.filter((v: string) => !old.includes(v))
  for (const v of added) {
    const label = varLabelMap[v]
    const snippet = label ? `\n${label}：{{${v}}}` : `{{${v}}}`
    if (!form.content.includes(`{{${v}}}`)) {
      const pos = savedCursorPos.value >= 0 ? savedCursorPos.value : form.content.length
      form.content = form.content.slice(0, pos) + snippet + form.content.slice(pos)
      // 更新光标记录到插入内容末尾
      savedCursorPos.value = pos + snippet.length
    }
  }
  // 删除的变量：独占一行则连前置换行一起删，否则只删字段本身
  const removed = old.filter((v: string) => !newVars.includes(v))
  for (const v of removed) {
    const label = varLabelMap[v]
    if (label) {
      const pattern = new RegExp(`${label}[：:]\\s*\\{\\{${v}\\}\\}`, 'g')
      const match = pattern.exec(form.content)
      if (match) {
        const idx = match.index
        const len = match[0].length
        const before = idx > 0 ? form.content[idx - 1] : ''
        const after = idx + len < form.content.length ? form.content[idx + len] : ''
        // 判断是否独占一整行（前后都是换行，或开头/结尾）
        const isWholeLine = (idx === 0 || before === '\n') && (idx + len >= form.content.length || after === '\n')
        if (isWholeLine && before === '\n') {
          // 连前置换行一起删，光标移到换行处
          form.content = form.content.slice(0, idx - 1) + form.content.slice(idx + len)
          savedCursorPos.value = idx - 1
        } else {
          form.content = form.content.slice(0, idx) + form.content.slice(idx + len)
          savedCursorPos.value = idx
        }
      }
    }
  }
})

async function loadTemplates() {
  loading.value = true
  try {
    const res = await listPromptTemplatesApi(filterType.value)
    templates.value = (res.data as any[]) || []
  } catch { templates.value = [] }
  finally { loading.value = false }
}

function openCreate() {
  isEdit.value = false
  editId.value = ''
  form.templateName = ''
  form.templateType = 0
  form.content = getDefaultContentForType(0)
  form.variables = []
  dialogVisible.value = true
}

/** 切换模板类型时自动刷新预设内容，并清理不属于新类型的变量 */
function onTypeChange(type: number) {
  // 清理不属于新类型的变量
  const validKeys = varMetaList.filter(v => v.types.includes(type)).map(v => v.key)
  form.variables = form.variables.filter(v => validKeys.includes(v))

  if (!isEdit.value) {
    // 如果当前内容是其他类型的预设，自动替换为新类型的预设
    const allDefaults = [0, 1, 2].map(t => getDefaultContentForType(t))
    if (!form.content || allDefaults.includes(form.content)) {
      form.content = getDefaultContentForType(type)
    }
  }
}

function openEdit(row: any) {
  isEdit.value = true
  editId.value = row.templateId
  form.templateName = row.templateName
  form.templateType = row.templateType
  form.content = row.content
  form.variables = row.variables || []
  dialogVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }

  saving.value = true
  try {
    if (isEdit.value) {
      await updatePromptTemplateApi({ templateId: editId.value, ...form })
      ElMessage.success('模板已更新')
    } else {
      await createPromptTemplateApi(form)
      ElMessage.success('模板已创建')
    }
    dialogVisible.value = false
    loadTemplates()
  } catch { /* handled by interceptor */ }
  finally { saving.value = false }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm('确定删除该模板吗？', '确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
    await deletePromptTemplateApi(row.templateId)
    ElMessage.success('模板已删除')
    loadTemplates()
  } catch { /* cancelled or error */ }
}

async function toggleStatus(row: any) {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await togglePromptTemplateStatusApi(row.templateId, newStatus)
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
    loadTemplates()
  } catch { loadTemplates() }
}
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: #8E8E93;
  margin-top: 6px;
}
.form-tip code {
  background: #f5f5f7;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 12px;
}
.no-data { color: #c0c4cc; }
</style>
