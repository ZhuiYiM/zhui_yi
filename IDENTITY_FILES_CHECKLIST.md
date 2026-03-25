# 身份认证系统 - 文件清单

## 📁 新增文件列表

### 后端核心文件 (Java)

#### 实体类
- `src/main/java/com/example/demo/entity/UserIdentity.java` - 用户身份实体 ⭐⭐⭐⭐⭐
- `src/main/java/com/example/demo/entity/User.java` - 更新（添加 isStaff 字段）

#### Mapper
- `src/main/java/com/example/demo/mapper/UserIdentityMapper.java` - MyBatis-Plus Mapper ⭐⭐⭐⭐

#### 服务层
- `src/main/java/com/example/demo/service/UserIdentityService.java` - 服务接口 ⭐⭐⭐⭐⭐
- `src/main/java/com/example/demo/service/impl/UserIdentityServiceImpl.java` - 服务实现 ⭐⭐⭐⭐⭐

#### 注解
- `src/main/java/com/example/demo/annotation/RequireIdentity.java` - 身份权限注解 ⭐⭐⭐⭐⭐
- `src/main/java/com/example/demo/annotation/RequireRole.java` - 角色权限注解 ⭐⭐⭐⭐⭐

#### AOP 切面
- `src/main/java/com/example/demo/aspect/PermissionAspect.java` - 权限检查切面 ⭐⭐⭐⭐⭐

#### 工具类
- `src/main/java/com/example/demo/utils/JwtUtil.java` - 更新（增强 Token 功能） ⭐⭐⭐⭐⭐

#### 控制器
- `src/main/java/com/example/demo/controller/UserIdentityController.java` - 用户身份 API ⭐⭐⭐⭐⭐
- `src/main/java/com/example/demo/controller/admin/AdminIdentityController.java` - 管理员审核 API ⭐⭐⭐⭐⭐

---

### 前端核心文件 (Vue/JS)

#### 状态管理
- `src/stores/auth.js` - 更新（添加身份信息管理） ⭐⭐⭐⭐⭐

#### API 接口
- `src/api/user.js` - 更新（添加身份认证和管理员接口） ⭐⭐⭐⭐⭐

#### UI 组件
- `src/components/common/IdentityBadges.vue` - 身份徽章展示 ⭐⭐⭐⭐
- `src/components/user/IdentityVerificationForm.vue` - 身份认证申请表单 ⭐⭐⭐⭐⭐
- `src/views/admin/IdentityVerificationList.vue` - 管理员审核页面 ⭐⭐⭐⭐⭐

#### 路由配置
- `src/router/index.js` - 更新（添加审核路由） ⭐⭐⭐⭐

---

### 数据库文件 (SQL)

#### 初始化脚本
- `database_user_identity.sql` - 完整的数据库初始化脚本 ⭐⭐⭐⭐⭐
  - 创建 user_identity 表
  - 添加 users.is_staff 字段
  - 插入测试数据
  - 创建视图、存储过程、触发器

#### 验证脚本
- `verify_identity_system.sql` - 快速验证 SQL 查询 ⭐⭐⭐⭐
  - 查看所有身份
  - 待审核申请
  - 已认证身份
  - 统计信息

---

### 文档文件 (Markdown)

#### 完整文档
- `IDENTITY_SYSTEM_COMPLETE.md` - 系统完成总结报告 ⭐⭐⭐⭐⭐
  - 已完成功能清单
  - 技术栈说明
  - 核心特性
  - 使用示例
  - 下一步建议

#### 快速指南
- `IDENTITY_QUICKSTART.md` - 5 分钟快速上手指南 ⭐⭐⭐⭐⭐
  - 数据库初始化
  - 启动步骤
  - 测试场景
  - 常见问题

---

## 📊 文件统计

| 类型 | 数量 | 重要程度 |
|------|------|----------|
| Java 源文件 | 9 | ⭐⭐⭐⭐⭐ |
| Vue 组件 | 3 | ⭐⭐⭐⭐⭐ |
| JavaScript 文件 | 2 | ⭐⭐⭐⭐⭐ |
| SQL 脚本 | 2 | ⭐⭐⭐⭐⭐ |
| Markdown 文档 | 2 | ⭐⭐⭐⭐ |
| **总计** | **18** | - |

---

## 🔍 核心文件说明

### ⭐⭐⭐⭐⭐ 关键文件（必须理解）

1. **UserIdentity.java** - 数据模型基础
2. **UserIdentityService.java** - 业务逻辑核心
3. **UserIdentityServiceImpl.java** - 具体实现
4. **JwtUtil.java** - Token 认证机制
5. **RequireIdentity.java** - 身份权限控制
6. **RequireRole.java** - 角色权限控制
7. **PermissionAspect.java** - AOP 自动拦截
8. **IdentityVerificationForm.vue** - 用户申请入口
9. **IdentityVerificationList.vue** - 管理员审核入口
10. **database_user_identity.sql** - 数据库基础

### ⭐⭐⭐⭐ 重要文件（经常使用）

1. **UserIdentityMapper.java** - 数据访问
2. **UserIdentityController.java** - 用户 API
3. **AdminIdentityController.java** - 管理 API
4. **auth.js** - 状态管理
5. **user.js** - API 封装
6. **IdentityBadges.vue** - 身份展示
7. **verify_identity_system.sql** - 验证查询

---

## 🎯 学习路径

### 第一步：理解数据模型
```
阅读顺序:
1. entity/UserIdentity.java - 了解数据结构
2. database_user_identity.sql - 了解表设计
```

### 第二步：理解业务流程
```
阅读顺序:
1. service/UserIdentityService.java - 了解业务能力
2. controller/UserIdentityController.java - 了解 API 设计
3. IdentityVerificationForm.vue - 了解用户操作
```

### 第三步：理解权限控制
```
阅读顺序:
1. annotation/RequireIdentity.java - 了解身份注解
2. annotation/RequireRole.java - 了解角色注解
3. aspect/PermissionAspect.java - 了解 AOP 实现
4. utils/JwtUtil.java - 了解 Token 机制
```

### 第四步：理解前后端交互
```
阅读顺序:
1. api/user.js - 了解前端 API 封装
2. stores/auth.js - 了解状态管理
3. components/common/IdentityBadges.vue - 了解组件使用
```

---

## 🛠️ 修改建议

### 高频修改文件
这些文件在后续开发中可能需要经常修改:

1. **IdentityVerificationForm.vue** - 添加新的身份类型字段
2. **UserIdentityServiceImpl.java** - 调整业务逻辑
3. **admin/IdentityVerificationList.vue** - 优化审核流程
4. **database_user_identity.sql** - 添加新的测试数据

### 低频修改文件
这些文件一般不需要修改:

1. **entity/UserIdentity.java** - 数据结构稳定
2. **Mapper 接口** - MyBatis-Plus 自动生成
3. **注解定义** - 权限机制稳定
4. **AOP 切面** - 通用逻辑

---

## 📝 依赖关系图

```
UserIdentity (实体)
    ↓
UserIdentityMapper (数据访问)
    ↓
UserIdentityService (业务接口)
    ↓
UserIdentityServiceImpl (业务实现)
    ↓
UserIdentityController (API 暴露)
    ↓
api/user.js (前端调用)
    ↓
stores/auth.js (状态管理)
    ↓
IdentityBadges.vue (UI 展示)
```

---

## 🚀 快速定位

### 需要修改数据库？
→ `database_user_identity.sql`

### 需要修改业务逻辑？
→ `service/impl/UserIdentityServiceImpl.java`

### 需要修改 API?
→ `controller/UserIdentityController.java` 或 `AdminIdentityController.java`

### 需要修改界面？
→ `components/user/IdentityVerificationForm.vue` 或 `views/admin/IdentityVerificationList.vue`

### 需要修改权限规则？
→ `annotation/RequireIdentity.java` 或 `aspect/PermissionAspect.java`

### 需要添加测试数据？
→ `database_user_identity.sql` 中的 INSERT 语句

---

所有文件都已创建并编译通过！✅
