import axios from 'axios'

const api = axios.create({
  baseURL: '/api/oss',
  timeout: 30000
})

// 响应拦截 —— 统一取出 data
api.interceptors.response.use(
  response => response.data,
  error => {
    const msg = error.response?.data?.msg || error.message || '网络错误'
    return Promise.reject(new Error(msg))
  }
)

/**
 * POST /api/oss/upload
 * 上传文件到 MinIO
 * @param {File} file
 * @returns {Promise<{code: number, msg: string, data: string}>} data 是 objectName
 */
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return api.post('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * GET /api/oss/preview/{objectName}
 * 获取预签名访问链接（有效期 10 分钟）
 * @param {string} objectName
 * @returns {Promise<{code: number, msg: string, data: string}>} data 是预签名 URL
 */
export function getPreviewUrl(objectName) {
  return api.get(`/preview/${encodeURIComponent(objectName)}`)
}

/**
 * DELETE /api/oss/delete/{objectName}
 * 删除指定文件
 * @param {string} objectName
 * @returns {Promise<{code: number, msg: string}>}
 */
export function deleteFile(objectName) {
  return api.delete(`/delete/${encodeURIComponent(objectName)}`)
}

/**
 * GET /api/oss/list
 * 列出 Bucket 中所有文件
 * @returns {Promise<{code: number, msg: string, data: Array}>} data 是文件列表
 */
export function listFiles() {
  return api.get('/list')
}
