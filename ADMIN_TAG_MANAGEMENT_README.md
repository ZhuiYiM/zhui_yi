# 管理员标签管理系统文档

## 系统概述

管理员标签管理系统提供了对四大类标签的全面管理功能：
1. **身份标签**（一级标签）- 用户身份标识
2. **话题标签**（二级标签）- 话题内容分类
3. **商品标签**（三/五级标签）- 商品分类管理
4. **地点标签**（四级标签）- 地理位置标记

所有标签类型都支持系统标签和用户自定义标签，其中用户自定义标签需要管理员审核后才能公开使用。

## 数据库表结构

### 1. 身份标签表（identity_tags）

```sql
CREATE TABLE `identity_tags` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '标签代码',
  `name` VARCHAR(100) NOT NULL COMMENT '标签名称',
  `icon` VARCHAR(50) COMMENT '图标',
  `description` VARCHAR(500) COMMENT '描述',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**特点：**
- 仅支持系统标签，不支持用户自定义
- 使用 `is_active` 字段控制启用/禁用状态
- 无需审核流程

### 2. 话题标签表（topic_tag）

```sql
CREATE TABLE `topic_tag` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '标签名称/代码',
  `code` VARCHAR(50) COMMENT '标签代码',
  `type` VARCHAR(20) NOT NULL DEFAULT 'custom' COMMENT '标签类型：system/custom',
  `usage_count` BIGINT DEFAULT 0 COMMENT '使用次数',
  `trend_score` DOUBLE DEFAULT 0.0 COMMENT '趋势分数',
  `last_used_at` DATETIME COMMENT '最后使用时间',
  `created_by` BIGINT COMMENT '创建者用户 ID（用户自定义标签）',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态：active/banned/pending',
  `icon` VARCHAR(255) COMMENT '图标 URL',
  `color` VARCHAR(20) COMMENT '标签颜色',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否启用',
  `description` VARCHAR(500) COMMENT '补充说明',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_name` (`name`),
  INDEX `idx_type` (`type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_usage` (`usage_count` DESC),
  INDEX `idx_trend` (`trend_score` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**特点：**
- 支持系统标签（type='system'）和用户自定义标签（type='custom'）
- 用户自定义标签需要审核（status='pending' → 'active'）
- 包含使用次数和趋势分数，用于热门排序
- 索引优化查询性能

### 3. 商品标签表（product_tag）

```sql
CREATE TABLE `product_tag` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '标签名称/代码',
  `code` VARCHAR(50) COMMENT '标签代码',
  `type` VARCHAR(20) NOT NULL DEFAULT 'custom' COMMENT '标签类型：system/custom',
  `category` VARCHAR(50) COMMENT '分类（用于商品标签）',
  `usage_count` BIGINT DEFAULT 0 COMMENT '使用次数',
  `trend_score` DOUBLE DEFAULT 0.0 COMMENT '趋势分数',
  `last_used_at` DATETIME COMMENT '最后使用时间',
  `created_by` BIGINT COMMENT '创建者用户 ID（用户自定义标签）',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态：active/banned/pending',
  `icon` VARCHAR(255) COMMENT '图标 URL',
  `color` VARCHAR(20) COMMENT '标签颜色',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否启用',
  `description` VARCHAR(500) COMMENT '补充说明',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_name` (`name`),
  INDEX `idx_type` (`type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_category` (`category`),
  INDEX `idx_usage` (`usage_count` DESC),
  INDEX `idx_trend` (`trend_score` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**特点：**
- 支持系统标签和用户自定义标签
- 独有的 `category` 字段用于商品分类
- 用户自定义标签需要审核
- 状态管理：active（通过）、pending（待审核）、banned（禁用）

### 4. 地点标签表（location_tag）

```sql
CREATE TABLE `location_tag` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '标签名称/代码',
  `type` VARCHAR(20) NOT NULL DEFAULT 'custom' COMMENT '标签类型：system/custom',
  `usage_count` BIGINT DEFAULT 0 COMMENT '使用次数',
  `trend_score` DOUBLE DEFAULT 0.0 COMMENT '趋势分数',
  `last_used_at` DATETIME COMMENT '最后使用时间',
  `created_by` BIGINT COMMENT '创建者用户 ID（用户自定义标签）',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态：active/banned/pending',
  `icon` VARCHAR(255) COMMENT '图标 URL',
  `color` VARCHAR(20) COMMENT '标签颜色',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_name` (`name`),
  INDEX `idx_type` (`type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_usage` (`usage_count` DESC),
  INDEX `idx_trend` (`trend_score` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**特点：**
- 支持系统标签和用户自定义标签
- 用户自定义标签需要审核
- 与话题标签类似的结构

## API 接口文档

### 基础路径
```
/api/admin/tags
```

### 身份标签管理

#### 1. 分页查询身份标签列表
```http
GET /api/admin/tags/identity
```

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页数量，默认 10 |
| keyword | String | 否 | 搜索关键词（名称或代码） |
| isActive | Boolean | 否 | 状态筛选 |

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 10,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

#### 2. 创建身份标签
```http
POST /api/admin/tags/identity
Content-Type: application/json

{
  "code": "student",
  "name": "学生",
  "icon": "👨‍🎓",
  "description": "在校学生",
  "sortOrder": 1,
  "isActive": true
}
```

#### 3. 更新身份标签
```http
PUT /api/admin/tags/identity/{id}
Content-Type: application/json

{
  "name": "学生",
  "icon": "👨‍🎓",
  "description": "在校学生",
  "sortOrder": 1,
  "isActive": true
}
```

#### 4. 删除身份标签
```http
DELETE /api/admin/tags/identity/{id}
```

#### 5. 批量删除身份标签
```http
DELETE /api/admin/tags/identity/batch?ids=1,2,3
```

#### 6. 更新身份标签状态
```http
PUT /api/admin/tags/identity/{id}/status
Content-Type: application/json

{
  "isActive": false
}
```

---

### 话题标签管理

#### 1. 分页查询话题标签列表
```http
GET /api/admin/tags/topic
```

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页数量，默认 10 |
| keyword | String | 否 | 搜索关键词 |
| isActive | Boolean | 否 | 状态筛选 |
| type | String | 否 | 类型筛选（system/custom） |

#### 2. 创建话题标签
```http
POST /api/admin/tags/topic
Content-Type: application/json

{
  "name": "学习交流",
  "code": "study_exchange",
  "type": "system",
  "icon": "📚",
  "color": "#4A90E2",
  "sortOrder": 1,
  "isActive": true
}
```

#### 3. 更新话题标签
```http
PUT /api/admin/tags/topic/{id}
Content-Type: application/json

{
  "name": "学习交流",
  "icon": "📚",
  "color": "#4A90E2",
  "sortOrder": 1,
  "isActive": true,
  "type": "system"
}
```

#### 4. 删除话题标签
```http
DELETE /api/admin/tags/topic/{id}
```

#### 5. 批量删除话题标签
```http
DELETE /api/admin/tags/topic/batch?ids=1,2,3
```

#### 6. 更新话题标签状态
```http
PUT /api/admin/tags/topic/{id}/status
Content-Type: application/json

{
  "isActive": false
}
```

---

### 地点标签管理

#### 1. 分页查询地点标签列表
```http
GET /api/admin/tags/location
```

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页数量，默认 10 |
| keyword | String | 否 | 搜索关键词 |
| isActive | Boolean | 否 | 状态筛选 |
| type | String | 否 | 类型筛选（system/custom） |

#### 2. 创建地点标签
```http
POST /api/admin/tags/location
Content-Type: application/json

{
  "name": "图书馆",
  "type": "system",
  "icon": "📚",
  "color": "#4A90E2",
  "sortOrder": 1
}
```

#### 3. 更新地点标签
```http
PUT /api/admin/tags/location/{id}
Content-Type: application/json

{
  "name": "图书馆",
  "type": "system",
  "icon": "📚",
  "color": "#4A90E2",
  "sortOrder": 1,
  "isActive": true
}
```

#### 4. 删除地点标签
```http
DELETE /api/admin/tags/location/{id}
```

#### 5. 批量删除地点标签
```http
DELETE /api/admin/tags/location/batch?ids=1,2,3
```

#### 6. 更新地点标签状态
```http
PUT /api/admin/tags/location/{id}/status
Content-Type: application/json

{
  "isActive": false
}
```

---

### 商品标签管理

#### 1. 分页查询商品标签列表
```http
GET /api/admin/tags/product
```

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页数量，默认 10 |
| keyword | String | 否 | 搜索关键词 |
| status | String | 否 | 状态筛选（active/pending/banned） |
| category | String | 否 | 分类筛选 |
| type | String | 否 | 类型筛选（system/custom） |

#### 2. 创建商品标签
```http
POST /api/admin/tags/product
Content-Type: application/json

{
  "name": "二手教材",
  "code": "secondhand_books",
  "type": "system",
  "category": "all",
  "icon": "📚",
  "color": "#4A90E2",
  "sortOrder": 1,
  "status": "active"
}
```

#### 3. 更新商品标签
```http
PUT /api/admin/tags/product/{id}
Content-Type: application/json

{
  "name": "二手教材",
  "code": "secondhand_books",
  "category": "all",
  "icon": "📚",
  "color": "#4A90E2",
  "sortOrder": 1,
  "status": "active",
  "type": "system"
}
```

#### 4. 删除商品标签
```http
DELETE /api/admin/tags/product/{id}
```

#### 5. 批量删除商品标签
```http
DELETE /api/admin/tags/product/batch?ids=1,2,3
```

#### 6. 更新商品标签状态
```http
PUT /api/admin/tags/product/{id}/status
Content-Type: application/json

{
  "status": "banned"
}
```

**注意：** 商品标签使用 `status` 字段（字符串），其他标签使用 `isActive` 字段（布尔值）

---

## 前端管理页面

### 访问路径
```
/admin/tags
```

### 功能特性

1. **标签分类切换**
   - 顶部下拉框选择标签类型：身份标签、话题标签、地点标签、商品标签
   - 自动切换对应的数据列表和操作

2. **搜索功能**
   - 支持关键词搜索（标签名称或代码）
   - 支持状态筛选（启用/禁用/待审核）

3. **标签操作**
   - 新增标签：根据标签类型显示对应的表单字段
   - 编辑标签：修改标签信息
   - 启用/禁用：快速切换标签状态
   - 删除标签：单个删除或批量删除

4. **状态显示**
   - 身份/话题/地点标签：显示"启用"或"禁用"
   - 商品标签：显示"启用"、"待审核"或"禁用"

5. **分页功能**
   - 支持每页 10/20/50/100 条记录
   - 显示总记录数
   - 快速跳转页面

### 使用说明

1. **查看标签列表**
   - 选择标签分类
   - 系统自动加载对应类型的标签列表

2. **搜索标签**
   - 输入关键词
   - 选择状态（可选）
   - 点击"查询"按钮

3. **新增标签**
   - 点击"新增标签"按钮
   - 填写标签信息
   - 点击"确定"提交

4. **编辑标签**
   - 点击标签行的"编辑"按钮
   - 修改标签信息
   - 点击"确定"保存

5. **启用/禁用标签**
   - 点击标签行的"启用"或"禁用"按钮
   - 确认操作

6. **删除标签**
   - 点击标签行的"删除"按钮
   - 确认删除

7. **批量删除**
   - 勾选多个标签
   - 点击"批量删除"按钮
   - 确认删除

---

## 审核流程

### 用户自定义标签提交流程

1. 用户在前端发布话题/商品/地点时，可以创建自定义标签
2. 系统自动将新标签插入到对应的标签表
3. 设置 `type='custom'`，`status='pending'`，`is_active=0`
4. 标签进入待审核状态

### 管理员审核流程

1. 管理员登录后台管理系统
2. 访问标签管理页面
3. 切换到对应的标签分类
4. 查看状态为"待审核"的标签
5. 点击"通过"或"禁用"按钮进行审核

**审核通过：**
```http
PUT /api/admin/tags/product/{id}/status
{
  "status": "active"
}
```

**审核禁用：**
```http
PUT /api/admin/tags/product/{id}/status
{
  "status": "banned"
}
```

### 转为系统标签

管理员可以将优质的用户自定义标签转为系统标签：

```http
POST /api/tags/{type}/{id}/convert
```

**参数说明：**
- `type`: 标签类型（topic/product/location）
- `id`: 标签 ID

**后端处理逻辑：**
1. 将 `type` 改为 `'system'`
2. 将 `status` 改为 `'active'`
3. 将 `is_active` 设为 `1`
4. 清空 `created_by` 字段

---

## 数据统计

### 热门标签统计

系统会自动统计各类型标签的使用次数和趋势分数：

**话题热门标签：**
```sql
SELECT id, name, code, usage_count, trend_score
FROM topic_tag
WHERE is_active = 1
ORDER BY usage_count DESC, trend_score DESC
LIMIT 10;
```

**商品热门标签：**
```sql
SELECT id, name, code, category, usage_count, trend_score
FROM product_tag
WHERE is_active = 1
ORDER BY usage_count DESC, trend_score DESC
LIMIT 10;
```

**地点热门标签：**
```sql
SELECT id, name, type, usage_count, trend_score
FROM location_tag
WHERE is_active = 1
ORDER BY usage_count DESC, trend_score DESC
LIMIT 10;
```

### 使用情况统计

**统计各类型标签数量：**
```sql
SELECT 
    '身份标签' AS tag_type,
    COUNT(*) AS total_count,
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) AS active_count
FROM identity_tags
UNION ALL
SELECT 
    '话题标签' AS tag_type,
    COUNT(*) AS total_count,
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) AS active_count
FROM topic_tag
UNION ALL
SELECT 
    '商品标签' AS tag_type,
    COUNT(*) AS total_count,
    SUM(CASE WHEN status = 'active' THEN 1 ELSE 0 END) AS active_count,
    SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END) AS pending_count,
    SUM(CASE WHEN status = 'banned' THEN 1 ELSE 0 END) AS banned_count
FROM product_tag
UNION ALL
SELECT 
    '地点标签' AS tag_type,
    COUNT(*) AS total_count,
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) AS active_count
FROM location_tag;
```

---

## 测试脚本

### 初始化数据
```bash
Get-Content database_admin_tag_management.sql | mysql -u root -p123456
```

### 运行测试
```bash
Get-Content test_admin_tag_management.sql | mysql -u root -p123456
```

### 查看统计
```sql
source test_admin_tag_management.sql;
```

---

## 常见问题

### Q1: 为什么商品标签使用 status 字段，而其他标签使用 isActive？

**答：** 商品标签需要支持三态：active（通过）、pending（待审核）、banned（禁用），而其他标签只需要两态（启用/禁用）。为了更清晰地表达审核状态，商品标签采用 status 字符串字段。

### Q2: 如何批量导入系统标签？

**答：** 可以通过 SQL 脚本直接插入：
```sql
INSERT INTO topic_tag (name, code, type, status, is_active, sort_order) VALUES
('学习', 'study', 'system', 'active', 1, 1),
('生活', 'life', 'system', 'active', 1, 2);
```

### Q3: 用户自定义标签审核后如何通知用户？

**答：** 可以在 `TagManagementService` 的审核方法中添加消息通知逻辑，通过站内信或邮件通知用户审核结果。

### Q4: 如何防止用户创建重复的自定义标签？

**答：** 在创建标签前先查询是否存在同名标签：
```java
LambdaQueryWrapper<TopicTag> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(TopicTag::getName, tagName);
Long count = topicTagMapper.selectCount(wrapper);
if (count > 0) {
    return ApiResult.error("标签已存在");
}
```

---

## 后续优化建议

1. **标签推荐算法**
   - 基于用户历史行为推荐相关标签
   - 智能提示相似标签，减少重复创建

2. **标签合并功能**
   - 允许管理员将多个相似标签合并为一个
   - 自动更新关联的话题/商品/地点

3. **标签使用分析**
   - 统计标签的日活/周活/月活
   - 分析标签的趋势变化

4. **标签层级关系**
   - 支持父子标签关系
   - 实现标签分类树

5. **批量审核功能**
   - 支持批量通过/拒绝待审核标签
   - 提高审核效率

6. **标签举报机制**
   - 用户可以举报不当标签
   - 管理员优先处理被举报标签

---

## 相关文件清单

### 后端文件
- `/src/main/java/com/example/demo/controller/admin/AdminTagController.java` - 标签管理控制器
- `/src/main/java/com/example/demo/service/tags/TagManagementService.java` - 标签管理服务接口
- `/src/main/java/com/example/demo/service/tags/impl/TagManagementServiceImpl.java` - 服务实现
- `/src/main/java/com/example/demo/entity/IdentityTags.java` - 身份标签实体
- `/src/main/java/com/example/demo/entity/TopicTag.java` - 话题标签实体
- `/src/main/java/com/example/demo/entity/ProductTag.java` - 商品标签实体
- `/src/main/java/com/example/demo/entity/LocationTag.java` - 地点标签实体

### 前端文件
- `/src/views/admin/TagManagement.vue` - 标签管理页面
- `/src/api/admin.js` - 管理员 API 接口

### 数据库文件
- `/database_admin_tag_management.sql` - 初始化和完善脚本
- `/test_admin_tag_management.sql` - 测试脚本

---

## 版本历史

### v1.0 (2026-03-25)
- ✅ 完成四大标签表结构设计
- ✅ 实现标签 CRUD 基础功能
- ✅ 支持用户自定义标签和审核流程
- ✅ 前端管理页面开发完成
- ✅ 添加数据库索引优化查询性能
- ✅ 完善 API 接口文档
