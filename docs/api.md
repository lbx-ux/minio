# MinIO 文件管理系统 — API 接口文档

## 概述

Base URL: `http://localhost:8080`

所有接口统一使用以下响应格式：

```json
{
  "code": 200,
  "msg": "操作描述",
  "data": "业务数据（可选）"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | number | 状态码，200 表示成功 |
| msg | string | 操作结果描述 |
| data | any | 业务数据，删除操作无此字段 |

---

## 一、文件上传

### 基本信息

| 项目 | 内容 |
|------|------|
| 请求路径 | `POST /api/oss/upload` |
| Content-Type | `multipart/form-data` |
| 最大文件 | 50MB |

### 请求参数

| 参数名 | 类型 | 必填 | 位置 | 说明 |
|--------|------|:--:|------|------|
| file | file | ✅ | Body (form-data) | 要上传的文件 |

### 请求示例

```bash
curl -X POST http://localhost:8080/api/oss/upload \
  -F "file=@/path/to/photo.jpg"
```

```javascript
// JavaScript / Axios
const formData = new FormData()
formData.append('file', file)
axios.post('/api/oss/upload', formData)
```

### 成功响应

```json
{
  "code": 200,
  "msg": "上传成功",
  "data": "a1b2c3d4e5f6.jpg"
}
```

`data` 字段返回的是 MinIO 上的 Object Name（UUID 重命名），用于后续操作和预览。

### 错误响应

| 场景 | HTTP 状态码 | 说明 |
|------|:---------:|------|
| 未选择文件 | 400 | 缺少 `file` 参数 |
| 文件超过 50MB | 413 | 超出后端 `max-file-size` 限制 |
| MinIO 连接异常 | 500 | 后端抛出 RuntimeException |
| Bucket 初始化失败 | 启动时失败 | 应用无法启动 |

---

## 二、列出所有文件

### 基本信息

| 项目 | 内容 |
|------|------|
| 请求路径 | `GET /api/oss/list` |
| 请求方式 | GET |

### 请求参数

无。

### 请求示例

```bash
curl http://localhost:8080/api/oss/list
```

### 成功响应

```json
{
  "code": 200,
  "msg": "获取成功",
  "data": [
    {
      "name": "a1b2c3d4e5f6.jpg",
      "size": 204800,
      "lastModified": "2026-06-28T12:00:00Z",
      "etag": "d41d8cd98f00b204e9800998ecf8427e"
    }
  ]
}
```

### data 文件对象字段

| 字段 | 类型 | 说明 |
|------|------|------|
| name | string | Object Name（文件名标识） |
| size | number | 文件大小（字节） |
| lastModified | string | 最后修改时间 (ISO 8601) |
| etag | string | 文件 ETag 值 |

### 错误响应

| 场景 | HTTP 状态码 | 说明 |
|------|:---------:|------|
| MinIO 连接异常 | 500 | 后端抛出 RuntimeException |

---

## 三、获取预览链接

### 基本信息

| 项目 | 内容 |
|------|------|
| 请求路径 | `GET /api/oss/preview/{objectName}` |
| 请求方式 | GET |
| 链接有效期 | 10 分钟 |

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|:--:|------|
| objectName | string | ✅ | 文件名标识（上传接口返回的 data 值） |

### 请求示例

```bash
curl http://localhost:8080/api/oss/preview/a1b2c3d4e5f6.jpg
```

```javascript
// JavaScript — 注意对特殊字符编码
axios.get(`/api/oss/preview/${encodeURIComponent('a1b2c3d4e5f6.jpg')}`)
```

### 成功响应

```json
{
  "code": 200,
  "msg": "获取成功",
  "data": "http://192.168.146.130:9005/my-bucket/a1b2c3d4e5f6.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=..."
}
```

> `data` 为 MinIO 预签名 URL，可直接在浏览器打开或嵌入 `<img>` / `<video>` / `<iframe>` 中预览。

### 错误响应

| 场景 | HTTP 状态码 | 说明 |
|------|:---------:|------|
| 文件名不存在 | 500 | "获取文件访问链接失败" |
| MinIO 连接异常 | 500 | 后端抛出 RuntimeException |

---

## 四、删除文件

### 基本信息

| 项目 | 内容 |
|------|------|
| 请求路径 | `DELETE /api/oss/delete/{objectName}` |
| 请求方式 | DELETE |

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|:--:|------|
| objectName | string | ✅ | 要删除的文件名标识 |

### 请求示例

```bash
curl -X DELETE http://localhost:8080/api/oss/delete/a1b2c3d4e5f6.jpg
```

### 成功响应

```json
{
  "code": 200,
  "msg": "删除成功"
}
```

> ⚠️ 删除操作不可逆，请在调用前做二次确认。

### 错误响应

| 场景 | HTTP 状态码 | 说明 |
|------|:---------:|------|
| 文件名不存在 | 500 | "文件删除失败" |
| MinIO 连接异常 | 500 | 后端抛出 RuntimeException |

---

## 错误码速查

| HTTP 状态码 | 常见原因 |
|:---------:|------|
| 200 | 正常 |
| 400 | 缺少必填参数 |
| 405 | HTTP 方法不匹配（如 GET 访问 POST 接口） |
| 413 | 文件大小超出限制 |
| 500 | 服务端异常（MinIO 连接失败、文件不存在等） |

---

## 附录

### 配置文件参考

```yaml
# src/main/resources/application.yaml
spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB

minio:
  endpoint: http://<MinIO IP>:<端口>
  access-key: <Access Key>
  secret-key: <Secret Key>
  bucket-name: <Bucket 名称>
```

### 项目启动顺序

1. 确保 MinIO 服务已运行
2. 启动后端：`mvn spring-boot:run`
3. 启动前端：`cd frontend && npm run dev`
4. 访问：`http://localhost:5173`

### 测试

```bash
mvn test -Dtest=FileControllerTest
```
