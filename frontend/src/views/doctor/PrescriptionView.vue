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

              <!-- 药品选择 -->
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="药品名称" :prop="`items.${index}.drugName`" :rules="[{ required: true, message: '必填', trigger: 'change' }]">
                    <el-select
                      v-model="item.drugName"
                      filterable
                      remote
                      reserve-keyword
                      placeholder="输入药品名称搜索"
                      :remote-method="(query) => searchDrugs(query, index)"
                      :loading="drugSearchLoading"
                      style="width:100%"
                      @change="(val: string) => handleDrugSelect(val, index)"
                    >
                      <el-option
                        v-for="d in drugOptions"
                        :key="d.drugId"
                        :label="d.drugName"
                        :value="d.drugName"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="药品编码">
                    <el-input :model-value="item.drugCode" disabled placeholder="选择后自动填充" />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="通用名">
                    <el-input :model-value="item.genericName" disabled placeholder="选择后自动填充" />
                  </el-form-item>
                </el-col>
              </el-row>

              <!-- 药品信息（只读，自动填充） -->
              <div v-if="item.drugId" class="drug-info-bar">
                <el-tag size="small">{{ item.genericName || '—' }}</el-tag>
                <el-tag size="small" type="info">规格: {{ item.spec || '—' }}</el-tag>
                <el-tag size="small" type="info">剂型: {{ item.dosageForm || '—' }}</el-tag>
                <el-tag size="small" type="info">厂家: {{ item.manufacturer || '—' }}</el-tag>
                <el-tag size="small" type="info">单位: {{ item.unit || '—' }}</el-tag>
                <el-tag size="small" type="warning">单价: ¥{{ (item.unitPrice || 0).toFixed(2) }}</el-tag>
                <el-tag size="small" :type="item.prescriptionType === 0 ? 'success' : 'danger'">
                  {{ item.prescriptionType === 0 ? 'OTC' : '处方药' }}
                </el-tag>
                <el-tag size="small" type="info">{{ item.drugCategory || '—' }}</el-tag>
              </div>
              <div v-if="item.usageMethod" class="usage-method-hint">
                <el-icon><InfoFilled /></el-icon>
                <span>参考用法：{{ item.usageMethod }}</span>
              </div>
              <div v-if="item.cautiousCrowd || item.sideEffects" class="precautions-hint">
                <el-icon><WarningFilled /></el-icon>
                <div class="precautions-text">
                  <span v-if="item.cautiousCrowd"><strong>禁忌人群：</strong>{{ item.cautiousCrowd }}</span>
                  <span v-if="item.cautiousCrowd && item.sideEffects" class="precautions-sep" />
                  <span v-if="item.sideEffects"><strong>不良反应：</strong>{{ item.sideEffects }}</span>
                </div>
              </div>

              <!-- 用法用量（医生填写） -->
              <el-row :gutter="16" style="margin-top: 12px">
                <el-col :span="8">
                  <el-form-item label="每次用量" :prop="`items.${index}.dosage`" :rules="[{ required: true, message: '必填', trigger: 'blur' }]">
                    <el-input v-model="item.dosage" placeholder="如 0.25g / 2片" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="频次" :prop="`items.${index}.frequency`" :rules="[{ required: true, message: '必填', trigger: 'change' }]">
                    <el-select v-model="item.frequency" placeholder="选择频次" style="width:100%">
                      <el-option label="每日1次(qd)" value="qd" />
                      <el-option label="每日2次(bid)" value="bid" />
                      <el-option label="每日3次(tid)" value="tid" />
                      <el-option label="每晚1次(qn)" value="qn" />
                      <el-option label="必要时(prn)" value="prn" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="给药途径">
                    <el-select v-model="item.administration" placeholder="给药途径" style="width:100%">
                      <el-option label="口服" value="口服" />
                      <el-option label="静脉注射" value="静脉注射" />
                      <el-option label="肌肉注射" value="肌肉注射" />
                      <el-option label="皮下注射" value="皮下注射" />
                      <el-option label="外用" value="外用" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="16">
                <el-col :span="6">
                  <el-form-item label="数量" :prop="`items.${index}.quantity`" :rules="[{ required: true, message: '必填', trigger: 'blur' }]">
                    <el-input-number v-model="item.quantity" :min="1" :max="999" controls-position="right" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="单价(元)">
                    <el-input :model-value="'¥' + (item.unitPrice || 0).toFixed(2)" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="小计">
                    <span class="subtotal-text">¥{{ ((item.unitPrice || 0) * (item.quantity || 0)).toFixed(2) }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
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

            <div class="total-row">
              <span class="total-label">处方总额：</span>
              <span class="total-value">¥{{ totalAmount.toFixed(2) }}</span>
            </div>

            <div class="form-actions">
              <el-button @click="router.back()">返回</el-button>
              <el-button type="warning" plain @click="handleAiAudit" :loading="auditing">
                <el-icon><MagicStick /></el-icon>AI 审核
              </el-button>
              <el-button @click="handleSaveDraft" :loading="saving">保存草稿</el-button>
              <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting">提交处方</el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>

    <!-- AI 审核结果对话框 -->
    <el-dialog v-model="showAuditDialog" title="AI 处方审核结果" width="700px">
      <div v-if="auditResult" class="audit-content">
        <div class="audit-overall" :class="auditClass">
          <span class="audit-label">审核结论：</span>
          <el-tag :type="auditTagType" size="large">{{ auditResult.overallResultName || auditResult.overallResult }}</el-tag>
          <span v-if="auditResult.confidenceScore" class="audit-score">
            置信度：{{ (auditResult.confidenceScore * 100).toFixed(0) }}%
          </span>
        </div>
        <div v-if="auditResult.patientContext" class="audit-context">
          <el-alert v-if="auditResult.patientContext.allergyHistory" title="过敏史提醒" :description="auditResult.patientContext.allergyHistory" type="warning" show-icon :closable="false" />
        </div>
        <div v-if="auditResult.items && auditResult.items.length" class="audit-items">
          <div v-for="(item, idx) in auditResult.items" :key="idx" class="audit-drug">
            <div class="audit-drug-header">
              <span class="drug-name">{{ item.drugName }}</span>
              <el-tag :type="itemAuditTag(item.result)" size="small">{{ item.resultName || item.result }}</el-tag>
            </div>
            <div v-if="item.checks && item.checks.length" class="audit-checks">
              <div v-for="(check, cIdx) in item.checks" :key="cIdx" class="audit-check-item">
                <el-tag :type="itemAuditTag(check.result)" size="small" effect="plain">{{ check.checkType }}</el-tag>
                <span class="check-detail">{{ check.detail }}</span>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="AI 未返回药品审核明细，请检查处方信息是否完整" :image-size="60" />
        <div v-if="auditResult.summary" class="audit-summary">
          <el-divider />
          <p>{{ auditResult.summary }}</p>
        </div>
      </div>
      <div v-else-if="auditFallbackMode" class="audit-fallback">
        <el-alert title="AI 服务暂不可用" type="warning" description="请根据临床经验进行审核" show-icon :closable="false" />
      </div>
      <template #footer>
        <el-button @click="showAuditDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, FirstAidKit, Plus, Delete, MagicStick, InfoFilled, WarningFilled } from '@element-plus/icons-vue'
import { createPrescriptionApi, updatePrescriptionApi, getPrescriptionDetailApi } from '@/api/medical'
import { getMedicalRecordDetailApi } from '@/api/medical'
import { searchDrugApi } from '@/api/pharmacy'
import { prescriptionCheckApi } from '@/api/ai'
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
const auditing = ref(false)
const recordInfo = ref<any>(null)
const showAuditDialog = ref(false)
const auditResult = ref<any>(null)
const auditFallbackMode = ref(false)

interface DrugItem {
  drugId: string
  drugName: string
  drugCode: string
  genericName: string
  spec: string
  dosageForm: string
  manufacturer: string
  unit: string
  drugCategory: string
  prescriptionType: number
  usageMethod: string
  cautiousCrowd: string
  sideEffects: string
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
    drugCode: '',
    genericName: '',
    spec: '',
    dosageForm: '',
    manufacturer: '',
    unit: '',
    drugCategory: '',
    prescriptionType: 0,
    usageMethod: '',
    cautiousCrowd: '',
    sideEffects: '',
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

// --- 药品搜索 ---
const drugSearchLoading = ref(false)
const drugOptions = ref<any[]>([])
const drugSearchCache = ref<Record<string, any>>({})

async function searchDrugs(query: string, index: number) {
  if (!query || query.length < 1) { drugOptions.value = []; return }
  drugSearchLoading.value = true
  try {
    const res = await searchDrugApi({ keyword: query, pageSize: 20 })
    const data = res.data as any
    const list = data?.records || data?.list || (Array.isArray(data) ? data : [])
    drugOptions.value = list
    list.forEach((d: any) => { drugSearchCache.value[d.drugName] = d })
  } catch {
    drugOptions.value = []
  } finally {
    drugSearchLoading.value = false
  }
}

function handleDrugSelect(drugName: string, index: number) {
  const drug = drugSearchCache.value[drugName]
  if (!drug) return
  const item = form.items[index]
  item.drugId = drug.drugId || item.drugId
  item.drugCode = drug.drugCode || ''
  item.genericName = drug.genericName || ''
  item.spec = drug.spec || ''
  item.dosageForm = drug.dosageForm || ''
  item.manufacturer = drug.manufacturer || ''
  item.unit = drug.unit || ''
  item.drugCategory = drug.drugCategory || ''
  item.prescriptionType = drug.prescriptionType ?? 0
  item.unitPrice = drug.unitPrice ?? 0
  item.usageMethod = drug.usageMethod || ''
  item.cautiousCrowd = drug.cautiousCrowd || ''
  item.sideEffects = drug.sideEffects || ''

  // 从 usageMethod 自动解析填充用法用量
  const um = item.usageMethod
  if (um) {
    // 提取给药途径：开头的"口服"/"静脉注射"/"肌肉注射"/"皮下注射"/"外用"
    const adminMatch = um.match(/^(口服|静脉注射|肌肉注射|皮下注射|外用)/)
    if (adminMatch) item.administration = adminMatch[1]

    // 提取每次用量："一次(X)" → dosage
    const dosageMatch = um.match(/一次([^，,]+)/)
    if (dosageMatch) item.dosage = dosageMatch[1].trim()

    // 提取频次："一日(X)次" / "每日(X)次"
    const freqMatch = um.match(/[一每]日(\S*)次/)
    if (freqMatch) {
      const raw = freqMatch[1] // "2-3", "2", "1-2" 等
      if (raw.includes('3') || raw.includes('2-3') || raw.includes('1-3')) item.frequency = 'tid'
      else if (raw.includes('2') || raw.includes('1-2')) item.frequency = 'bid'
      else if (raw.includes('1')) item.frequency = 'qd'
      // 每晚 → qn
    }
  }
}

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
          drugCode: it.drugCode || '',
          genericName: it.genericName || '',
          spec: it.spec || '',
          dosageForm: it.dosageForm || '',
          manufacturer: it.manufacturer || '',
          unit: it.unit || '',
          drugCategory: it.drugCategory || '',
          prescriptionType: it.prescriptionType ?? 0,
          usageMethod: it.usageMethod || '',
          cautiousCrowd: it.cautiousCrowd || '',
          sideEffects: it.sideEffects || '',
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

async function handleAiAudit() {
  if (!recordInfo.value) {
    ElMessage.warning('请先加载病历信息')
    return
  }
  auditing.value = true
  try {
    const res = await prescriptionCheckApi({
      prescriptionId: isEdit ? prescriptionId : '',
      recordId: isEdit ? editRecordId.value : recordId,
      patientId: recordInfo.value.patientId || '',
      doctorId: recordInfo.value.doctorId || '',
      items: form.items.map(it => ({
        drugId: it.drugId,
        drugName: it.drugName,
        dosage: it.dosage,
        frequency: it.frequency,
        days: it.days
      }))
    })
    auditFallbackMode.value = false
    auditResult.value = res.data
    if (res.data?.auditId === 'FALLBACK' || (!res.data?.summary && !res.data?.items?.length)) {
      auditFallbackMode.value = true
    }
    showAuditDialog.value = true
  } catch {
    auditFallbackMode.value = true
    showAuditDialog.value = true
  } finally {
    auditing.value = false
  }
}

const auditClass = computed(() => {
  const r = auditResult.value?.overallResult
  if (r === 'PASS') return 'audit-pass'
  if (r === 'WARNING' || r === 'MANUAL') return 'audit-warn'
  if (r === 'REJECT') return 'audit-reject'
  return ''
})

const auditTagType = computed(() => {
  const r = auditResult.value?.overallResult
  if (r === 'PASS') return 'success'
  if (r === 'WARNING' || r === 'MANUAL') return 'warning'
  if (r === 'REJECT') return 'danger'
  return 'info'
})

function itemAuditTag(result: string) {
  if (result === 'PASS') return 'success'
  if (result === 'WARNING') return 'warning'
  if (result === 'REJECT') return 'danger'
  return 'info'
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

.drug-info-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 10px 12px;
  margin-top: 8px;
  background: var(--el-fill-color-lighter);
  border-radius: var(--cb-radius-sm);
  border: 1px solid var(--cb-border);
}

.usage-method-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  padding: 6px 12px;
  font-size: 13px;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  border-radius: var(--cb-radius-sm);
}

.precautions-hint {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  margin-top: 4px;
  padding: 6px 12px;
  font-size: 13px;
  color: var(--el-color-warning);
  background: var(--el-color-warning-light-9);
  border-radius: var(--cb-radius-sm);
}

.precautions-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1.6;
}

.precautions-sep {
  display: none;
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

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 8px;
}

.form-actions .el-button {
  min-width: 140px;
}

/* AI 审核结果 */
.audit-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.audit-overall {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: var(--cb-radius-md);
  background: var(--cb-background);
}

.audit-overall.audit-pass {
  border-left: 4px solid var(--cb-success);
}

.audit-overall.audit-warn {
  border-left: 4px solid var(--cb-warning);
}

.audit-overall.audit-reject {
  border-left: 4px solid var(--cb-danger);
}

.audit-label {
  font-weight: 600;
  color: var(--cb-text-primary);
}

.audit-score {
  color: var(--cb-text-secondary);
  font-size: var(--cb-font-sm);
  margin-left: auto;
}

.audit-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.audit-drug {
  background: var(--cb-background);
  border-radius: var(--cb-radius-md);
  padding: 12px 16px;
}

.audit-drug-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.drug-name {
  font-weight: 600;
  color: var(--cb-text-primary);
}

.audit-checks {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.audit-check-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: var(--cb-font-sm);
}

.check-detail {
  color: var(--cb-text-secondary);
  line-height: 1.5;
}

.audit-summary p {
  color: var(--cb-text-secondary);
  line-height: 1.6;
  margin: 0;
}

.audit-fallback {
  padding: 16px 0;
}
</style>
