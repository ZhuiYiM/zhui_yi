# 身份认证系统 - 快速启动指南

## 🚀 5 分钟快速上手

### 前置条件
- MySQL 数据库已安装并运行
- Node.js 16+ 和 npm 已安装
- Maven 3.6+ 已安装

---

## 步骤 1: 数据库初始化 (2 分钟)

```powershell
# 进入项目目录
cd F:\code\CampusInformationPlatform\demo

# 执行数据库初始化脚本
Get-Content database_user_identity.sql -Encoding UTF8 | mysql -u root -p123456 campus_db
```

**预期输出**:
```
status
status
status
status
status
user_id username        identity_type   identity_name   verified
...
```

---

## 步骤 2: 验证数据库 (1 分钟)

```powershell
# 运行验证查询
Get-Content verify_identity_system.sql | mysql -u root -p123456 campus_db
```

**应该看到**:
- 所有用户身份列表（5 条记录）
- 待审核申请（1 条：merchant1）
- 已认证身份（4 条）
- 统计信息

---

## 步骤 3: 启动后端 (1 分钟)

```powershell
# 编译并启动 Spring Boot 应用
mvn clean spring-boot:run
```

**预期输出**:
```
Started DemoApplication in X.XXX seconds
Tomcat started on port(s): 8080
```

---

## 步骤 4: 启动前端 (1 分钟)

打开新终端窗口:

```powershell
cd F:\code\CampusInformationPlatform\demo
npm run dev
```

**预期输出**:
```
VITE v5.x.x ready in xxx ms
➜  Local:   http://localhost:5173/
```

---

## 步骤 5: 测试功能

### 测试场景 1: 管理员审核身份认证

1. **访问**: http://localhost:5173
2. **登录**: 
   - 用户名：`admin`
   - 密码：`admin123`
3. **进入审核页面**: `/admin/identity-verifications`
4. **操作**:
   - 查看待审核申请（merchant1）
   - 点击"通过"或"拒绝"

### 测试场景 2: 用户申请身份认证

1. **退出登录**
2. **登录**: 
   - 用户名：`guest1`
   - 密码：`password123`
3. **进入个人中心**
4. **点击"申请认证"**
5. **填写表单**:
   - 选择身份类型：商户
   - 填写店铺名称
   - 上传营业执照图片
6. **提交申请**

### 测试场景 3: 查看身份徽章

1. **登录后查看**用户头像区域
2. **已认证用户**会显示对应身份的徽章
3. **多身份用户**显示所有已认证身份

---

## 🔍 API 测试

### 使用 Postman 测试

#### 1. 获取用户身份列表
```http
GET http://localhost:8080/api/user/identity
Authorization: Bearer <your_token>
```

#### 2. 申请身份认证
```http
POST http://localhost:8080/api/user/identity/apply
Content-Type: application/json
Authorization: Bearer <your_token>

{
  "identityType": "merchant",
  "shopName": "测试店铺",
  "businessLicenseUrl": "/uploads/test.jpg"
}
```

#### 3. 管理员审核
```http
PUT http://localhost:8080/api/user/identity/5/verify
Content-Type: application/json
Authorization: Bearer <admin_token>

{
  "approved": true,
  "reason": "资料齐全，予以通过"
}
```

#### 4. 获取管理员统计
```http
GET http://localhost:8080/api/admin/identity/stats
Authorization: Bearer <admin_token>
```

**预期响应**:
```json
{
  "code": 200,
  "data": {
    "total": 5,
    "verified": 4,
    "pending": 1,
    "byType": {
      "student": 2,
      "staff": 1,
      "merchant": 2,
      "organization": 1
    }
  }
}
```

---

## 🛠️ 常见问题

### Q1: 数据库初始化失败
**错误**: `Table 'users' doesn't exist`

**解决**: 先执行基础数据库脚本创建 users 表
```sql
-- 确保已有 users 表
SELECT COUNT(*) FROM users;
```

### Q2: 后端启动失败
**错误**: `Port 8080 is already in use`

**解决**: 修改端口或关闭占用程序
```bash
# 查看占用端口的进程
netstat -ano | findstr :8080
taskkill /F /PID <pid>
```

### Q3: 前端无法连接后端
**错误**: `Network Error`

**解决**: 
1. 检查后端是否启动在 `http://localhost:8080`
2. 检查 `src/api/request.js` 的 baseURL 配置
3. 检查浏览器控制台 CORS 错误

### Q4: Token 过期
**错误**: `登录已过期`

**解决**: 
1. 清除 localStorage
2. 重新登录
3. Token 有效期默认为 24 小时

---

## 📊 测试账号

| 用户名 | 密码 | 角色 | 已有身份 |
|--------|------|------|----------|
| admin | admin123 | 管理员 | 学生（已认证） |
| zhangsan | password123 | 用户 | 商户（已认证） |
| lisi | password123 | 用户 | 教职工（已认证） |
| wangwu | password123 | 用户 | 团体（已认证） |
| merchant1 | password123 | 用户 | 商户（待审核） |
| guest1 | password123 | 用户 | 无身份 |

---

## 🎯 验证清单

完成以下检查确保系统正常:

- [ ] 数据库表 `user_identity` 创建成功
- [ ] 测试数据插入成功（至少 5 条记录）
- [ ] 后端编译无错误
- [ ] 前端编译无错误
- [ ] 能够使用 admin 账号登录
- [ ] 能够访问管理员审核页面
- [ ] 能够查看待审核列表
- [ ] 能够通过/拒绝认证申请
- [ ] 普通用户能够申请身份认证
- [ ] 身份徽章正确显示

---

## 📝 下一步

系统正常运行后，可以:

1. **添加更多测试数据** - 模拟真实场景
2. **完善 UI 界面** - 优化用户体验
3. **集成到现有系统** - 与个人中心、管理员系统集成
4. **添加通知功能** - 邮件/短信提醒
5. **实现高级筛选** - 按时间、类型、状态筛选

---

## 🆘 获取帮助

遇到问题时:

1. 查看后端日志：`target/spring.log`
2. 查看前端控制台：浏览器 F12
3. 检查数据库连接：`mysql -u root -p123456 campus_db`
4. 阅读完整文档：`IDENTITY_SYSTEM_COMPLETE.md`

---

祝使用愉快！🎉
