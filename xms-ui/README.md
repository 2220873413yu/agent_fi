
## 开发

```bash
# 克隆项目
git clone https://gitee.com/y_project/RuoYi-Vue

# 进入项目目录
cd ruoyi-ui

# 安装依赖
npm install

# 建议不要直接使用 cnpm 安装依赖，会有各种诡异的 bug。可以通过如下操作解决 npm 下载速度慢的问题
npm install --registry=https://registry.npmmirror.com

# 启动服务
npm run dev
```xms-api

浏览器访问 http://localhost:80

## 发布 注意： 要与NINGX的配置一致，比如，URL前缀有DEV-WEB，那么对应的服务端入口的NG需要重写转发

```bash

#node版本要是太高执行以下命令
set NODE_OPTIONS=--openssl-legacy-provider

# 构建测试环境
npm run build:stage

# 构建生产环境
npm run build:prod

# 构建生产环境的时候需要把后端vue项目的prod配置复制到stage配置文件那边去
然后用这个命令:npm run build:stage
http://xms-api.szrnet.xyz'
```