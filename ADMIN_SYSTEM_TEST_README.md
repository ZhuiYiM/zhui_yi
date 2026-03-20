# 管理员系统测试说明

## 概述

本文档说明如何测试管理员系统的评论管理、操作日志、系统设置和举报管理功能。

## 测试环境准备

### 1. 数据库准备

确保以下数据库表已创建并有测试数据：

```sql
-- 1. 操作日志表
mysql -u root -p123456 campus_db -e "SELECT COUNT(*) FROM admin_operation_log;"
-- 应该返回 5 条测试数据

-- 2. 系统设置表
mysql -u root -p123456 campus_db -e "SELECT COUNT(*) FROM system_settings;"
-- 应该返回多条系统配置数据

-- 3. 评论表
mysql -u root -p123456 campus_db -e "SELECT COUNT(*) FROM topic_comments;"
-- 应该返回 14 条评论数据

-- 4. 举报表
mysql -u root -p123456 campus_db -e "SELECT COUNT(*) FROM reports;"
-- 应该返回 5 条举报数据
```

### 2. 后端服务

确保 Spring Boot 服务已启动并运行在 `http://localhost:8080`

```bash
# 启动服务
.\mvnw.cmd spring-boot:run

# 验证服务
curl http://localhost:8080/admin/comments?page=1&limit=10
```

### 3. 前端服务

确保 Vite 开发服务器已启动并运行在 `http://localhost:5173`

```bash
# 启动前端
npm run dev

# 访问管理后台
http://localhost:5173/admin/dashboard
```

## 测试方法

### 方法一：使用 Postman 测试集合

1. **导入集合**
   - 打开 Postman
   - 点击 Import
   - 选择 `admin-system.postman_collection.json` 文件

2. **运行集合**
   - 先运行 "认证管理/管理员登录" 接口获取 Token
   - Token 会自动保存到集合变量
   - 右键点击集合，选择 "Run collection"
   - 查看所有测试结果

3. **测试覆盖**
   - ✅ 评论列表查询（分页、关键词、状态筛选）
   - ✅ 评论删除功能
   - ✅ 操作日志查询（分页、模块、操作类型、操作人筛选）
   - ✅ 系统设置获取和更新
   - ✅ 举报列表查询（分页、类型、状态筛选）
   - ✅ 举报统计功能

### 方法二：使用 Jest 运行自动化测试

```bash
# 安装依赖（如果还没安装）
npm install --save-dev jest @babel/core @babel/preset-env

# 运行测试
npm test -- tests/admin-system.integration.test.js

# 查看测试覆盖率
npm test -- --coverage tests/admin-system.integration.test.js
```

### 方法三：使用 Java 单元测试

```bash
# 运行所有管理员系统测试
.\mvnw.cmd test -Dtest=AdminSystemTest

# 运行特定测试方法
.\mvnw.cmd test -Dtest=AdminSystemTest#testCommentManagement
```

### 方法四：手动测试前端页面

1. **访问管理后台**
   ```
   http://localhost:5173/admin/dashboard
   ```

2. **测试评论管理**
   - 访问：http://localhost:5173/admin/dashboard/comments
   - 验证功能：
     - ✅ 评论列表展示
     - ✅ 分页功能
     - ✅ 关键词搜索
     - ✅ 状态筛选
     - ✅ 删除评论

3. **测试操作日志**
   - 访问：http://localhost:5173/admin/dashboard/logs
   - 验证功能：
     - ✅ 日志列表展示
     - ✅ 分页功能
     - ✅ 按模块筛选
     - ✅ 按操作类型筛选
     - ✅ 按操作人筛选

4. **测试系统设置**
   - 访问：http://localhost:5173/admin/dashboard/settings
   - 验证功能：
     - ✅ 设置项加载
     - ✅ 表单编辑
     - ✅ 保存设置

5. **测试举报管理**
   - 访问：http://localhost:5173/admin/dashboard/reports
   - 验证功能：
     - ✅ 举报列表展示
     - ✅ 分页功能
     - ✅ 类型筛选
     - ✅ 状态筛选
   - 访问：http://localhost:5173/admin/dashboard/report-stats
   - 验证功能：
     - ✅ 统计卡片展示
     - ✅ 图表展示
     - ✅ 最近处理记录

## API 接口列表

### 评论管理

| 接口 | 方法 | 说明 | 参数 |
|------|------|------|------|
| `/admin/comments` | GET | 分页查询评论列表 | page, limit, keyword, status |
| `/admin/comments/{id}` | DELETE | 删除评论 | - |

### 操作日志

| 接口 | 方法 | 说明 | 参数 |
|------|------|------|------|
| `/admin/logs` | GET | 分页查询操作日志 | page, limit, module, operation, adminId |

### 系统设置

| 接口 | 方法 | 说明 | 参数 |
|------|------|------|------|
| `/admin/settings` | GET | 获取系统设置 | - |
| `/admin/settings` | PUT | 更新系统设置 | settingKey, settingValue |

### 举报管理

| 接口 | 方法 | 说明 | 参数 |
|------|------|------|------|
| `/admin/reports` | GET | 分页查询举报列表 | page, limit, type, status |
| `/admin/reports/stats` | GET | 获取举报统计 | - |

## 测试数据说明

### 操作日志测试数据

| ID | 操作人 | 操作类型 | 模块 | 目标 ID | 详情 |
|----|--------|----------|------|--------|------|
| 1 | admin | delete | topic | 10 | Delete topic |
| 2 | admin | update | user | 5 | Update user status |
| 3 | admin | delete | product | 8 | Remove product |
| 4 | admin | audit | verification | 3 | Approve verification |
| 5 | admin | delete | comment | 15 | Delete comment |

### 系统设置测试数据

| Key | Value | 说明 |
|-----|-------|------|
| site_name | Campus Platform | 网站名称 |
| site_description | Campus information exchange platform | 网站描述 |
| allow_registration | true | 允许注册 |
| max_upload_size | 5242880 | 最大上传文件大小（5MB） |
| allowed_image_types | jpg,jpeg,png,gif | 允许的图片类型 |

## 预期结果

### 评论管理测试
- ✅ 能够成功查询评论列表
- ✅ 分页功能正常
- ✅ 关键词搜索返回正确结果
- ✅ 状态筛选返回正确结果
- ✅ 删除评论成功

### 操作日志测试
- ✅ 能够成功查询操作日志
- ✅ 分页功能正常
- ✅ 各种筛选条件返回正确结果
- ✅ 日志数据完整

### 系统设置测试
- ✅ 能够成功获取系统设置
- ✅ 设置项完整
- ✅ 更新设置成功
- ✅ 更新后立即查询能获取到新值

### 举报管理测试
- ✅ 能够成功查询举报列表
- ✅ 分页功能正常
- ✅ 类型和状态筛选返回正确结果
- ✅ 统计数据准确

## 性能要求

- 评论列表加载时间 < 3 秒
- 操作日志加载时间 < 3 秒
- 系统设置加载时间 < 2 秒
- 举报统计加载时间 < 2 秒

## 常见问题

### Q1: 前端页面显示"加载失败"
**原因**: 后端服务未启动或 API 路径配置错误
**解决**: 
1. 检查后端服务是否运行在 8080 端口
2. 检查 vite.config.js 中的代理配置
3. 检查浏览器控制台的网络请求

### Q2: 后端返回 404 错误
**原因**: 控制器未正确注册或包扫描问题
**解决**:
1. 检查控制器是否有 @RestController 和 @RequestMapping 注解
2. 检查主启动类是否能扫描到控制器包路径
3. 重启 Spring Boot 服务

### Q3: 数据库查询返回空数据
**原因**: 测试数据未导入
**解决**:
1. 检查数据库表是否存在
2. 手动插入测试数据
3. 运行初始化 SQL 脚本

### Q4: 中文乱码问题
**原因**: 字符编码不一致
**解决**:
1. 确保数据库使用 utf8mb4 编码
2. 确保 JDBC 连接字符串包含 characterEncoding=utf8
3. 确保 Spring 配置了字符编码过滤器

## 测试报告

测试完成后，请记录以下信息：

- [ ] 评论管理功能测试通过
- [ ] 操作日志功能测试通过
- [ ] 系统设置功能测试通过
- [ ] 举报管理功能测试通过
- [ ] 所有 API 接口响应正常
- [ ] 前端页面展示正常
- [ ] 性能指标达标
- [ ] 无严重 Bug

## 下一步

测试通过后，可以：
1. 部署到测试环境
2. 进行用户验收测试（UAT）
3. 准备上线发布
