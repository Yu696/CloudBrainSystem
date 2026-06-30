<template>
  <div class="exam-result-page">
    <div class="page-title">
      <el-button text @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <span>检查结果填写</span>
    </div>

    <div v-loading="loading" class="result-content">
      <el-row :gutter="24">
        <el-col :span="16">
          <!-- 检查单信息 -->
          <div class="cb-card order-info-card">
            <div class="cb-card-header">
              <el-icon class="header-icon"><Document /></el-icon>
              <span>检查单信息</span>
            </div>
            <div class="cb-card-body" v-if="orderInfo">
              <el-descriptions :column="2" border size="default">
                <el-descriptions-item label="检查项目">{{ orderInfo.examName }}</el-descriptions-item>
                <el-descriptions-item label="检查类别">
                  <el-tag size="small" effect="plain" :type="categoryTagType(orderInfo.examCategory)">
                    {{ categoryText(orderInfo.examCategory) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="患者姓名">{{ orderInfo.patientName || '--' }}</el-descriptions-item>
                <el-descriptions-item label="检查目的">{{ orderInfo.examPurpose || '--' }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="statusTagType(orderInfo.status)" size="small">
                    {{ statusText(orderInfo.status) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="费用">&yen;{{ orderInfo.amount || 0 }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </div>

          <!-- 结果表单 -->
          <div class="cb-card result-form-card">
            <div class="cb-card-header">
              <el-icon class="header-icon"><EditPen /></el-icon>
              <span>检查结果</span>
              <el-tag v-if="resultExists" size="small" type="success" effect="plain" style="margin-left: 8px;">已填写</el-tag>
            </div>
            <div class="cb-card-body">
              <el-form label-position="top" size="default">
                <el-form-item label="检查所见 / 结果数据">
                  <el-input
                    v-model="form.resultData"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入检查所见或检查结果数据..."
                    maxlength="2000"
                    show-word-limit
                  />
                </el-form-item>
                <el-form-item label="参考范围">
                  <el-input
                    v-model="form.referenceRange"
                    placeholder="如：3.5-5.5 mmol/L"
                  />
                </el-form-item>
                <el-form-item label="是否异常">
                  <el-switch v-model="form.isAbnormal" />
                  <span class="switch-hint">{{ form.isAbnormal ? '异常' : '正常' }}</span>
                </el-form-item>
                <el-form-item label="医生诊断意见">
                  <el-input
                    v-model="form.doctorOpinion"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入诊断意见..."
                    maxlength="1000"
                    show-word-limit
                  />
                </el-form-item>
                <el-divider />
                <el-button
                  type="primary"
                  :loading="saving"
                  style="width: 100%"
                  @click="handleSave"
                >
                  {{ resultExists ? '更新结果' : '保存结果' }}
                </el-button>
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
                <li><strong>检查所见：</strong>详细描述检查过程中观察到的数据和现象</li>
                <li><strong>参考范围：</strong>该检查项目的正常值范围，用于判断异常</li>
                <li><strong>异常标记：</strong>根据结果数据与参考范围的对比进行判断</li>
                <li><strong>诊断意见：</strong>结合临床给出专业的诊断意见和建议</li>
                <li>保存后结果将同步至患者病历</li>
              </ul>
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
import { ArrowLeft, Document, InfoFilled, EditPen } from '@element-plus/icons-vue'
import { getExaminationDetailApi } from '@/api/medical'
import { getExaminationResultApi, saveExaminationResultApi } from '@/api/medical'

const router = useRouter()
const route = useRoute()
const orderId = route.params.orderId as string

const loading = ref(false)
const saving = ref(false)
const resultExists = ref(false)
const orderInfo = ref<any>(null)

const form = reactive({
  resultData: '',
  referenceRange: '',
  isAbnormal: false,
  doctorOpinion: ''
})

const categoryMap: Record<number, string> = {
  0: '实验室',
  1: '影像学',
  2: '功能检查'
}

function categoryText(cat: number): string {
  return categoryMap[cat] || '其他'
}

function categoryTagType(cat: number): '' | 'success' | 'info' | 'warning' | 'danger' {
  if (cat === 0) return 'success'
  if (cat === 1) return 'info'
  if (cat === 2) return 'warning'
  return ''
}

const statusMap: Record<number, string> = {
  0: '已开单',
  1: '已缴费',
  2: '检查中',
  3: '已完成',
  4: '已取消'
}

function statusText(status: number): string {
  return statusMap[status] || '未知'
}

function statusTagType(status: number): '' | 'warning' | 'success' | 'info' | 'danger' {
  if (status === 0) return 'warning'
  if (status === 1) return ''
  if (status === 2) return 'info'
  if (status === 3) return 'success'
  return 'info'
}

onMounted(async () => {
  loading.value = true
  try {
    // 加载检查单详情
    const detailRes = await getExaminationDetailApi(orderId)
    orderInfo.value = detailRes.data

    // 加载已有检查结果
    try {
      const res = await getExaminationResultApi(orderId)
      if (res.data) {
        resultExists.value = true
        form.resultData = res.data.resultData || ''
        form.referenceRange = res.data.referenceRange || ''
        form.isAbnormal = res.data.isAbnormal === 1
        form.doctorOpinion = res.data.doctorOpinion || ''
      }
    } catch {
      // 结果不存在，保持空表单
    }
  } catch {
    ElMessage.error('获取检查单信息失败')
  } finally {
    loading.value = false
  }
})

async function handleSave() {
  saving.value = true
  try {
    await saveExaminationResultApi({
      orderId,
      resultData: form.resultData,
      referenceRange: form.referenceRange,
      isAbnormal: form.isAbnormal ? 1 : 0,
      doctorOpinion: form.doctorOpinion
    })
    resultExists.value = true
    ElMessage.success('检查结果保存成功')
  } catch (err: any) {
    ElMessage.error('保存失败：' + (err?.message || '未知错误'))
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.exam-result-page {
  max-width: 1000px;
}

.header-icon {
  color: var(--cb-primary);
  font-size: 18px;
}

.order-info-card {
  margin-bottom: 20px;
}

.result-form-card {
  margin-bottom: 20px;
}

.switch-hint {
  margin-left: 10px;
  font-size: var(--cb-font-sm);
  color: var(--cb-text-secondary);
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

.tips-list li {
  margin-bottom: 6px;
}
</style>
