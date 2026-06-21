<template>
  <div class="detail-page">
    <div class="page-header">
      <el-button text @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>返回
      </el-button>
      <div class="page-title">患者档案详情</div>
    </div>

    <div v-loading="loading" class="detail-content">
      <!-- 患者基本信息卡片 -->
      <el-row :gutter="24">
        <el-col :span="16">
          <div class="cb-card">
            <div class="cb-card-header">
              <el-icon class="header-icon"><User /></el-icon>
              <span>基本信息</span>
              <div class="header-actions">
                <el-tag v-if="!editing" :type="patientInfo.status === 1 ? 'success' : 'info'" size="small">
                  {{ patientInfo.status === 1 ? '正常' : '归档' }}
                </el-tag>
                <el-button v-if="!editing" type="primary" link size="small" @click="enterEdit">
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
              </div>
            </div>
            <div class="cb-card-body">
              <!-- 查看模式 -->
              <template v-if="!editing">
                <div class="info-grid">
                  <div class="info-item">
                    <span class="info-label">病历号</span>
                    <span class="info-value mono">{{ patientInfo.medicalRecordNo }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">患者编号</span>
                    <span class="info-value mono">{{ patientInfo.patientId }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">姓名</span>
                    <span class="info-value">{{ patientInfo.name }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">性别</span>
                    <span class="info-value">{{ patientInfo.gender === 1 ? '男' : patientInfo.gender === 2 ? '女' : '未知' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">出生日期</span>
                    <span class="info-value">{{ patientInfo.birthDate }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">年龄</span>
                    <span class="info-value">{{ patientInfo.age ?? '-' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">身份证号</span>
                    <span class="info-value">{{ patientInfo.idCard }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">手机号码</span>
                    <span class="info-value">{{ patientInfo.phone }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">紧急电话</span>
                    <span class="info-value">{{ patientInfo.emergencyPhone || '-' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">血型</span>
                    <span class="info-value">{{ patientInfo.bloodType ? patientInfo.bloodType + '型' : '-' }}</span>
                  </div>
                  <div class="info-item" style="grid-column: 1 / -1;">
                    <span class="info-label">家庭住址</span>
                    <span class="info-value">{{ patientInfo.address || '-' }}</span>
                  </div>
                  <div class="info-item" style="grid-column: 1 / -1;">
                    <span class="info-label">建档来源</span>
                    <span class="info-value">{{ patientInfo.source === 1 ? '医院建档' : patientInfo.source === 2 ? '在线注册' : patientInfo.source === 3 ? '转诊' : '未知' }}</span>
                  </div>
                </div>
              </template>

              <!-- 编辑模式 -->
              <template v-else>
                <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px" label-position="left">
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="姓名" prop="name">
                        <el-input v-model="editForm.name" maxlength="50" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="性别" prop="gender">
                        <el-select v-model="editForm.gender" style="width: 100%">
                          <el-option label="男" :value="1" />
                          <el-option label="女" :value="2" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="出生日期" prop="birthDate">
                        <el-date-picker v-model="editForm.birthDate" type="date" style="width: 100%" value-format="YYYY-MM-DD" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="手机号" prop="phone">
                        <el-input v-model="editForm.phone" maxlength="11" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="紧急电话">
                        <el-input v-model="editForm.emergencyPhone" maxlength="20" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="血型">
                        <el-select v-model="editForm.bloodType" clearable style="width: 100%">
                          <el-option label="A型" value="A" />
                          <el-option label="B型" value="B" />
                          <el-option label="AB型" value="AB" />
                          <el-option label="O型" value="O" />
                          <el-option label="其他" value="其他" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-form-item label="家庭住址">
                    <el-input v-model="editForm.address" maxlength="255" />
                  </el-form-item>
                  <el-form-item label="过敏史">
                    <el-input v-model="editForm.allergyHistory" type="textarea" :rows="2" maxlength="500" />
                  </el-form-item>
                  <el-form-item label="既往病史">
                    <el-input v-model="editForm.medicalHistory" type="textarea" :rows="2" maxlength="500" />
                  </el-form-item>

                  <el-divider />

                  <div class="form-actions">
                    <el-button @click="cancelEdit">取消</el-button>
                    <el-button type="primary" @click="handleSave" :loading="saving">保存修改</el-button>
                  </div>
                </el-form>
              </template>
            </div>
          </div>
        </el-col>

        <!-- 右侧信息卡 -->
        <el-col :span="8">
          <div class="cb-card info-card">
            <div class="cb-card-header">
              <el-icon class="header-icon"><Clock /></el-icon>
              <span>档案时间轴</span>
            </div>
            <div class="cb-card-body">
              <div class="timeline-item">
                <span class="timeline-label">建档时间</span>
                <span class="timeline-value">{{ patientInfo.createTime || '-' }}</span>
              </div>
              <div class="timeline-item">
                <span class="timeline-label">最后更新</span>
                <span class="timeline-value">{{ patientInfo.updateTime || '-' }}</span>
              </div>
            </div>
          </div>

          <div class="cb-card info-card" style="margin-top: 16px;">
            <div class="cb-card-header">
              <el-icon class="header-icon"><WarningFilled /></el-icon>
              <span>医疗信息</span>
            </div>
            <div class="cb-card-body">
              <div class="med-section">
                <div class="med-label">过敏史</div>
                <div class="med-value">{{ patientInfo.allergyHistory || '无记录' }}</div>
              </div>
              <el-divider style="margin: 12px 0" />
              <div class="med-section">
                <div class="med-label">遗传病史</div>
                <div class="med-value">{{ patientInfo.geneticDiseases || '无记录' }}</div>
              </div>
              <el-divider style="margin: 12px 0" />
              <div class="med-section">
                <div class="med-label">既往病史</div>
                <div class="med-value">{{ patientInfo.medicalHistory || '无记录' }}</div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, User, Edit, Clock, WarningFilled } from '@element-plus/icons-vue'
import { getPatientInfoApi, updatePatientApi } from '@/api/patient'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const route = useRoute()
const editFormRef = ref<FormInstance>()

const patientId = route.params.id as string
const patientInfo = reactive<any>({})
const loading = ref(false)
const editing = ref(false)
const saving = ref(false)

const editForm = reactive({
  name: '',
  gender: undefined as number | undefined,
  birthDate: '',
  phone: '',
  emergencyPhone: '',
  address: '',
  bloodType: '',
  allergyHistory: '',
  medicalHistory: ''
})

const editRules = {
  name: [{ required: true, message: '请输入患者姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  birthDate: [{ required: true, message: '请选择出生日期', trigger: 'change' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

onMounted(async () => {
  await loadPatient()
})

async function loadPatient() {
  if (!patientId) return

  loading.value = true
  try {
    const res = await getPatientInfoApi(patientId)
    Object.assign(patientInfo, res.data || {})
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function enterEdit() {
  editForm.name = patientInfo.name
  editForm.gender = patientInfo.gender
  editForm.birthDate = patientInfo.birthDate
  editForm.phone = patientInfo.phone
  editForm.emergencyPhone = patientInfo.emergencyPhone || ''
  editForm.address = patientInfo.address || ''
  editForm.bloodType = patientInfo.bloodType || ''
  editForm.allergyHistory = patientInfo.allergyHistory || ''
  editForm.medicalHistory = patientInfo.medicalHistory || ''
  editing.value = true
}

function cancelEdit() {
  editing.value = false
}

async function handleSave() {
  if (!editFormRef.value) return
  try {
    await editFormRef.value.validate()
  } catch {
    return
  }

  saving.value = true
  try {
    await updatePatientApi({
      patientId,
      ...editForm
    })
    ElMessage.success('保存成功')
    editing.value = false
    await loadPatient()
  } catch {
    // handled by interceptor
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.detail-page {
  max-width: 1100px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.page-header .page-title {
  margin-bottom: 0;
}

.header-icon {
  color: var(--cb-primary);
  font-size: 18px;
}

.header-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: var(--cb-font-xs);
  color: var(--cb-text-placeholder);
}

.info-value {
  font-size: var(--cb-font-base);
  color: var(--cb-text-primary);
}

.mono {
  font-family: monospace;
  color: var(--cb-primary);
  font-weight: 500;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 8px;
}

.info-card {
  position: sticky;
  top: 24px;
}

.timeline-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px 0;
}

.timeline-label {
  font-size: var(--cb-font-xs);
  color: var(--cb-text-placeholder);
}

.timeline-value {
  font-size: var(--cb-font-sm);
  color: var(--cb-text-primary);
}

.med-section {
  padding: 4px 0;
}

.med-label {
  font-size: var(--cb-font-xs);
  color: var(--cb-text-placeholder);
  margin-bottom: 4px;
}

.med-value {
  font-size: var(--cb-font-sm);
  color: var(--cb-text-primary);
  line-height: 1.6;
}
</style>
