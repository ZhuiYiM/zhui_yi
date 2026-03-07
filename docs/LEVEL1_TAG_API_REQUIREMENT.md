# 一级标签（用户身份）接口需求说明

## 问题描述

当前前端在发布新话题时，一级身份标签持续显示"正在加载身份标签..."，无法正常显示。

**根本原因**：后端 `/api/topic/tags/level1` 接口可能未实现或返回数据格式不符合预期。

---

## 功能需求

### 业务规则

1. **自动显示规则**：发布话题时，系统应根据用户身份自动确定并直接显示对应的一级标签
2. **只读模式**：一级标签在前端显示为只读状态，用户无法手动更改
3. **优先级逻辑**：管理员 > 学生 > 商户 > 团体 > 个人 > 社会

### 使用场景

- **场景 1**：用户进入发布话题页面 → 系统自动获取用户身份 → 显示对应的一级标签
- **场景 2**：后端返回所有一级标签列表 → 前端根据用户身份自动选中对应的标签

---

## 接口规范

### 1. 获取一级标签列表

**接口地址**：`GET /api/topic/tags/level1`

**功能描述**：获取所有可用的一级标签（用户身份类型）

**请求参数**：无

**响应格式**：

```json
[
  {
    "code": "student",
    "name": "认证学生",
    "icon": "👨‍🎓",
    "isActive": true,
    "sort": 1
  },
  {
    "code": "merchant",
    "name": "校外商户",
    "icon": "🏪",
    "isActive": true,
    "sort": 2
  },
  {
    "code": "organization",
    "name": "认证团体",
    "icon": "👥",
    "isActive": true,
    "sort": 3
  },
  {
    "code": "individual",
    "name": "个人",
    "icon": "👤",
    "isActive": true,
    "sort": 4
  },
  {
    "code": "society",
    "name": "社会",
    "icon": "🌐",
    "isActive": true,
    "sort": 5
  },
  {
    "code": "admin",
    "name": "管理员",
    "icon": "🛡️",
    "isActive": true,
    "sort": 0
  }
]
```

**字段说明**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | String | 是 | 标签代码标识（如：student, merchant, society） |
| name | String | 是 | 标签显示名称（如：认证学生，校外商户） |
| icon | String | 否 | Emoji 图标，如前端未提供则使用后端返回的 |
| isActive | Boolean | 是 | 是否启用，false 表示该标签不可用 |
| sort | Number | 否 | 排序权重，数值越小越靠前 |

---

### 2. 获取当前用户身份（建议新增）

**接口地址**：`GET /api/user/identity`

**功能描述**：获取当前登录用户的身份类型（用于更精确的身份识别）

**请求参数**：无（从 Token 中获取用户信息）

**响应格式**：

```json
{
  "userId": 123,
  "identity": "student",
  "realName": "张三",
  "studentId": "2021001",
  "verified": true
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Number | 用户 ID |
| identity | String | 用户身份代码（student/merchant/organization/individual/society/admin） |
| realName | String | 真实姓名 |
| studentId | String | 学号（仅学生有） |
| verified | Boolean | 是否已实名认证 |

---

## 前端处理逻辑

### 当前实现方式（临时方案）

前端目前通过 localStorage 中的用户信息判断身份：

```javascript
// 优先级：管理员 > 学生 > 商户 > 团体 > 个人 > 社会
const user = JSON.parse(localStorage.getItem('user') || '{}');

if (user.role === 'admin' || user.isAdmin) {
  selectedTag = level1Tags.value.find(t => t.code === 'admin');
} else if (user.studentId) {
  selectedTag = level1Tags.value.find(t => t.code === 'student');
} else if (user.isMerchant) {
  selectedTag = level1Tags.value.find(t => t.code === 'merchant');
} else if (user.isOrganization) {
  selectedTag = level1Tags.value.find(t => t.code === 'organization');
} else {
  // 默认为社会用户
  selectedTag = level1Tags.value.find(t => t.code === 'society') || level1Tags.value[0];
}
```

### 期望的实现方式（推荐）

1. 前端调用 `GET /api/topic/tags/level1` 获取所有一级标签
2. 前端调用 `GET /api/user/identity` 获取当前用户身份
3. 前端根据返回的 identity 字段自动匹配并显示对应标签

---

## 数据库设计建议

### tags 表（标签表）

```sql
CREATE TABLE `tags` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `code` VARCHAR(50) NOT NULL COMMENT '标签代码',
  `name` VARCHAR(100) NOT NULL COMMENT '标签名称',
  `level` TINYINT NOT NULL COMMENT '标签级别 (1:一级标签，2:二级标签...)',
  `icon` VARCHAR(50) COMMENT 'Emoji 图标',
  `is_active` BOOLEAN DEFAULT TRUE COMMENT '是否启用',
  `sort` INT DEFAULT 0 COMMENT '排序权重',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父标签 ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_code_level` (`code`, `level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';
```

### 初始化数据

```sql
-- 一级标签（用户身份）
INSERT INTO `tags` (`code`, `name`, `level`, `icon`, `is_active`, `sort`) VALUES
('admin', '管理员', 1, '🛡️', TRUE, 0),
('student', '认证学生', 1, '👨‍🎓', TRUE, 1),
('merchant', '校外商户', 1, '🏪', TRUE, 2),
('organization', '认证团体', 1, '👥', TRUE, 3),
('individual', '个人', 1, '👤', TRUE, 4),
('society', '社会', 1, '🌐', TRUE, 5);
```

---

## 排查步骤

### 后端需要检查：

1. ✅ 是否存在 `/api/topic/tags/level1` 接口
2. ✅ 接口是否能正常返回数据
3. ✅ 返回的数据格式是否符合上述规范
4. ✅ `tags` 表是否存在且包含一级标签数据
5. ✅ `isActive` 字段是否正确设置

### 前端调试方法：

打开浏览器控制台，查看以下日志：

```
📥 TagSelector - Level1 原始响应：{响应数据}
✅ TagSelector - Level1 标签已加载：[标签数组]
```

如果看到：
- ❌ 网络请求失败 → 接口不存在或 CORS 问题
- ✅ 但标签列表为空 → 后端返回空数组或数据过滤问题
- ⚠️ 标签已加载但仍显示加载中 → `autoSelectIdentity` 逻辑问题

---

## 测试用例

### 测试场景 1：普通学生用户

**前置条件**：
- 用户已登录
- 用户信息包含学号字段

**期望结果**：
- 一级标签自动显示为"认证学生" 👨‍🎓
- 标签为只读状态，不可修改

### 测试场景 2：社会用户

**前置条件**：
- 用户已登录
- 用户无学号、非商户、非团体

**期望结果**：
- 一级标签自动显示为"社会" 🌐
- 标签为只读状态，不可修改

### 测试场景 3：未登录用户

**前置条件**：
- 用户未登录

**期望结果**：
- 提示用户先登录
- 或一级标签显示为空，要求用户手动选择（如果可以发布）

---

## 紧急修复建议

如果后端接口暂时无法完成，可以采用以下临时方案：

### 方案 1：前端硬编码（当前使用）

```javascript
// 在 TagSelector.vue 中已经实现
// 优点：快速生效
// 缺点：不够灵活，需要后端配合才能实现动态配置
```

### 方案 2：后端返回简化版

即使后端无法实现完整的身份识别，至少应该：

```javascript
// GET /api/topic/tags/level1
// 返回所有启用的标签即可
[
  { "code": "student", "name": "认证学生", "isActive": true },
  { "code": "society", "name": "社会", "isActive": true }
]
```

前端会根据 localStorage 中的用户信息自动选择。

---

## 联系方式

如有任何疑问，请查看前端代码：
- 文件位置：`src/components/topic/TagSelector.vue`
- 关键方法：`loadLevel1Tags()`, `autoSelectIdentity()`
- 接口调用：`tagAPI.getLevel1Tags()`

---

**文档版本**：v1.0  
**更新时间**：2026-03-07  
**优先级**：高（影响发布话题功能）
