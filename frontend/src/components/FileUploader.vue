<template>
  <el-card shadow="hover" class="uploader-card">
    <template #header>
      <div class="card-header">
        <el-icon :size="20"><Upload /></el-icon>
        <span>文件上传</span>
      </div>
    </template>

    <el-upload
      ref="uploadRef"
      class="upload-area"
      drag
      :auto-upload="false"
      :limit="1"
      :on-change="onFileChange"
      :on-exceed="onExceed"
      :before-upload="beforeUpload"
      :http-request="handleUpload"
    >
      <el-icon class="upload-icon"><UploadFilled /></el-icon>
      <div class="upload-text">
        <p>拖拽文件到此处，或 <em>点击选择文件</em></p>
        <p class="upload-hint">支持任意格式，单个文件最大 50MB</p>
      </div>
    </el-upload>

    <!-- 已选文件信息 -->
    <div v-if="selectedFile" class="file-info">
      <el-tag type="info" closable @close="selectedFile = null">
        {{ selectedFile.name }}（{{ formatSize(selectedFile.size) }}）
      </el-tag>
    </div>

    <!-- 上传按钮 & 进度 -->
    <div class="upload-actions">
      <el-button
        type="primary"
        :icon="Upload"
        :loading="uploading"
        :disabled="!selectedFile"
        @click="submitUpload"
      >
        {{ uploading ? '上传中…' : '开始上传' }}
      </el-button>
      <el-button :disabled="!selectedFile || uploading" @click="selectedFile = null">
        清空
      </el-button>
    </div>

    <el-progress
      v-if="uploading"
      :percentage="progress"
      :stroke-width="6"
      :status="progressStatus"
      class="progress-bar"
    />

    <!-- 上传结果 -->
    <el-alert
      v-if="uploadResult"
      :title="uploadResult"
      :type="uploadSuccess ? 'success' : 'error'"
      closable
      show-icon
      @close="uploadResult = ''"
    />
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { Upload, UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { uploadFile } from '../api/oss.js'

const emit = defineEmits(['uploaded'])

const uploadRef = ref(null)
const selectedFile = ref(null)
const uploading = ref(false)
const progress = ref(0)
const progressStatus = ref('')
const uploadResult = ref('')
const uploadSuccess = ref(false)

function formatSize(bytes) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(i > 0 ? 1 : 0) + ' ' + units[i]
}

function onFileChange(file) {
  selectedFile.value = file.raw
}

function onExceed() {
  ElMessage.warning('每次只能上传一个文件，请先清空已选文件')
}

function beforeUpload(file) {
  const maxSize = 50 * 1024 * 1024 // 50MB — 与后端 multipart.max-file-size 一致
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 50MB')
    return false
  }
  return true
}

async function submitUpload() {
  if (!selectedFile.value) return
  uploadRef.value.submit()
}

async function handleUpload() {
  const file = selectedFile.value
  if (!file) return

  uploading.value = true
  uploadResult.value = ''
  progress.value = 0
  progressStatus.value = ''

  // 模拟进度
  const timer = setInterval(() => {
    if (progress.value < 90) {
      progress.value += Math.floor(Math.random() * 10) + 5
      if (progress.value > 90) progress.value = 90
    }
  }, 300)

  try {
    const res = await uploadFile(file)
    clearInterval(timer)
    progress.value = 100
    progressStatus.value = 'success'
    uploadSuccess.value = true
    uploadResult.value = `上传成功！文件标识: ${res.data}`
    selectedFile.value = null
    uploadRef.value.clearFiles()
    emit('uploaded', res.data)
  } catch (e) {
    clearInterval(timer)
    progress.value = 0
    progressStatus.value = 'exception'
    uploadSuccess.value = false
    uploadResult.value = e.message || '上传失败'
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.uploader-card {
  --el-card-border-color: transparent;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
}

.upload-area {
  width: 100%;
}

.upload-icon {
  font-size: 48px;
  color: #409eff;
}

.upload-text p {
  margin-top: 8px;
  color: #606266;
}

.upload-hint {
  font-size: 12px;
  color: #909399 !important;
}

.file-info {
  margin-top: 12px;
}

.upload-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}

.progress-bar {
  margin-top: 16px;
}

.el-alert {
  margin-top: 12px;
}
</style>
