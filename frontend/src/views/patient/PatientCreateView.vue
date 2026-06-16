<template>
  <div class="create-page">
    <div class="page-title">新建患者档案</div>

    <div v-loading="submitting" class="create-content">
      <el-row :gutter="24">
        <el-col :span="16">
          <div class="cb-card">
            <div class="cb-card-header">
              <el-icon class="header-icon"><Edit /></el-icon>
              <span>患者基本信息</span>
            </div>
            <div class="cb-card-body">
              <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="left">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="患者姓名" prop="name">
                      <el-input v-model="form.name" placeholder="请输入姓名" maxlength="50" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="身份证号" prop="idCard">
                      <el-input v-model="form.idCard" placeholder="请输入18位身份证号" maxlength="18" @blur="handleIdCardBlur" />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="性别" prop="gender">
                      <el-select v-model="form.gender" placeholder="请选择" style="width: 100%">
                        <el-option label="男" :value="1" />
                        <el-option label="女" :value="2" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="出生日期" prop="birthDate">
                      <el-date-picker v-model="form.birthDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="手机号码" prop="phone">
                      <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="紧急电话">
                      <el-input v-model="form.emergencyPhone" placeholder="选填" maxlength="20" />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-form-item label="家庭住址">
                  <el-input v-model="form.address" placeholder="选填" maxlength="255" />
                </el-form-item>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="血型">
                      <el-select v-model="form.bloodType" placeholder="选填" clearable style="width: 100%">
                        <el-option label="A型" value="A" />
                        <el-option label="B型" value="B" />
                        <el-option label="AB型" value="AB" />
                        <el-option label="O型" value="O" />
                        <el-option label="其他" value="其他" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="建档来源">
                      <el-select v-model="form.source" placeholder="选填" clearable style="width: 100%">
                        <el-option label="医院建档" :value="1" />
                        <el-option label="在线注册" :value="2" />
                        <el-option label="转诊" :value="3" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-form-item label="过敏史">
                  <el-input v-model="form.allergyHistory" type="textarea" :rows="2" placeholder="选填" maxlength="500" />
                </el-form-item>

                <el-form-item label="既往病史">
                  <el-input v-model="form.medicalHistory" type="textarea" :rows="2" placeholder="选填" maxlength="500" />
                </el-form-item>

                <el-divider />

                <div class="form-actions">
                  <el-button @click="handleReset">重置</el-button>
                  <el-button type="primary" @click="handleSubmit" :loading="submitting">创建档案</el-button>
                </div>
              </el-form>
            </div>
          </div>
        </el-col>

        <el-col :span="8">
          <div class="cb-card tips-card">
            <div class="cb-card-header">
              <el-icon class="header-icon"><InfoFilled /></el-icon>
              <span>填写说明</span>
            </div>
            <div class="cb-card-body">
              <ul class="tips-list">
                <li>标有 <span class="required-mark">*</span> 为必填项</li>
                <li>身份证号需为 18 位，提交后将校验唯一性</li>
                <li>系统将自动生成病历号</li>
                <li>手机号用于联系和通知</li>
              </ul>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit, InfoFilled } from '@element-plus/icons-vue'
import { createPatientApi, checkIdCardApi } from '@/api/patient'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive({
  name: '',
  idCard: '',
  gender: undefined as number | undefined,
  birthDate: '',
  phone: '',
  emergencyPhone: '',
  address: '',
  bloodType: '',
  source: 1,
  allergyHistory: '',
  medicalHistory: ''
})

const checkIdCard = async (_rule: any, value: string, callback: any) => {
  if (!value || value.length !== 18) {
    callback()
    return
  }
  try {
    const res = await checkIdCardApi(value)
    if ((res.data as any)?.exists) {
      callback(new Error('该身份证号已存在'))
    } else {
      callback()
    }
  } catch {
    callback()
  }
}

const rules = {
  name: [{ required: true, message: '请输入患者姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' },
    { validator: checkIdCard, trigger: 'blur' }
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  birthDate: [{ required: true, message: '请选择出生日期', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

function handleIdCardBlur() {
  const card = form.idCard
  if (card.length === 18) {
    const birthStr = card.substring(6, 14)
    const year = parseInt(birthStr.substring(0, 4))
    const month = parseInt(birthStr.substring(4, 6))
    const day = parseInt(birthStr.substring(6, 8))
    if (year > 1900 && year < new Date().getFullYear()) {
      form.birthDate = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    }
    const genderDigit = parseInt(card[16])
    form.gender = genderDigit % 2 === 1 ? 1 : 2
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const res = await createPatientApi(form as any)
    const result = res.data as { patientId: string; medicalRecordNo: string }
    ElMessage.success(`建档成功！病历号：${result.medicalRecordNo}`)
    router.push(`/patient/detail/${result.patientId}`)
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

function handleReset() {
  formRef.value?.resetFields()
}
</script>

<style scoped>
.create-page {
  max-width: 1100px;
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

.tips-list {
  padding-left: 20px;
  line-height: 2;
  color: var(--cb-text-secondary);
  font-size: var(--cb-font-sm);
}

.required-mark {
  color: var(--cb-danger);
  font-weight: 600;
}
</style>
