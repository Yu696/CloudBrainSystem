<template>
  <div class="dept-page">
    <div class="page-title">选择科室</div>

    <!-- 科室分类导航 -->
    <el-tabs v-model="activeCategory" class="dept-tabs" @tab-change="handleCategoryChange">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane v-for="cat in categories" :key="cat" :label="cat" :name="cat" />
    </el-tabs>

    <!-- 科室卡片网格 -->
    <div v-loading="loading" class="dept-grid">
      <div
        v-for="dept in filteredDepartments"
        :key="dept.departmentId"
        class="dept-card"
        @click="selectDept(dept)"
      >
        <div class="dept-icon" :style="{ background: deptColors[dept.name] || deptColors.default }">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="1.5">
            <rect x="3" y="3" width="18" height="18" rx="3" />
            <path d="M12 8v8M8 12h8" />
          </svg>
        </div>
        <div class="dept-info">
          <div class="dept-name">{{ dept.name }}</div>
          <div class="dept-desc">{{ dept.description || '暂无介绍' }}</div>
        </div>
        <div class="dept-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <el-empty v-if="!loading && filteredDepartments.length === 0" description="暂无科室数据" :image-size="80" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import { listDepartmentsApi } from '@/api/appointment'

interface Department {
  departmentId: string
  name: string
  parentId: string
  category: string
  description: string
  location: string
  phone: string
  sortOrder: number
  status: number
}

const router = useRouter()

const departments = ref<Department[]>([])
const loading = ref(false)
const activeCategory = ref('')
const categories = ref<string[]>([])

const deptColors: Record<string, string> = {
  '心血管内科': 'linear-gradient(135deg, #e74c3c, #c0392b)',
  '呼吸内科': 'linear-gradient(135deg, #3498db, #2980b9)',
  '消化内科': 'linear-gradient(135deg, #2ecc71, #27ae60)',
  '神经内科': 'linear-gradient(135deg, #9b59b6, #8e44ad)',
  '内分泌科': 'linear-gradient(135deg, #f39c12, #e67e22)',
  '肾内科': 'linear-gradient(135deg, #1abc9c, #16a085)',
  '普通外科': 'linear-gradient(135deg, #e67e22, #d35400)',
  '骨科': 'linear-gradient(135deg, #2c3e50, #34495e)',
  '神经外科': 'linear-gradient(135deg, #8e44ad, #7d3c98)',
  '泌尿外科': 'linear-gradient(135deg, #2980b9, #1a5276)',
  '胸外科': 'linear-gradient(135deg, #a93226, #7b241c)',
  '妇科': 'linear-gradient(135deg, #e91e63, #c2185b)',
  '儿科': 'linear-gradient(135deg, #00bcd4, #0097a7)',
  '眼科': 'linear-gradient(135deg, #009688, #00796b)',
  '耳鼻喉科': 'linear-gradient(135deg, #ff5722, #e64a19)',
  '皮肤科': 'linear-gradient(135deg, #ff9800, #f57c00)',
  '口腔科': 'linear-gradient(135deg, #795548, #5d4037)',
  '中医科': 'linear-gradient(135deg, #4caf50, #388e3c)',
  '急诊科': 'linear-gradient(135deg, #f44336, #d32f2f)',
  'default': 'linear-gradient(135deg, var(--cb-primary), var(--cb-primary-dark))'
}

const filteredDepartments = computed(() => {
  if (!activeCategory.value) return departments.value
  return departments.value.filter(d => d.category === activeCategory.value)
})

onMounted(async () => {
  await loadDepartments()
})

async function loadDepartments() {
  loading.value = true
  try {
    const res = await listDepartmentsApi()
    const data = res.data as Department[]
    departments.value = data || []
    categories.value = [...new Set(data.map(d => d.category).filter(Boolean))] as string[]
  } catch {
    departments.value = []
  } finally {
    loading.value = false
  }
}

function handleCategoryChange() {
  // tab change triggers computed
}

function selectDept(dept: Department) {
  router.push({
    path: '/appointment/doctor',
    query: { deptId: dept.departmentId, deptName: dept.name }
  })
}
</script>

<style scoped>
.dept-page {
  max-width: 1100px;
}

.dept-tabs {
  margin-bottom: 20px;
}

.dept-tabs :deep(.el-tabs__item) {
  font-size: var(--cb-font-base);
  padding: 0 20px;
}

.dept-tabs :deep(.el-tabs__active-bar) {
  background: var(--cb-primary);
}

.dept-tabs :deep(.el-tabs__item.is-active) {
  color: var(--cb-primary);
}

.dept-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.dept-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--cb-white);
  border-radius: var(--cb-radius-lg);
  box-shadow: var(--cb-shadow-sm);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.dept-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--cb-shadow-lg);
  border-color: var(--cb-primary-lighter);
}

.dept-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.dept-info {
  flex: 1;
  min-width: 0;
}

.dept-name {
  font-size: var(--cb-font-lg);
  font-weight: 600;
  color: var(--cb-text-primary);
  margin-bottom: 4px;
}

.dept-desc {
  font-size: var(--cb-font-xs);
  color: var(--cb-text-placeholder);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dept-arrow {
  color: var(--cb-text-placeholder);
  flex-shrink: 0;
  transition: transform 0.2s;
}

.dept-card:hover .dept-arrow {
  transform: translateX(4px);
  color: var(--cb-primary);
}
</style>
