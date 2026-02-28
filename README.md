# 校园信息平台前端

基于 Vue 3 + Vite 构建的校园信息平台前端项目。

## 项目介绍

这是一个现代化的校园信息平台前端应用，提供以下核心功能：

- 🏫 校园地图导航
- 💬 消息交流系统  
- 🛒 校园商城服务
- 📝 话题墙社区
- 👤 个人中心管理
- 🔐 账户认证系统

## 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **路由管理**: Vue Router 4
- **状态管理**: Pinia
- **UI 组件**: Element Plus
- **HTTP 客户端**: Axios
- **测试框架**: Vitest + Vue Test Utils

## 目录结构

```
src/
├── api/           # API 接口定义
├── components/    # 组件目录
│   ├── common/    # 公共组件
│   └── user/      # 用户相关组件
├── config/        # 配置文件
├── router/        # 路由配置
├── stores/        # 状态管理
├── utils/         # 工具函数
└── views/         # 页面视图
tests/             # 测试文件
```

## 开发环境搭建

### 前置要求

- Node.js >= 16.0.0
- npm >= 8.0.0

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

默认访问地址: http://localhost:5173

### 构建生产版本

```bash
npm run build
```

### 运行测试

```bash
npm run test
```

## Git 工作流

### 分支策略

- `main`: 主分支，用于生产环境
- `develop`: 开发分支，集成所有功能
- `feature/*`: 功能分支，开发新功能
- `hotfix/*`: 热修复分支，紧急修复bug

### 提交规范

遵循 conventional commits 规范：

```
<type>(<scope>): <subject>

<body>

<footer>
```

常见类型：
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具变动

### 示例提交

```bash
git commit -m "feat(user): 添加用户登录功能"
git commit -m "fix(api): 修复接口请求超时问题"
```

## 配置说明

### 环境变量

项目支持以下环境变量配置：

```bash
# .env.development
VITE_API_BASE_URL=http://localhost:3000/api
VITE_APP_TITLE=校园信息平台

# .env.production  
VITE_API_BASE_URL=https://api.example.com
VITE_APP_TITLE=校园信息平台
```

### 代理配置

开发环境下通过 Vite 代理解决跨域问题，在 `vite.config.js` 中配置：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:3000',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

## 部署指南

### 本地部署

1. 构建项目：
```bash
npm run build
```

2. 将 `dist` 目录部署到 Web 服务器

### Docker 部署

```dockerfile
FROM nginx:alpine
COPY dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## 测试

项目包含单元测试和集成测试：

```bash
# 运行所有测试
npm run test

# 运行测试并监听文件变化
npm run test:watch

# 生成测试覆盖率报告
npm run test:coverage
```

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

MIT License

## 联系方式

如有问题，请联系项目维护者。