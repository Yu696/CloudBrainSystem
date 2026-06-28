<template>
  <div class="ai-tools-page">
    <div class="page-title">AI 辅助工具</div>
    <p class="page-desc">以下 AI 功能可在诊疗流程中使用，提升诊疗效率和质量</p>

    <el-row :gutter="20" class="tools-grid">
      <el-col :span="8" v-for="tool in tools" :key="tool.title">
        <el-card class="tool-card" shadow="hover">
          <div class="tool-icon">
            <el-icon :size="32"><component :is="tool.icon" /></el-icon>
          </div>
          <h3 class="tool-title">{{ tool.title }}</h3>
          <p class="tool-desc">{{ tool.desc }}</p>
          <el-button type="primary" plain @click="router.push(tool.route)">
            前往使用
          </el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { MagicStick, Document, FirstAidKit, Search } from '@element-plus/icons-vue'

const router = useRouter()

interface AiTool {
  title: string
  desc: string
  icon: any
  route: string
}

const tools: AiTool[] = [
  {
    title: 'AI 病历生成',
    desc: '在接诊时输入医患对话内容，AI 自动生成结构化病历（主诉、现病史、诊断等），核对后一键应用到病历表单。',
    icon: Document,
    route: '/doctor/waiting'
  },
  {
    title: 'AI 处方审核',
    desc: '开具处方时，AI 自动审核药品合理性，检测药物相互作用、禁忌人群和剂量问题，确保用药安全。',
    icon: FirstAidKit,
    route: '/doctor/waiting'
  },
  {
    title: 'AI 辅助诊断',
    desc: '基于患者症状、体征和病史，AI 给出诊断建议、疾病匹配和鉴别诊断分析。',
    icon: Search,
    route: '/patient/list'
  }
]
</script>

<style scoped>
.ai-tools-page {
  max-width: 1000px;
}

.page-desc {
  color: var(--cb-text-secondary);
  font-size: var(--cb-font-base);
  margin-bottom: 24px;
}

.tools-grid {
  display: flex;
  flex-wrap: wrap;
}

.tool-card {
  text-align: center;
  padding: 16px 8px;
  height: 100%;
}

.tool-icon {
  color: var(--cb-primary);
  margin-bottom: 12px;
}

.tool-title {
  font-size: var(--cb-font-lg);
  font-weight: 600;
  color: var(--cb-text-primary);
  margin-bottom: 8px;
}

.tool-desc {
  font-size: var(--cb-font-sm);
  color: var(--cb-text-secondary);
  line-height: 1.6;
  margin-bottom: 16px;
  min-height: 60px;
}
</style>
