<template>
  <div class="list-page">
    <div class="page-title">影像列表</div>

    <!-- 搜索栏 -->
    <div class="cb-card search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="患者ID">
          <el-input v-model="searchForm.patientId" placeholder="患者ID" clearable style="width: 160px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="检查ID">
          <el-input v-model="searchForm.examinationId" placeholder="检查ID" clearable style="width: 160px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="设备类型">
          <el-select v-model="searchForm.modality" placeholder="全部" clearable style="width: 140px" @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="CT" value="CT" />
            <el-option label="MRI" value="MRI" />
            <el-option label="X光" value="X光" />
            <el-option label="超声" value="超声" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格 -->
    <div class="cb-card">
      <el-table :data="images" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="imageName" label="影像文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="imageType" label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.imageType === 'DICOM'" type="primary" size="small">DICOM</el-tag>
            <el-tag v-else-if="row.imageType === 'JPG'" type="success" size="small">JPG</el-tag>
            <el-tag v-else-if="row.imageType === 'PNG'" type="warning" size="small">PNG</el-tag>
            <el-tag v-else size="small">{{ row.imageType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="modality" label="成像设备" width="110" align="center" />
        <el-table-column prop="bodyPart" label="身体部位" width="120" align="center" />
        <el-table-column prop="fileSize" label="文件大小" width="120" align="right">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="uploadTime" label="上传时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click.stop="handlePreview(row)">
              预览
            </el-button>
            <el-button type="danger" link size="small" @click.stop="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="loadImages"
          @size-change="onPageSizeChange"
        />
      </div>

      <el-empty v-if="!loading && images.length === 0" description="暂无影像数据" :image-size="80" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { imageListApi, deleteImageApi } from '@/api/image'

const router = useRouter()

const images = ref<any[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  patientId: '',
  examinationId: '',
  modality: ''
})

onMounted(() => {
  loadImages()
})

async function loadImages() {
  loading.value = true
  try {
    const params: any = {
      page: page.value,
      pageSize: pageSize.value
    }
    if (searchForm.patientId) params.patientId = searchForm.patientId
    if (searchForm.examinationId) params.examinationId = searchForm.examinationId
    if (searchForm.modality) params.modality = searchForm.modality

    const res = await imageListApi(params)
    const data = res.data || {}
    images.value = (data.records || data.list || data) as any[]
    total.value = data.total || images.value.length
  } catch {
    images.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  loadImages()
}

function handleReset() {
  searchForm.patientId = ''
  searchForm.examinationId = ''
  searchForm.modality = ''
  page.value = 1
  loadImages()
}

function onPageSizeChange() {
  page.value = 1
  loadImages()
}

function handlePreview(row: any) {
  router.push(`/image/viewer/${row.imageId}`)
}

function handleDelete(row: any) {
  ElMessageBox.confirm(`确定要删除影像「${row.imageName}」吗？此操作不可撤销。`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteImageApi(row.imageId)
        ElMessage.success('删除成功')
        loadImages()
      } catch {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {
      // 取消删除，不做处理
    })
}

function formatFileSize(size: number | string | null | undefined): string {
  if (size === null || size === undefined || size === '') return '-'
  const bytes = Number(size)
  if (isNaN(bytes) || bytes < 0) return '-'
  if (bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let unitIndex = 0
  let value = bytes
  while (value >= 1024 && unitIndex < units.length - 1) {
    value /= 1024
    unitIndex++
  }
  return `${unitIndex === 0 ? value : value.toFixed(2)} ${units[unitIndex]}`
}
</script>

<style scoped>
.search-card {
  margin-bottom: 16px;
}

.search-card .el-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.search-card .el-form-item {
  margin-bottom: 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-bottom: 4px;
}
</style>
