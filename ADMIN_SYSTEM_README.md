# 管理员系统快速启动指南

## 📋 第一步：执行数据库脚本

在 MySQL 中运行以下命令创建管理员系统表：

```bash
mysql -u root -p campus_db < src/main/resources/sql/admin_tables.sql
```

或者手动复制 `src/main/resources/sql/admin_tables.sql` 的内容到 MySQL 客户端执行。

## 🔐 默认管理员账号

- **用户名**: admin
- **密码**: admin123

## 🌐 访问地址

### 管理员登录页
```
http://localhost:5173/admin/login
```

### 管理后台首页
```
http://localhost:5173/admin/dashboard
```

## 📁 已实现功能

### ✅ 后端功能
- [x] 管理员登录/退出
- [x] JWT Token 认证
- [x] BCrypt 密码加密
- [x] 管理员信息管理
- [x] 修改密码
- [x] 操作日志记录（待完善）

### ✅ 前端功能
- [x] 管理员登录页面
- [x] 管理后台布局
- [x] 数据统计面板
- [x] 侧边栏导航
- [x] 用户信息展示

## 🚀 下一步开发建议

### 第一阶段（已完成）✅
- [x] 数据库设计
- [x] 基础实体类和 Mapper
- [x] 登录认证功能
- [x] 管理后台框架

### 第二阶段（待开发）
- [ ] 用户管理列表
- [ ] 话题审核功能
- [ ] 商品管理功能
- [ ] 举报处理功能
- [ ] 角色权限管理

### 第三阶段（优化）
- [ ] 操作日志完整实现
- [ ] 数据导出功能
- [ ] 批量操作功能
- [ ] 权限拦截器

## 🛠️ 技术栈

**后端**
- Spring Boot 3.2.0
- MyBatis-Plus
- JWT (jjwt)
- BCrypt 密码加密

**前端**
- Vue 3 + Vite
- Element Plus
- Vue Router
- Axios

## 📝 API 接口说明

### 管理员登录
```
POST /admin/login
Body: {
  "username": "admin",
  "password": "admin123"
}
Response: {
  "code": 200,
  "data": {
    "token": "xxx",
    "adminId": 1,
    "username": "admin"
  }
}
```

### 获取管理员信息
```
GET /admin/info
Headers: Authorization: Bearer {token}
```

### 修改密码
```
PUT /admin/change-password
Headers: Authorization: Bearer {token}
Body: {
  "oldPassword": "admin123",
  "newPassword": "new123456"
}
```

## ⚠️ 注意事项

1. **JWT 解析**: 当前控制器中的 Token 解析是简化版本，需要完善 JWT 工具类来正确解析 adminId
2. **权限控制**: 路由守卫需要添加 admin token 的检查逻辑
3. **安全性**: 生产环境需要添加验证码、登录失败限制等功能
4. **日志记录**: 建议使用 AOP 切面自动记录操作日志

## 🔧 常见问题

### Q: 登录后提示"登录成功"但无法进入后台？
A: 检查 localStorage 是否正确保存了 admin_token 和 admin_info

### Q: 访问管理后台被重定向到登录页？
A: 检查路由守卫中对 admin token 的验证逻辑

### Q: 数据库表创建失败？
A: 确保使用的是 MySQL 8.0+，并且字符集为 utf8mb4
