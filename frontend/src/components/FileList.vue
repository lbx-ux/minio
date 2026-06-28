<template>
  <el-card shadow="hover" class="file-list-card">
    <template #header>
      <div class="card-header">
        <el-icon :size="20"><List /></el-icon>
        <span>文件列表</span>
        <el-button
          size="small"
          type="primary"
          :icon="Refresh"
          :loading="loading"
          @click="fetchList"
        >
          刷新
        </el-button>
        <el-tag v-if="files.length" type="info" size="small" class="count-tag">
          共 {{ files.length }} 个文件
        </el-tag>
      </div>
    </template>

    <!-- 空状态 -->
    <el-empty
      v-if="!loading && files.length === 0"
      description="暂无文件，请先上传"
      :image-size="120"
    />

    <!-- 加载中 -->
    <div v-if="loading" class="loading-area">
      <el-skeleton :rows="4" animated />
    </div>

    <!-- 文件表格 -->
    <el-table
      v-if="!loading && files.length > 0"
      :data="files"
      stripe
      style="width: 100%"
    >
      <el-table-column prop="name" label="文件名" min-width="280" show-overflow-tooltip />
      <el-table-column label="大小" width="130" align="right">
        <template #default="{ row }">
          {{ formatSize(row.size) }}
        </template>
      </el-table-column>
      <el-table-column label="最后修改时间" width="200" align="center">
        <template #default="{ row }">
          <span class="time-cell">{{ formatTime(row.lastModified) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            size="small"
            :icon="Link"
            link
            @click="handlePreview(row.name)"
          >
            预览
          </el-button>
          <el-button
            type="success"
            size="small"
            :icon="CopyDocument"
            link
            @click="handleCopy(row.name)"
          >
            复制标识
          </el-button>
          <el-popconfirm
            title="确定要删除该文件吗？"
            confirm-button-text="删除"
            cancel-button-text="取消"
            @confirm="handleDelete(row.name)"
          >
            <template #reference>
              <el-button type="danger" size="small" :icon="Delete" link>
                删除
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" title="文件预览" width="800px" destroy-on-close>
      <div v-if="previewLoading" class="preview-loading">
        <el-skeleton :rows="3" animated />
      </div>
      <div v-else-if="previewUrl" class="preview-body">
        <el-descriptions :column="2" border size="small" class="preview-meta">
          <el-descriptions-item label="文件名">{{ previewObjectName }}</el-descriptions-item>
          <el-descriptions-item label="有效期">10 分钟</el-descriptions-item>
        </el-descriptions>
        <div class="preview-link-row">
          <el-input v-model="previewUrl" readonly size="small" />
          <el-button type="primary" size="small" :icon="CopyDocument" @click="copyPreviewUrl">
            复制
          </el-button>
        </div>
        <!-- 内联预览 -->
        <div v-if="isImageFile" class="inline-preview-box">
          <el-image
            :src="previewUrl"
            fit="contain"
            style="max-width:100%;max-height:400px;border-radius:8px"
            :preview-src-list="[previewUrl]"
            preview-teleported
          />
        </div>
        <div v-else-if="isPdfFile" class="inline-preview-box">
          <iframe :src="previewUrl" class="pdf-frame" />
        </div>
        <div v-else-if="isVideoFile" class="inline-preview-box">
          <video :src="previewUrl" controls style="max-width:100%;max-height:400px" />
        </div>
        <el-alert
          v-else
          title="该类型文件不支持内联预览，请点击上方链接在新标签页中打开"
          type="info"
          show-icon
          :closable="false"
        />
      </div>
      <el-alert v-else type="error" title="获取预览链接失败" show-icon :closable="false" />
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary" @click="openPreviewNewTab" :disabled="!previewUrl">
          在新标签页打开
        </el-button>
      </template>
    </el-dialog>

    <!-- 错误提示 -->
    <el-alert
      v-if="errorMsg"
      :title="errorMsg"
      type="error"
      closable
      show-icon
      class="error-alert"
      @close="errorMsg = ''"
    />
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { List, Refresh, Link, CopyDocument, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { listFiles, deleteFile, getPreviewUrl } from '../api/oss.js'

const props = defineProps({
  // 外部可通过此 prop 触发列表刷新
  refreshTrigger: { type: Number, default: 0 }
})

const files = ref([])
const loading = ref(false)
const errorMsg = ref('')

// 预览状态
const previewVisible = ref(false)
const previewLoading = ref(false)
const previewUrl = ref('')
const previewObjectName = ref('')

// 文件类型判断
const isImageFile = computed(() => {
  const n = previewObjectName.value.toLowerCase()
  return /\.(jpg|jpeg|png|gif|webp|bmp|svg)$/.test(n)
})
const isPdfFile   = computed(() => /\.pdf$/i.test(previewObjectName.value))
const isVideoFile = computed(() => /\.(mp4|webm|ogg|mov)$/i.test(previewObjectName.value))

function formatSize(bytes) {
  if (bytes == null) return '-'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = Number(bytes)
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(i > 0 ? 1 : 0) + ' ' + units[i]
}

function formatTime(val) {
  if (!val) return '-'
  try {
    const d = new Date(val)
    if (isNaN(d.getTime())) return val
    return d.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return val
  }
}

async function fetchList() {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await listFiles()
    files.value = res.data || []
  } catch (e) {
    errorMsg.value = e.message || '获取文件列表失败'
    files.value = []
  } finally {
    loading.value = false
  }
}

async function handleDelete(objectName) {
  try {
    await deleteFile(objectName)
    ElMessage.success(`"${objectName}" 已删除`)
    await fetchList()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

async function handlePreview(objectName) {
  previewObjectName.value = objectName
  previewVisible.value = true
  previewUrl.value = ''
  previewLoading.value = true
  try {
    const res = await getPreviewUrl(objectName)
    previewUrl.value = res.data
  } catch (e) {
    ElMessage.error(e.message || '获取预览链接失败')
  } finally {
    previewLoading.value = false
  }
}

function handleCopy(objectName) {
  navigator.clipboard.writeText(objectName).then(
    () => ElMessage.success(`已复制: ${objectName}`),
    () => {
      // fallback
      const ta = document.createElement('textarea')
      ta.value = objectName
      document.body.appendChild(ta)
      ta.select()
      document.execCommand('copy')
      document.body.removeChild(ta)
      ElMessage.success(`已复制: ${objectName}`)
    }
  )
}

function copyPreviewUrl() {
  navigator.clipboard.writeText(previewUrl.value).then(
    () => ElMessage.success('预览链接已复制'),
    () => {
      const ta = document.createElement('textarea')
      ta.value = previewUrl.value
      document.body.appendChild(ta)
      ta.select()
      document.execCommand('copy')
      document.body.removeChild(ta)
      ElMessage.success('预览链接已复制')
    }
  )
}

function openPreviewNewTab() {
  if (previewUrl.value) {
    window.open(previewUrl.value, '_blank')
  }
}

// 监听外部刷新触发器
watch(() => props.refreshTrigger, () => {
  fetchList()
})

// 挂载时自动加载
onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.file-list-card {
  --el-card-border-color: transparent;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
  flex-wrap: wrap;
}

.count-tag {
  margin-left: auto;
}

.loading-area {
  padding: 24px 0;
}

.time-cell {
  font-size: 13px;
  color: #909399;
}

.preview-link-row {
  display: flex;
  gap: 8px;
  margin: 12px 0;
}

.inline-preview-box {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.pdf-frame {
  width: 100%;
  height: 450px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
}

.error-alert {
  margin-top: 12px;
}

.preview-loading {
  padding: 24px 0;
}
</style>
