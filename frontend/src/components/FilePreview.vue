<template>
  <el-card shadow="hover" class="preview-card">
    <template #header>
      <div class="card-header">
        <el-icon :size="20"><Link /></el-icon>
        <span>获取预览链接</span>
      </div>
    </template>

    <div class="preview-form">
      <el-input
        v-model="objectName"
        placeholder="输入文件标识（Object Name），如上传成功后返回的值"
        clearable
        :prefix-icon="Document"
        @keyup.enter="fetchPreview"
      >
        <template #append>
          <el-button
            :icon="Search"
            :loading="loading"
            @click="fetchPreview"
          >
            获取链接
          </el-button>
        </template>
      </el-input>
    </div>

    <!-- 预览结果 -->
    <div v-if="previewUrl" class="preview-result">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="文件标识">{{ currentObject }}</el-descriptions-item>
        <el-descriptions-item label="预览链接（10分钟有效）">
          <div class="url-row">
            <el-link type="primary" :href="previewUrl" target="_blank" :underline="false">
              <el-icon><Link /></el-icon>
              {{ previewUrl }}
            </el-link>
            <el-button type="primary" size="small" :icon="CopyDocument" @click="copyUrl">
              复制
            </el-button>
          </div>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 图片 / PDF 直接预览 -->
      <div v-if="isImage" class="inline-preview">
        <el-image
          :src="previewUrl"
          fit="contain"
          style="max-width: 100%; max-height: 500px; border-radius: 8px;"
          :preview-src-list="[previewUrl]"
          preview-teleported
        />
      </div>
      <div v-else-if="isPdf" class="inline-preview">
        <iframe :src="previewUrl" class="pdf-frame" />
      </div>
      <div v-else-if="isVideo" class="inline-preview">
        <video :src="previewUrl" controls style="max-width: 100%; max-height: 400px;" />
      </div>
      <div v-else class="inline-preview">
        <el-alert
          title="该类型文件暂不支持内联预览"
          type="info"
          show-icon
          :closable="false"
        >
          <template #default>
            请点击链接在新标签页中打开
          </template>
        </el-alert>
      </div>
    </div>

    <el-alert
      v-if="errorMsg"
      :title="errorMsg"
      type="error"
      closable
      show-icon
      @close="errorMsg = ''"
    />

    <!-- 使用说明 -->
    <el-divider />
    <div class="help-text">
      <p><strong>使用说明：</strong></p>
      <ul>
        <li>① 先在上方"文件上传"中上传文件，记录返回的 Object Name</li>
        <li>② 将 Object Name 粘贴到输入框中，点击"获取链接"</li>
        <li>③ 复制链接或在浏览器中打开预览 / 下载</li>
      </ul>
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Link, Search, Document, CopyDocument } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getPreviewUrl } from '../api/oss.js'

const objectName = ref('')
const loading = ref(false)
const previewUrl = ref('')
const errorMsg = ref('')
const currentObject = ref('')

const fileExtension = computed(() => {
  const name = (currentObject.value || '').toLowerCase()
  const idx = name.lastIndexOf('.')
  return idx > -1 ? name.substring(idx + 1) : ''
})

const isImage  = computed(() => ['jpg','jpeg','png','gif','webp','bmp','svg'].includes(fileExtension.value))
const isPdf    = computed(() => fileExtension.value === 'pdf')
const isVideo  = computed(() => ['mp4','webm','ogg','mov'].includes(fileExtension.value))

async function fetchPreview() {
  const name = objectName.value.trim()
  if (!name) {
    errorMsg.value = '请输入文件标识'
    return
  }

  loading.value = true
  errorMsg.value = ''
  previewUrl.value = ''

  try {
    const res = await getPreviewUrl(name)
    previewUrl.value = res.data
    currentObject.value = name
  } catch (e) {
    errorMsg.value = e.message || '获取预览链接失败'
  } finally {
    loading.value = false
  }
}

async function copyUrl() {
  try {
    await navigator.clipboard.writeText(previewUrl.value)
    ElMessage.success('链接已复制到剪贴板')
  } catch {
    // fallback
    const textarea = document.createElement('textarea')
    textarea.value = previewUrl.value
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success('链接已复制到剪贴板')
  }
}
</script>

<style scoped>
.preview-card {
  --el-card-border-color: transparent;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
}

.preview-form {
  margin-bottom: 8px;
}

.preview-result {
  margin-top: 16px;
}

.url-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.url-row .el-link {
  font-size: 13px;
  word-break: break-all;
  flex: 1;
  min-width: 200px;
}

.inline-preview {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.pdf-frame {
  width: 100%;
  height: 500px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
}

.el-alert {
  margin-top: 12px;
}

.help-text {
  color: #909399;
  font-size: 13px;
  line-height: 1.8;
}

.help-text ul {
  padding-left: 16px;
  margin-top: 4px;
}

.help-text strong {
  color: #606266;
}
</style>
