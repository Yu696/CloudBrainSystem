<template>
  <div class="dispense-page">
    <div class="page-title">
      <el-button text @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      发药管理
    </div>

    <!-- 发药表单 -->
    <div v-if="!dispenseResult" v-loading="submitting" class="form-content">
      <div class="cb-card">
        <div class="cb-card-header">
          <el-icon class="header-icon"><Goods /></el-icon>
          <span>发药出库</span>
        </div>
        <div class="cb-card-body">
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="120px"
            label-position="left"
          >
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="处方 ID" prop="prescriptionId">
                  <el-input v-model="form.prescriptionId" placeholder="请输入处方 ID" maxlength="50" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="患者 ID" prop="patientId">
                  <el-input v-model="form.patientId" placeholder="请输入患者 ID" maxlength="50" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider />

            <div class="section-title">
              <el-icon><List /></el-icon>
              <span>发药明细</span>
            </div>

            <div
              v-for="(item, index) in form.items"
              :key="index"
              class="dispense-item-row"
            >
              <el-row :gutter="16" align="middle">
                <el-col :span="8">
                  <el-form-item
                    :label="`药品 ID`"
                    :prop="`items.${index}.drugId`"
                    :rules="[{ required: true, message: '请输入药品 ID', trigger: 'blur' }]"
                    :label-width="index === 0 ? '80px' : '0'"
                  >
                    <el-input
                      v-model="item.drugId"
                      placeholder="请输入药品 ID"
                      maxlength="50"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item
                    :label="`数量`"
                    :prop="`items.${index}.quantity`"
                    :rules="[
                      { required: true, message: '请输入数量', trigger: 'blur' },
                      { type: 'number', min: 1, message: '数量必须 ≥ 1', trigger: 'blur' }
                    ]"
                    :label-width="index === 0 ? '60px' : '0'"
                  >
                    <el-input-number
                      v-model="item.quantity"
                      :min="1"
                      controls-position="right"
                      placeholder="数量"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="8" class="item-actions">
                  <el-button
                    type="danger"
                    size="small"
                    :disabled="form.items.length <= 1"
                    @click="removeItem(index)"
                  >
                    删除
                  </el-button>
                </el-col>
              </el-row>
            </div>

            <div class="add-item-row">
              <el-button type="primary" plain @click="addItem">
                <el-icon><Plus /></el-icon>
                添加一行
              </el-button>
            </div>

            <el-divider />

            <div class="form-actions">
              <el-button type="primary" @click="handleSubmit" :loading="submitting">
                提交发药
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 发药结果 -->
    <div v-else class="result-content">
      <div class="cb-card">
        <div class="cb-card-header">
          <el-icon class="header-icon"><SuccessFilled /></el-icon>
          <span>发药成功</span>
        </div>
        <div class="cb-card-body">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="发药记录 ID" label-class-name="desc-label">
              {{ dispenseResult.recordId }}
            </el-descriptions-item>
            <el-descriptions-item label="处方 ID" label-class-name="desc-label">
              {{ dispenseResult.prescriptionId }}
            </el-descriptions-item>
            <el-descriptions-item label="出药总件数" label-class-name="desc-label">
              {{ dispenseResult.shipNum }}
            </el-descriptions-item>
            <el-descriptions-item label="支付金额" label-class-name="desc-label">
              <span class="amount">￥{{ dispenseResult.payAmount }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="出药时间" label-class-name="desc-label" :span="2">
              {{ dispenseResult.shipTime }}
            </el-descriptions-item>
          </el-descriptions>

          <div class="result-actions">
            <el-button type="success" @click="handlePrint" :loading="printing">
              打印取药单
            </el-button>
            <el-button @click="handleReset">重新发药</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Goods, List, Plus, SuccessFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { dispenseDrugApi, printShipRecordApi } from '@/api/pharmacy'

interface DispenseItem {
  drugId: string
  quantity: number
}

interface DispenseForm {
  prescriptionId: string
  patientId: string
  items: DispenseItem[]
}

interface DispenseResultData {
  recordId: string
  prescriptionId: string
  shipNum: number
  payAmount: number
  shipTime: string
}

const router = useRouter()

const submitting = ref(false)
const printing = ref(false)
const formRef = ref<FormInstance>()
const dispenseResult = ref<DispenseResultData | null>(null)

const form = reactive<DispenseForm>({
  prescriptionId: '',
  patientId: '',
  items: [{ drugId: '', quantity: 1 }]
})

const rules: FormRules<DispenseForm> = {
  prescriptionId: [{ required: true, message: '请输入处方 ID', trigger: 'blur' }],
  patientId: [{ required: true, message: '请输入患者 ID', trigger: 'blur' }]
}

function addItem() {
  form.items.push({ drugId: '', quantity: 1 })
}

function removeItem(index: number) {
  if (form.items.length <= 1) return
  form.items.splice(index, 1)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const res = await dispenseDrugApi({
      prescriptionId: form.prescriptionId,
      patientId: form.patientId,
      items: form.items
    })
    const data = res.data as DispenseResultData
    dispenseResult.value = data
    ElMessage.success('发药成功')
  } catch {
    ElMessage.error('发药失败')
  } finally {
    submitting.value = false
  }
}

async function handlePrint() {
  if (!dispenseResult.value) return
  printing.value = true
  try {
    await printShipRecordApi(dispenseResult.value.recordId)
    ElMessage.success('打印成功')
  } catch {
    ElMessage.error('打印失败')
  } finally {
    printing.value = false
  }
}

function handleReset() {
  dispenseResult.value = null
  form.prescriptionId = ''
  form.patientId = ''
  form.items = [{ drugId: '', quantity: 1 }]
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}
</script>

<style scoped>
.dispense-page {
  max-width: 1000px;
}

.form-content {
  margin-top: 16px;
}

.result-content {
  margin-top: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 16px;
}

.dispense-item-row {
  padding: 12px 0;
  border-bottom: 1px solid var(--el-border-color-light);
}

.dispense-item-row:last-child {
  border-bottom: none;
}

.item-actions {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding-top: 0;
}

.add-item-row {
  margin-top: 12px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
}

.amount {
  color: var(--el-color-danger);
  font-weight: 600;
}

:deep(.desc-label) {
  font-weight: 600;
  width: 120px;
}
</style>
