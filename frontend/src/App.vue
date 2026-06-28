<template>
  <el-container class="app-container">
    <el-header class="app-header">
      <h1>
        <el-icon :size="28"><FolderOpened /></el-icon>
        MinIO 文件管理
      </h1>
    </el-header>

    <el-main>
      <FileUploader @uploaded="onUploaded" />
      <FileList ref="fileListRef" :refresh-trigger="refreshTrigger" />
    </el-main>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { FolderOpened } from '@element-plus/icons-vue'
import FileUploader from './components/FileUploader.vue'
import FileList from './components/FileList.vue'

const fileListRef = ref(null)
const refreshTrigger = ref(0)

function onUploaded() {
  // 上传成功后触发文件列表刷新
  refreshTrigger.value++
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background: #f0f2f5;
}

.app-container {
  min-height: 100vh;
}

.app-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  padding: 0 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.app-header h1 {
  color: #fff;
  font-size: 22px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
}

.el-main {
  max-width: 1200px;
  margin: 24px auto;
  padding: 0 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}
</style>
