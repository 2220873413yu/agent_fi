# XMS 管理后台前端

`xms-ui` 是 XMS 管理后台前端项目，基于 RuoYi-Vue / Vue2 / Element UI 开发，主要对应后端服务 `xms-agent-admin`。

## 环境要求

- 建议使用 Node.js 16 或更低的稳定版本。
- 如果 Node 版本过高，构建时可能出现 OpenSSL 相关错误，可临时设置：

```bash
# Windows CMD
set NODE_OPTIONS=--openssl-legacy-provider

# Linux / macOS
export NODE_OPTIONS=--openssl-legacy-provider
```

## 本地开发

```bash
# 进入前端目录
cd xms-ui

# 安装依赖
npm install

# 如果 npm 下载慢，使用镜像源安装
npm install --registry=https://registry.npmmirror.com

# 启动开发服务
npm run dev
```

启动后浏览器访问：

```text
http://localhost:80
```

不建议直接使用 `cnpm` 安装依赖，容易出现依赖结构和构建异常。

## 构建发布包

```bash
# 构建测试环境
npm run build:stage

# 构建生产环境
npm run build:prod
```

构建完成后，静态文件输出到：

```text
xms-ui/dist/
```

如需检查代码风格：

```bash
npm run lint
```

## 环境配置说明

环境配置文件位于项目根目录：

- `.env.development`：本地开发环境。
- `.env.staging`：测试环境构建，对应 `npm run build:stage`。
- `.env.production`：生产环境构建，对应 `npm run build:prod`。

常用变量：

- `VUE_APP_BASE_API`：前端请求后端接口的基础路径。当前配置为 `/dev-web`。
- `VUE_APP_TARGET`：本地开发代理或部署时后端目标地址，例如 `https://backend.agentwin.pro`。
- `VUE_APP_PIC_API`：图片、文件等资源拼接域名。
- `VUE_APP_TITLE`：页面标题。

当前 `vue.config.js` 中 `publicPath` 为 `/`，表示前端静态资源默认部署在域名根路径。

如果未来前端要部署到类似 `/DEV-WEB/`、`/admin/` 这样的子路径，需要同步调整：

- `vue.config.js` 的 `publicPath`
- Nginx 静态资源 `location`
- 前端访问地址
- 后端接口代理路径

否则容易出现页面可打开，但刷新 404 或静态资源 404。

## Linux / Nginx 部署口径

标准流程：

1. 本地或构建机执行 `npm run build:stage` 或 `npm run build:prod`。
2. 将 `xms-ui/dist/` 上传到服务器 Nginx 静态目录。
3. Nginx 配置前端静态资源和 `/dev-web` 后端代理。
4. 重载 Nginx。

Nginx 示例：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /data/www/xms-ui/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /dev-web/ {
        proxy_pass http://127.0.0.1:18676/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

注意：

- `/dev-web` 必须和 `VUE_APP_BASE_API` 保持一致。
- `proxy_pass` 的目标地址需要替换为实际后端 `xms-agent-admin` 地址。
- 如果后端接口本身也带 `/dev-web` 前缀，Nginx 是否重写路径要和后端实际入口保持一致。
- `try_files $uri $uri/ /index.html;` 用于支持 Vue Router 页面刷新。

## 常见问题

### 接口 404

优先检查：

- `.env.*` 中的 `VUE_APP_BASE_API` 是否和 Nginx `location` 一致。
- Nginx `/dev-web/` 是否正确转发到后端服务。
- 后端服务是否已启动，端口是否正确。

### 页面刷新 404

检查 Nginx 是否配置：

```nginx
try_files $uri $uri/ /index.html;
```

### 静态资源 404

当前 `publicPath=/`，默认要求前端部署在域名根路径。

如果部署到子路径，需要调整 `vue.config.js` 的 `publicPath`，并同步调整 Nginx 静态资源路径。

### Node OpenSSL 报错

临时设置：

```bash
set NODE_OPTIONS=--openssl-legacy-provider
```

Linux 使用：

```bash
export NODE_OPTIONS=--openssl-legacy-provider
```

### 图片或上传地址不对

检查当前构建使用的 `.env.*` 文件中：

```text
VUE_APP_PIC_API
```

图片域名、上传域名和 Nginx 映射路径要保持一致。
