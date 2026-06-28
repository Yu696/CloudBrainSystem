<template>
  <div class="prescription-page">
    <div class="page-title">
      <el-button text @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      {{ isEdit ? '修改处方' : '处方开具' }}
    </div>

    <div v-loading="loading" class="prescription-content">
      <div class="cb-card">
        <div class="cb-card-header">
          <el-icon class="header-icon"><FirstAidKit /></el-icon>
          <span>处方信息</span>
          <el-tag v-if="recordInfo" type="info" size="small" style="margin-left:12px">
            病历号: {{ recordInfo.recordId }}
          </el-tag>
        </div>
        <div class="cb-card-body">
          <el-form ref="formRef" :model="form" label-width="80px">
            <el-form-item label="处方描述">
              <el-input v-model="form.prescriptionDesc" type="textarea" :rows="2" placeholder="处方描述（选填）" maxlength="500" show-word-limit />
            </el-form-item>

            <el-divider>药品明细</el-divider>

            <div v-for="(item, index) in form.items" :key="index" class="drug-item">
              <div class="drug-item-header">
                <span class="drug-index">药品 {{ index + 1 }}</span>
                <el-button type="danger" text size="small" @click="removeItem(index)" :disabled="form.items.length <= 1">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </div>

              <el-row :gutter="16">
                <el-col :span="10">
                  <el-form-item label="药品名称" :prop="`items.${index}.drugName`" :rules="[{ required: true, message: '必填', trigger: 'blur' }]">
                    <el-input v-model="item.drugName" placeholder="请输入药品名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="7">
                  <el-form-item label="规格">
                    <el-input v-model="item.spec" placeholder="如 0.25g×12片" />
                  </el-form-item>
                </el-col>
                <el-col :span="7">
                  <el-form-item label="剂量" :prop="`items.${index}.dosage`" :rules="[{ required: true, message: '必填', trigger: 'blur' }]">
                    <el-input v-model="item.dosage" placeholder="如 0.25g" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="16">
                <el-col :span="7">
                  <el-form-item label="频次" :prop="`items.${index}.frequency`" :rules="[{ required: true, message: '必填', trigger: 'blur' }]">
                    <el-select v-model="item.frequency" placeholder="选择频次" style="width:100%">
                      <el-option label="每日1次(qd)" value="qd" />
                      <el-option label="每日2次(bid)" value="bid" />
                      <el-option label="每日3次(tid)" value="tid" />
                      <el-option label="每晚1次(qn)" value="qn" />
                      <el-option label="必要时(prn)" value="prn" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="7">
                  <el-form-item label="途径">
                    <el-select v-model="item.administration" placeholder="给药途径" style="width:100%">
                      <el-option label="口服" value="口服" />
                      <el-option label="静脉注射" value="静脉注射" />
                      <el-option label="肌肉注射" value="肌肉注射" />
                      <el-option label="皮下注射" value="皮下注射" />
                      <el-option label="外用" value="外用" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="5">
                  <el-form-item label="天数" :prop="`items.${index}.days`" :rules="[{ required: true, message: '必填', trigger: 'blur' }]">
                    <el-input-number v-model="item.days" :min="1" :max="90" controls-position="right" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="5">
                  <el-form-item label="数量" :prop="`items.${index}.quantity`" :rules="[{ required: true, message: '必填', trigger: 'blur' }]">
                    <el-input-number v-model="item.quantity" :min="1" :max="999" controls-position="right" style="width:100%" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="16">
                <el-col :span="8">
                  <el-form-item label="单价(元)">
                    <el-input-number v-model="item.unitPrice" :min="0" :precision="2" controls-position="right" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="小计">
                    <span class="subtotal-text">¥{{ ((item.unitPrice || 0) * (item.quantity || 0)).toFixed(2) }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="备注">
                    <el-input v-model="item.remark" placeholder="选填" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>

            <el-button type="primary" plain @click="addItem" style="width:100%;margin-top:8px">
              <el-icon><Plus /></el-icon>添加药品
            </el-button>

            <el-divider />

            <!-- AI 处方审核 -->
            <div class="audit-trigger-row">
              <el-button type="warning" plain @click="handleAiAudit" :loading="auditLoading" :disabled="!canAudit">
                <el-icon><MagicStick /></el-icon>AI 处方审核
              </el-button>
              <span v-if="!canAudit" class="audit-hint">请先填写药品名称后发起审核</span>
            </div>
            <PrescriptionAuditPanel
              :audit-result="auditResult"
              :loading="auditLoading"
              :fallback-mode="auditFallbackMode"
            />

            <el-divider />

            <div class="total-row">
              <span class="total-label">处方总额：</span>
              <span class="total-value">¥{{ totalAmount.toFixed(2) }}</span>
            </div>

            <div class="form-actions">
              <el-button @click="router.back()">返回</el-button>
              <el-button @click="handleSaveDraft" :loading="saving">保存草稿</el-button>
              <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting">提交处方</el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, FirstAidKit, Plus, Delete, MagicStick } from '@element-plus/icons-vue'
import { createPrescriptionApi, updatePrescriptionApi, getPrescriptionDetailApi, getMedicalRecordDetailApi } from '@/api/medical'
import { prescriptionCheckApi } from '@/api/ai'
import PrescriptionAuditPanel from '@/components/ai/PrescriptionAuditPanel.vue'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const route = useRoute()
const recordId = route.params.recordId as string
const prescriptionId = (route.params.prescriptionId as string) || '' // 编辑模式时有值
const isEdit = !!prescriptionId
const editRecordId = ref('') // 编辑模式下记录原病历ID

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const submitting = ref(false)
const recordInfo = ref<any>(null)

// AI 处方审核状态
const auditResult = ref<any>(null)
const auditLoading = ref(false)
const auditFallbackMode = ref(false)

interface DrugItem {
  drugId: string
  drugName: string
  spec: string
  dosage: string
  frequency: string
  administration: string
  days: number
  quantity: number
  unitPrice: number
  remark: string
}

const form = reactive<{ prescriptionDesc: string; items: DrugItem[] }>({
  prescriptionDesc: '',
  items: [createEmptyItem()]
})

function createEmptyItem(): DrugItem {
  return {
    drugId: 'DRUG_' + Date.now(),
    drugName: '',
    spec: '',
    dosage: '',
    frequency: '',
    administration: '口服',
    days: 3,
    quantity: 1,
    unitPrice: 0,
    remark: ''
  }
}

const totalAmount = computed(() => {
  return form.items.reduce((sum, item) => {
    return sum + (item.unitPrice || 0) * (item.quantity || 0)
  }, 0)
})

const canAudit = computed(() => {
  return form.items.some((item: DrugItem) => item.drugName.trim() !== '')
})

onMounted(async () => {
  loading.value = true
  try {
    // 编辑模式：加载现有处方数据
    if (isEdit) {
      const presRes = await getPrescriptionDetailApi(prescriptionId)
      const pres = presRes.data as any
      form.prescriptionDesc = pres.prescriptionDesc || ''
      if (pres.items && pres.items.length > 0) {
        form.items = pres.items.map((it: any) => ({
          drugId: it.drugId || 'DRUG_' + Date.now(),
          drugName: it.drugName || '',
          spec: it.spec || '',
          dosage: it.dosage || '',
          frequency: it.frequency || '',
          administration: it.administration || '口服',
          days: it.days || 3,
          quantity: it.quantity || 1,
          unitPrice: it.unitPrice || 0,
          remark: it.remark || ''
        }))
      }
      // 加载病历信息
      editRecordId.value = pres.recordId
      const recRes = await getMedicalRecordDetailApi(pres.recordId)
      recordInfo.value = recRes.data
    } else {
      const res = await getMedicalRecordDetailApi(recordId)
      recordInfo.value = res.data
    }
  } catch { /* ignore */ }
  finally { loading.value = false }
})

function addItem() {
  form.items.push(createEmptyItem())
}

function removeItem(index: number) {
  if (form.items.length > 1) {
    form.items.splice(index, 1)
  }
}

async function handleSaveDraft() {
  saving.value = true
  try {
    const data = {
      recordId: isEdit ? editRecordId.value : recordId,
      prescriptionDesc: form.prescriptionDesc,
      items: form.items,
      status: 0 // 草稿
    }
    if (isEdit) {
      await updatePrescriptionApi(prescriptionId, data)
      ElMessage.success('处方草稿已更新')
    } else {
      await createPrescriptionApi(data)
      ElMessage.success('处方草稿已保存')
    }
  } catch { /* handled by interceptor */ }
  finally { saving.value = false }
}

async function handleSubmit() {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }

  submitting.value = true
  try {
    const data = {
      recordId: isEdit ? editRecordId.value : recordId,
      prescriptionDesc: form.prescriptionDesc,
      items: form.items,
      status: 1 // 待审核（正式提交）
    }
    if (isEdit) {
      await updatePrescriptionApi(prescriptionId, data)
      ElMessage.success('处方已更新并提交审核')
    } else {
      await createPrescriptionApi(data)
      ElMessage.success('处方开具成功，已提交审核')
    }
    router.back()
  } catch { /* handled by interceptor */ }
  finally { submitting.value = false }
}

async function handleAiAudit() {
  if (!canAudit.value) return
  auditLoading.value = true
  auditResult.value = null
  auditFallbackMode.value = false
  try {
    const data = {
      prescriptionId: isEdit ? prescriptionId : '',
      recordId: isEdit ? editRecordId.value : recordId,
      patientId: recordInfo.value?.patientId || '',
      doctorId: recordInfo.value?.doctorId || '',
      items: form.items.map((item: DrugItem) => ({
        drugId: item.drugId,
        drugName: item.drugName,
        dosage: item.dosage,
        frequency: item.frequency,
        days: item.days
      }))
    }
    const res = await prescriptionCheckApi(data)
    auditResult.value = (res.data as any)?.auditResult || res.data
  } catch (err: any) {
    if (err.response?.status === 500 || err.message?.includes('AI 服务')) {
      auditFallbackMode.value = true
    } else {
      ElMessage.error('AI 审核请求失败')
    }
  } finally {
    auditLoading.value = false
  }
}
</script>

<style scoped>
.prescription-page {
  max-width: 1000px;
}

.header-icon {
  color: var(--cb-primary);
  font-size: 18px;
}

.drug-item {
  background: var(--cb-background);
  border-radius: var(--cb-radius-md);
  padding: 16px;
  margin-bottom: 12px;
}

.drug-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--cb-border);
}

.drug-index {
  font-weight: 600;
  color: var(--cb-primary);
  font-size: var(--cb-font-sm);
}

.subtotal-text {
  font-weight: 600;
  color: var(--cb-primary);
  font-size: var(--cb-font-base);
}

.total-row {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 12px 0;
  gap: 12px;
}

.total-label {
  font-size: var(--cb-font-lg);
  font-weight: 600;
  color: var(--cb-text-primary);
}

.total-value {
  color: var(--cb-danger);
  font-size: 22px;
  font-weight: 700;
}

.audit-trigger-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.audit-hint {
  font-size: var(--cb-font-xs);
  color: var(--cb-text-placeholder);
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 8px;
}

.form-actions .el-button {
  min-width: 140px;
}
</style>
