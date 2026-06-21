<template>
  <div class="exam-result-page">
    <div class="page-title">
      <el-button text @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      检查报告
    </div>

    <div v-loading="loading" class="result-content">
      <el-row :gutter="24">
        <el-col :span="16">
          <div class="cb-card">
            <div class="cb-card-header">
              <el-icon class="header-icon"><Document /></el-icon>
              <span>检查结果详情</span>
            </div>
            <div class="cb-card-body" v-if="result">
              <div class="result-section">
                <div class="section-title">基本信息</div>
                <el-descriptions :column="2" border size="default">
                  <el-descriptions-item label="报告编号">{{ result.resultId }}</el-descriptions-item>
                  <el-descriptions-item label="检查单编号">{{ result.orderId }}</el-descriptions-item>
                  <el-descriptions-item label="检查时间">{{ result.resultTime || '--' }}</el-descriptions-item>
                  <el-descriptions-item label="异常标记">
                    <el-tag :type="result.isAbnormal ? 'danger' : 'success'" size="small">
                      {{ result.isAbnormal ? '异常' : '正常' }}
                    </el-tag>
                  </el-descriptions-item>
                </el-descriptions>
              </div>

              <el-divider />

              <div class="result-section">
                <div class="section-title">检查数据</div>
                <div class="result-data-container">
                  <pre class="result-data">{{ formatResultData(result.resultData) }}</pre>
                </div>
              </div>

              <div v-if="result.referenceRange" class="result-section">
                <div class="section-title">参考范围</div>
                <p class="result-text">{{ result.referenceRange }}</p>
              </div>

              <div v-if="result.aiAnalysis" class="result-section">
                <div class="section-title">
                  <el-icon class="ai-icon"><Cpu /></el-icon>
                  AI 智能分析
                </div>
                <el-alert type="info" :closable="false">
                  <p class="result-text">{{ result.aiAnalysis }}</p>
                </el-alert>
              </div>

              <div v-if="result.doctorOpinion" class="result-section">
                <div class="section-title">医生诊断意见</div>
                <p class="result-text">{{ result.doctorOpinion }}</p>
              </div>

              <div v-if="result.reportFileUrl" class="result-section">
                <div class="section-title">附件报告</div>
                <el-link type="primary" :href="result.reportFileUrl" target="_blank">
                  <el-icon><Link /></el-icon>查看报告文件
                </el-link>
              </div>
            </div>
            <div v-else-if="!loading" class="cb-card-body">
              <el-empty description="暂无检查结果" :image-size="80" />
            </div>
          </div>
        </el-col>

        <el-col :span="8">
          <div class="cb-card tips-card">
            <div class="cb-card-header">
              <el-icon class="header-icon"><InfoFilled /></el-icon>
              <span>操作提示</span>
            </div>
            <div class="cb-card-body">
              <ul class="tips-list">
                <li>AI分析仅供参考，请结合临床判断</li>
                <li>异常指标已标红提示</li>
                <li>可在诊断意见中补充医生判断</li>
              </ul>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Document, InfoFilled, Cpu, Link } from '@element-plus/icons-vue'
import { getExaminationResultApi } from '@/api/medical'

const router = useRouter()
const route = useRoute()
const orderId = route.params.orderId as string

const loading = ref(false)
const result = ref<any>(null)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getExaminationResultApi(orderId)
    result.value = res.data
  } catch {
    result.value = null
  } finally {
    loading.value = false
  }
})

function formatResultData(data: string): string {
  if (!data) return '暂无数据'
  try {
    const parsed = JSON.parse(data)
    return JSON.stringify(parsed, null, 2)
  } catch {
    return data
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

.result-section {
  margin-bottom: 16px;
}

.section-title {
  font-weight: 600;
  color: var(--cb-text-primary);
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--cb-font-base);
}

.ai-icon {
  color: var(--cb-primary);
}

.result-data-container {
  background: var(--cb-background);
  border-radius: var(--cb-radius-md);
  padding: 16px;
  max-height: 300px;
  overflow-y: auto;
}

.result-data {
  margin: 0;
  font-size: var(--cb-font-sm);
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-all;
  color: var(--cb-text-primary);
}

.result-text {
  margin: 0;
  color: var(--cb-text-secondary);
  font-size: var(--cb-font-sm);
  line-height: 1.8;
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
</style>
