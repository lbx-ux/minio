# MinIO 文件管理系统

基于 **Spring Boot 4.1 + Vue 3 + MinIO** 的私有云存储文件管理平台，支持文件上传、列表展示、预览链接生成和文件删除。

## 项目结构

```
MinIo/
├── pom.xml                              # Maven 依赖（Spring Boot 4.1 / MinIO SDK）
├── src/
│   ├── main/
│   │   ├── java/com/fjq/
│   │   │   ├── MinIoApplication.java    # Spring Boot 入口
│   │   │   ├── config/
│   │   │   │   └── MinioConfig.java     # MinIO Client Bean 配置
│   │   │   ├── controller/
│   │   │   │   └── FileController.java  # REST API 控制器
│   │   │   ├── properties/
│   │   │   │   └── MinioProperties.java # MinIO 配置属性映射
│   │   │   └── service/
│   │   │       └── MinioService.java    # 业务逻辑层
│   │   └── resources/
│   │       └── application.yaml         # 应用配置文件
│   └── test/
│       └── java/com/fjq/
│           └── controller/
│               └── FileControllerTest.java  # 控制器 API 测试
├── frontend/                            # Vue 3 前端项目
│   ├── index.html
│   ├── vite.config.js                   # Vite 配置（含 API 代理）
│   ├── package.json
│   └── src/
│       ├── main.js                      # Vue 入口
│       ├── App.vue                      # 根组件
│       ├── api/
│       │   └── oss.js                   # 后端 API 封装
│       └── components/
│           ├── FileUploader.vue         # 文件上传组件
│           ├── FilePreview.vue          # 预览链接获取组件
│           └── FileList.vue             # 文件列表与删除组件
└── .claude/
    └── launch.json                      # CCD 启动配置
```

## 技术栈

| 层次 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 4.1.0 |
| Java | JDK | 17 |
| 对象存储 | MinIO SDK | 8.5.17 |
| 构建工具 | Maven | 3.x |
| 前端框架 | Vue 3 | 3.5+ |
| UI 组件库 | Element Plus | 2.9+ |
| HTTP 客户端 | Axios | 1.7+ |
| 构建工具 | Vite | 6.0+ |

## 后端 API 接口

Base URL: `http://localhost:8080/api/oss`

### 文件上传

```
POST /api/oss/upload
Content-Type: multipart/form-data

参数:
  file (required) — 文件

返回:
  { "code": 200, "msg": "上传成功", "data": "abc123.jpg" }
```

### 列出所有文件

```
GET /api/oss/list

返回:
  {
    "code": 200,
    "msg": "获取成功",
    "data": [
      { "name": "abc123.jpg", "size": 102400, "lastModified": "...", "etag": "..." }
    ]
  }
```

### 获取预览链接

```
GET /api/oss/preview/{objectName}

返回:
  { "code": 200, "msg": "获取成功", "data": "http://...?sign=..." }
```

> 生成的预签名链接有效期 10 分钟。

### 删除文件

```
DELETE /api/oss/delete/{objectName}

返回:
  { "code": 200, "msg": "删除成功" }
```

## 快速开始

### 1. 前置条件

- JDK 17+
- Maven 3.x
- Node.js 18+
- MinIO 服务（已运行并可访问）

### 2. 配置 MinIO 连接

编辑 `src/main/resources/application.yaml`，修改为你自己的 MinIO 连接信息：

```yaml
minio:
  endpoint: http://<你的MinIO IP>:<端口>
  access-key: <Access Key>
  secret-key: <Secret Key>
  bucket-name: <Bucket 名称>
```

### 3. 启动后端

```bash
# 编译
mvn clean compile

# 启动 Spring Boot（默认 8080 端口）
mvn spring-boot:run
```

### 4. 启动前端

```bash
cd frontend

# 安装依赖（仅首次）
npm install

# 启动开发服务器（默认 5173 端口）
npm run dev
```

### 5. 访问

浏览器打开 **`http://localhost:5173`**，前端会自动将 `/api` 请求代理到后端 `http://localhost:8080`。

### 6. 运行测试

```bash
mvn test -Dtest=FileControllerTest
```

## 关键设计说明

- **文件存储**：上传后使用 UUID 重命名，防止文件重名冲突。
- **预览链接**：通过 MinIO 预签名 URL 实现临时授权访问，默认 10 分钟有效，不暴露实际存储路径。
- **文件上限**：后端 `multipart.max-file-size` 设置为 50MB，前端也有对应校验。
- **自动初始化**：MinioClient Bean 创建时自动检查并创建 Bucket（若不存在）。
- **前端错误处理**：Axios 拦截器统一提取错误信息，组件层按场景展示。
