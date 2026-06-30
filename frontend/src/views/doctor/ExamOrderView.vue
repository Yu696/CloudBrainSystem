<template>
  <div class="exam-page">
    <div class="page-title">
      <el-button text @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      {{ isEdit ? '修改检查单' : '检查单开单' }}
    </div>

    <div v-loading="loading" class="exam-content">
      <el-row :gutter="24">
        <el-col :span="14">
          <div class="cb-card">
            <div class="cb-card-header">
              <el-icon class="header-icon"><Search /></el-icon>
              <span>选择检查项目</span>
            </div>
            <div class="cb-card-body">
              <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
                <el-form-item label="检查类别" prop="examCategory">
                  <el-radio-group v-model="form.examCategory">
                    <el-radio-button :value="0">实验室检查</el-radio-button>
                    <el-radio-button :value="1">影像学检查</el-radio-button>
                    <el-radio-button :value="2">功能检查</el-radio-button>
                  </el-radio-group>
                </el-form-item>

                <el-form-item label="检查项目" prop="examName">
                  <el-select v-model="form.examName" placeholder="请选择检查项目" filterable style="width: 100%">
                    <el-option v-for="item in filteredExamOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>

                <el-form-item label="检查目的">
                  <el-input v-model="form.examPurpose" type="textarea" :rows="3" placeholder="请输入检查目的" maxlength="500" />
                </el-form-item>

                <el-divider />

                <div class="form-actions">
                  <el-button @click="router.back()">返回</el-button>
                  <el-button v-if="editingOrderId" @click="cancelEdit">取消编辑</el-button>
                  <el-button type="primary" @click="handleSubmit" :loading="submitting">
                    {{ editingOrderId ? '保存修改' : '确认开单' }}
                  </el-button>
                </div>
              </el-form>
            </div>
          </div>
        </el-col>

        <el-col :span="10">
          <div class="cb-card tips-card">
            <div class="cb-card-header">
              <el-icon class="header-icon"><InfoFilled /></el-icon>
              <span>已开检查单</span>
            </div>
            <div class="cb-card-body">
              <div v-if="existingOrders.length === 0" class="empty-hint">暂无已开检查单</div>
              <div v-for="order in existingOrders" :key="order.orderId" class="order-item" :class="{ 'order-item-active': editingOrderId === order.orderId }">
                <div class="order-name" @click="editExisting(order)">{{ order.examName }}</div>
                <div class="order-meta">
                  <el-tag :type="orderStatusTag(order.status)" size="small">{{ orderStatusText(order.status) }}</el-tag>
                  <span class="order-category">{{ examCategoryText(order.examCategory) }}</span>
                  <span class="order-amount">¥{{ order.amount || 0 }}</span>
                </div>
                <div class="order-actions">
                  <el-button size="small" text type="warning" @click="editExisting(order)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button size="small" text type="danger" @click="handleDelete(order)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Search, InfoFilled, Edit, Delete } from '@element-plus/icons-vue'
import { createExaminationOrderApi, updateExaminationOrderApi, deleteExaminationOrderApi, listExaminationOrdersApi } from '@/api/medical'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const route = useRoute()
const recordId = route.params.recordId as string
const orderId = (route.params.orderId as string) || ''
const isEdit = !!orderId

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)
const existingOrders = ref<any[]>([])
const editingOrderId = ref(orderId)

const form = reactive({
  examCategory: 0 as number,
  examName: '',
  examPurpose: ''
})

const rules = {
  examCategory: [{ required: true, message: '请选择检查类别', trigger: 'change' }],
  examName: [{ required: true, message: '请选择或输入检查项目', trigger: 'blur' }]
}

const filteredExamOptions = computed(() => {
  const items = examOptions[form.examCategory]
  return items ? items.options : []
})

// 切换类别时清空已选项目
watch(() => form.examCategory, () => {
  form.examName = ''
})

const examOptions = [
  {
    label: '实验室检查',
    options: ['血常规', '尿常规', '肝功能全套', '肾功能全套', '血糖', '血脂全套', '电解质', '凝血功能', '心肌酶谱', '甲状腺功能', '肿瘤标志物']
  },
  {
    label: '影像学检查',
    options: ['X光胸片', 'CT平扫', 'CT增强', 'MRI平扫', 'MRI增强', 'B超腹部', 'B超心脏', 'B超甲状腺', 'B超妇科', 'PET-CT']
  },
  {
    label: '功能检查',
    options: ['心电图', '动态心电图', '肺功能检测', '脑电图', '肌电图', '胃镜', '肠镜', '支气管镜', '骨密度检测', '视力检查']
  }
]

onMounted(async () => {
  await loadExistingOrders()
  // 编辑模式：从已加载列表中找到匹配项预填表单
  if (isEdit) {
    const existing = existingOrders.value.find((o: any) => o.orderId === orderId)
    if (existing) {
      form.examCategory = existing.examCategory
      form.examName = existing.examName
      form.examPurpose = existing.examPurpose || ''
    }
  }
})

async function loadExistingOrders() {
  loading.value = true
  try {
    const res = await listExaminationOrdersApi(recordId)
    existingOrders.value = (res.data as any[]) || []
  } catch { existingOrders.value = [] }
  finally { loading.value = false }
}

function examCategoryText(cat: number): string {
  const map: Record<number, string> = { 0: '实验室', 1: '影像学', 2: '功能检查' }
  return map[cat] || '未知'
}

function orderStatusTag(status: number): string {
  const map: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger' }
  return map[status] || 'info'
}

function orderStatusText(status: number): string {
  const map: Record<number, string> = { 0: '已开单', 1: '已缴费', 2: '检查中', 3: '已完成', 4: '已取消' }
  return map[status] || '未知'
}

function editExisting(order: any) {
  editingOrderId.value = order.orderId
  form.examCategory = order.examCategory
  form.examName = order.examName
  form.examPurpose = order.examPurpose || ''
}

function cancelEdit() {
  editingOrderId.value = ''
  form.examCategory = 0
  form.examName = ''
  form.examPurpose = ''
}

async function handleDelete(order: any) {
  try {
    await ElMessageBox.confirm('确定删除该检查单吗？', '提示', { type: 'warning' })
  } catch { return }
  try {
    await deleteExaminationOrderApi(order.orderId)
    ElMessage.success('检查单已删除')
    if (editingOrderId.value === order.orderId) cancelEdit()
    await loadExistingOrders()
  } catch { /* handled by interceptor */ }
}

async function handleSubmit() {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }

  submitting.value = true
  try {
    if (editingOrderId.value) {
      await updateExaminationOrderApi(editingOrderId.value, { ...form })
      ElMessage.success('检查单已更新')
      cancelEdit()
    } else {
      await createExaminationOrderApi({ recordId, ...form })
      ElMessage.success('检查单已开具')
      form.examName = ''
      form.examPurpose = ''
    }
    await loadExistingOrders()
  } catch { /* handled by interceptor */ }
  finally { submitting.value = false }
}
</script>

<style scoped>
.exam-page {
  max-width: 1000px;
}

.header-icon {
  color: var(--cb-primary);
  font-size: 18px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 8px;
}

.form-actions .el-button {
  min-width: 120px;
}

.tips-card {
  position: sticky;
  top: 24px;
}

.empty-hint {
  text-align: center;
  color: var(--cb-text-placeholder);
  font-size: var(--cb-font-sm);
  padding: 20px 0;
}

.order-item {
  padding: 12px 0;
  border-bottom: 1px solid var(--cb-border-light);
  position: relative;
}

.order-item:hover {
  background: var(--cb-primary-lighter);
  border-radius: var(--cb-radius-sm);
  padding-left: 8px;
  padding-right: 8px;
}

.order-item-active {
  background: var(--cb-primary-lighter) !important;
  border-radius: var(--cb-radius-sm);
  padding-left: 8px;
  padding-right: 8px;
}

.order-item:last-child {
  border-bottom: none;
}

.order-name {
  font-weight: 500;
  color: var(--cb-text-primary);
  margin-bottom: 6px;
  cursor: pointer;
}

.order-name:hover {
  color: var(--cb-primary);
}

.order-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.order-category {
  font-size: var(--cb-font-xs);
  color: var(--cb-text-placeholder);
}

.order-amount {
  font-size: var(--cb-font-sm);
  font-weight: 600;
  color: var(--cb-danger);
}

.order-actions {
  position: absolute;
  right: 8px;
  top: 8px;
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.order-item:hover .order-actions {
  opacity: 1;
}
</style>
