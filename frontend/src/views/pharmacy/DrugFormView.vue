<template>
  <div class="drug-form-page">
    <div class="page-title">
      <el-button text @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      {{ isNew ? '新增药品' : '编辑药品' }}
    </div>

    <div v-loading="pageLoading" class="form-content">
      <div class="cb-card">
        <div class="cb-card-header">
          <el-icon class="header-icon"><Goods /></el-icon>
          <span>药品信息</span>
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
                <el-form-item label="药品编码" prop="drugCode">
                  <el-input v-model="form.drugCode" placeholder="请输入药品编码" maxlength="50" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="药品名称" prop="drugName">
                  <el-input v-model="form.drugName" placeholder="请输入药品名称" maxlength="100" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="通用名" prop="genericName">
                  <el-input v-model="form.genericName" placeholder="请输入通用名" maxlength="100" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="主要成分" prop="ingredients">
                  <el-input v-model="form.ingredients" placeholder="请输入主要成分" maxlength="200" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="规格" prop="spec">
                  <el-input v-model="form.spec" placeholder="如 0.25g×12片" maxlength="50" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="剂型" prop="dosageForm">
                  <el-select v-model="form.dosageForm" placeholder="请选择剂型" clearable style="width: 100%">
                    <el-option label="片剂" value="片剂" />
                    <el-option label="胶囊" value="胶囊" />
                    <el-option label="注射液" value="注射液" />
                    <el-option label="口服液" value="口服液" />
                    <el-option label="颗粒剂" value="颗粒剂" />
                    <el-option label="软膏" value="软膏" />
                    <el-option label="滴眼液" value="滴眼液" />
                    <el-option label="气雾剂" value="气雾剂" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="生产厂家" prop="manufacturer">
                  <el-input v-model="form.manufacturer" placeholder="请输入生产厂家" maxlength="200" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="单位" prop="unit">
                  <el-select v-model="form.unit" placeholder="请选择单位" style="width: 100%">
                    <el-option label="盒" value="盒" />
                    <el-option label="瓶" value="瓶" />
                    <el-option label="支" value="支" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="单价" prop="unitPrice">
                  <el-input-number
                    v-model="form.unitPrice"
                    :min="0.01"
                    :precision="2"
                    :step="0.5"
                    controls-position="right"
                    placeholder="请输入单价"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="进价" prop="purchasePrice">
                  <el-input-number
                    v-model="form.purchasePrice"
                    :min="0"
                    :precision="2"
                    :step="0.5"
                    controls-position="right"
                    placeholder="请输入进价（选填）"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="药品分类" prop="drugCategory">
                  <el-select v-model="form.drugCategory" placeholder="请选择分类" clearable style="width: 100%">
                    <el-option label="抗生素" value="抗生素" />
                    <el-option label="解热镇痛药" value="解热镇痛药" />
                    <el-option label="心血管药物" value="心血管药物" />
                    <el-option label="维生素类" value="维生素类" />
                    <el-option label="中成药" value="中成药" />
                    <el-option label="生物制剂" value="生物制剂" />
                    <el-option label="其他" value="其他" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="处方类型" prop="prescriptionType">
                  <el-radio-group v-model="form.prescriptionType">
                    <el-radio :value="0">OTC</el-radio>
                    <el-radio :value="1">处方药</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="24">
              <el-col :span="24">
                <el-form-item label="用法" prop="usageMethod">
                  <el-input v-model="form.usageMethod" placeholder="如 口服，每次1片，每日3次" maxlength="200" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="禁忌人群" prop="cautiousCrowd">
                  <el-input
                    v-model="form.cautiousCrowd"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入禁忌人群说明"
                    maxlength="500"
                    show-word-limit
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="副作用" prop="sideEffects">
                  <el-input
                    v-model="form.sideEffects"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入副作用说明"
                    maxlength="500"
                    show-word-limit
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider />

            <div class="form-actions">
              <el-button @click="handleCancel">取消</el-button>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">
                {{ isNew ? '新增' : '保存' }}
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { addDrugApi, updateDrugApi, getDrugDetailApi } from '@/api/pharmacy'

const route = useRoute()
const router = useRouter()

const drugId = computed(() => route.params.drugId as string)
const isNew = computed(() => drugId.value === 'new')

const pageLoading = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

interface DrugForm {
  drugCode: string
  drugName: string
  genericName: string
  ingredients: string
  spec: string
  dosageForm: string
  manufacturer: string
  unit: string
  unitPrice: number | undefined
  purchasePrice: number | undefined
  usageMethod: string
  cautiousCrowd: string
  sideEffects: string
  drugCategory: string
  prescriptionType: number | undefined
}

const form = reactive<DrugForm>({
  drugCode: '',
  drugName: '',
  genericName: '',
  ingredients: '',
  spec: '',
  dosageForm: '',
  manufacturer: '',
  unit: '',
  unitPrice: undefined,
  purchasePrice: undefined,
  usageMethod: '',
  cautiousCrowd: '',
  sideEffects: '',
  drugCategory: '',
  prescriptionType: undefined
})

const rules: FormRules<DrugForm> = {
  drugName: [{ required: true, message: '请输入药品名称', trigger: 'blur' }],
  unit: [{ required: true, message: '请选择单位', trigger: 'change' }],
  unitPrice: [
    { required: true, message: '请输入单价', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '单价必须大于 0', trigger: 'blur' }
  ],
  prescriptionType: [{ required: true, message: '请选择处方类型', trigger: 'change' }]
}

onMounted(async () => {
  if (!isNew.value) {
    pageLoading.value = true
    try {
      const res = await getDrugDetailApi(drugId.value)
      const item = res.data as any
      if (item) {
        form.drugCode = item.drugCode ?? ''
        form.drugName = item.drugName ?? ''
        form.genericName = item.genericName ?? ''
        form.ingredients = item.ingredients ?? ''
        form.spec = item.spec ?? ''
        form.dosageForm = item.dosageForm ?? ''
        form.manufacturer = item.manufacturer ?? ''
        form.unit = item.unit ?? ''
        form.unitPrice = item.unitPrice ?? undefined
        form.purchasePrice = item.purchasePrice ?? undefined
        form.usageMethod = item.usageMethod ?? ''
        form.cautiousCrowd = item.cautiousCrowd ?? ''
        form.sideEffects = item.sideEffects ?? ''
        form.drugCategory = item.drugCategory ?? ''
        form.prescriptionType = item.prescriptionType ?? undefined
      } else {
        ElMessage.warning('未找到该药品信息')
      }
    } catch {
      ElMessage.error('获取药品信息失败')
    } finally {
      pageLoading.value = false
    }
  }
})

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = { ...form }
    if (isNew.value) {
      await addDrugApi(payload)
      ElMessage.success('新增药品成功')
    } else {
      await updateDrugApi({ ...payload, drugId: drugId.value })
      ElMessage.success('修改药品成功')
    }
    router.push('/pharmacy/drugs')
  } catch {
    ElMessage.error(isNew.value ? '新增药品失败' : '修改药品失败')
  } finally {
    submitting.value = false
  }
}

function handleCancel() {
  router.push('/pharmacy/drugs')
}
</script>

<style scoped>
.drug-form-page {
  max-width: 1000px;
}

.form-content {
  margin-top: 16px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style>
