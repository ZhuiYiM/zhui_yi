# 管理员系统 - 广告管理和标签管理功能说明

## 概述

本次更新为校园信息平台管理员系统新增了**广告管理**和**标签管理**两大功能模块，完善了内容管理能力。

---

## 一、数据库结构

### 1.1 广告管理数据库

**文件位置**: `database_advertisements.sql`

**表结构**:
```sql
CREATE TABLE `advertisements` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL COMMENT '广告标题',
    `image_url` VARCHAR(500) COMMENT '广告图片 URL',
    `link_url` VARCHAR(500) COMMENT '跳转链接',
    `content` TEXT COMMENT '广告内容描述',
    `position` VARCHAR(50) NOT NULL DEFAULT 'home' COMMENT '广告位置',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `is_active` TINYINT DEFAULT 1 COMMENT '是否启用',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `click_count` INT DEFAULT 0 COMMENT '点击次数',
    `view_count` INT DEFAULT 0 COMMENT '展示次数',
    `created_by` BIGINT COMMENT '创建人 ID',
    `updated_by` BIGINT COMMENT '更新人 ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**字段说明**:
- `position`: 广告位置（home-首页，topicwall-话题墙，mall-交易中心，map-地图）
- `is_active`: 状态（0-禁用，1-启用）
- `click_count` / `view_count`: 自动统计点击和展示次数

### 1.2 标签管理数据库升级

**文件位置**: `database_tags_upgrade.sql`

**新增字段**:
```sql
ALTER TABLE `tags` ADD COLUMN `icon` VARCHAR(100) COMMENT '图标 URL 或类名';
ALTER TABLE `tags` ADD COLUMN `color` VARCHAR(20) COMMENT '标签颜色';
ALTER TABLE `tags` ADD COLUMN `is_hot` TINYINT DEFAULT 0 COMMENT '是否热门';
ALTER TABLE `tags` ADD COLUMN `usage_count` INT DEFAULT 0 COMMENT '使用次数';
ALTER TABLE `tags` ADD COLUMN `created_by` BIGINT COMMENT '创建人 ID';
ALTER TABLE `tags` ADD COLUMN `updated_by` BIGINT COMMENT '更新人 ID';
```

**完善后的 tags 表结构**:
- `level`: 标签层级（1-4）
- `tag_code`: 标签代码（如：B01）
- `tag_name`: 标签名称
- `parent_id`: 父级 ID
- `sort_order`: 排序顺序
- `icon`: 图标
- `color`: 颜色
- `is_hot`: 是否热门
- `status`: 状态（0-禁用，1-启用）

---

## 二、后端接口

### 2.1 广告管理接口

**基础路径**: `/admin/advertisements`

**需要权限**: `admin` 或 `content_admin`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | `/` | 分页查询广告列表 | pageNum, pageSize, title, position, isActive |
| GET | `/{id}` | 获取广告详情 | path: id |
| POST | `/` | 创建广告 | AdvertisementDTO |
| PUT | `/{id}` | 更新广告 | path: id, AdvertisementDTO |
| DELETE | `/{id}` | 删除广告 | path: id |
| DELETE | `/batch` | 批量删除 | ids (逗号分隔) |
| PUT | `/{id}/status` | 更新状态 | isActive |

**AdvertisementDTO 示例**:
```json
{
    "title": "广告标题",
    "imageUrl": "https://...",
    "linkUrl": "/jump/url",
    "content": "广告内容",
    "position": "home",
    "sortOrder": 1,
    "isActive": 1,
    "startTime": "2026-03-23 00:00:00",
    "endTime": "2026-04-23 23:59:59"
}
```

### 2.2 标签管理接口

**基础路径**: `/admin/tags`

**需要权限**: `admin` 或 `content_admin`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | `/` | 分页查询标签列表 | pageNum, pageSize, level, tagName, isHot |
| GET | `/{id}` | 获取标签详情 | path: id |
| POST | `/` | 创建标签 | Tags |
| PUT | `/{id}` | 更新标签 | path: id, Tags |
| DELETE | `/{id}` | 删除标签 | path: id |
| DELETE | `/batch` | 批量删除 | ids (逗号分隔) |
| PUT | `/{id}/status` | 更新状态 | status |
| PUT | `/{id}/hot` | 设置热门 | isHot |

**Tags 实体示例**:
```json
{
    "level": 1,
    "tagCode": "B01",
    "tagName": "教学楼",
    "parentId": 0,
    "icon": "el-icon-building",
    "color": "#4A90E2",
    "sortOrder": 1,
    "isHot": 0,
    "status": 1
}
```

---

## 三、前端页面

### 3.1 广告管理页面

**文件位置**: `src/views/admin/AdvertisementManagement.vue`

**功能特性**:
- ✅ 分页列表展示
- ✅ 按标题、位置、状态筛选
- ✅ 图片预览
- ✅ 新增/编辑广告
- ✅ 一键启用/禁用
- ✅ 删除确认

**访问路径**: `/admin/dashboard/advertisements`

### 3.2 标签管理页面

**文件位置**: `src/views/admin/TagManagement.vue`

**功能特性**:
- ✅ 分页列表展示
- ✅ 按层级、名称、热门状态筛选
- ✅ 颜色选择器
- ✅ 新增/编辑标签
- ✅ 设为热门/取消热门
- ✅ 启用/禁用
- ✅ 层级标识（不同颜色标签）

**访问路径**: `/admin/dashboard/tags`

---

## 四、API 封装

### 4.1 广告管理 API

**文件位置**: `src/api/admin-advertisement.js`

```javascript
import { advertisementAPI } from '@/api/admin-advertisement'

// 查询列表
advertisementAPI.getAdvertisements({ pageNum: 1, pageSize: 10 })

// 创建广告
advertisementAPI.createAdvertisement(adData)

// 更新广告
advertisementAPI.updateAdvertisement(id, adData)

// 删除广告
advertisementAPI.deleteAdvertisement(id)

// 更新状态
advertisementAPI.updateAdvertisementStatus(id, isActive)
```

### 4.2 标签管理 API

**文件位置**: `src/api/admin-tag.js`

```javascript
import { tagAPI } from '@/api/admin-tag'

// 查询列表
tagAPI.getTags({ pageNum: 1, pageSize: 10, level: 1 })

// 创建标签
tagAPI.createTag(tagData)

// 更新标签
tagAPI.updateTag(id, tagData)

// 删除标签
tagAPI.deleteTag(id)

// 设置热门
tagAPI.setHotTag(id, isHot)

// 更新状态
tagAPI.updateTagStatus(id, status)
```

---

## 五、测试用例

### 5.1 广告管理测试

**文件位置**: `tests/advertisement.admin.test.js`

**测试覆盖**:
1. ✓ 查询广告列表
2. ✓ 创建广告
3. ✓ 获取广告详情
4. ✓ 更新广告
5. ✓ 更新广告状态
6. ✓ 删除广告

**运行方式**:
```bash
# 确保后端服务已启动
node tests/advertisement.admin.test.js
```

### 5.2 标签管理测试

**文件位置**: `tests/tag.admin.test.js`

**测试覆盖**:
1. ✓ 查询标签列表
2. ✓ 创建一级标签
3. ✓ 创建二级标签
4. ✓ 获取标签详情
5. ✓ 更新标签
6. ✓ 设置热门标签
7. ✓ 更新标签状态
8. ✓ 删除标签

**运行方式**:
```bash
# 确保后端服务已启动
node tests/tag.admin.test.js
```

---

## 六、快速开始

### 6.1 数据库初始化

```bash
# 1. 执行广告表创建脚本
mysql -u root -p campus_db < database_advertisements.sql

# 2. 执行标签表升级脚本
mysql -u root -p campus_db < database_tags_upgrade.sql
```

### 6.2 启动服务

```bash
# 1. 启动后端服务
cd src/main/java/com/example/demo
# 运行 DemoApplication.java

# 2. 启动前端开发服务器
npm run dev
```

### 6.3 访问管理后台

1. 访问：`http://localhost:5173/admin/login`
2. 登录账号：`admin` / `admin123`
3. 进入管理后台后，在左侧菜单找到 **内容管理**
4. 点击 **广告管理** 或 **标签管理** 即可使用

---

## 七、菜单结构

在管理员后台的 **内容管理** 子菜单中，新增了：

```
内容管理
├── 话题管理
├── 商品管理
├── 地点管理
├── 评论管理
├── 广告管理  ← 新增
└── 标签管理  ← 新增
```

---

## 八、注意事项

### 8.1 权限要求

- 两个功能都需要 `admin` 或 `content_admin` 角色
- 未授权用户访问会返回 403 错误

### 8.2 数据验证

**广告管理**:
- 标题必填（最多 200 字符）
- 位置必填
- 图片 URL 可选
- 时间范围可选

**标签管理**:
- 层级必填（1-4）
- 名称必填（最多 100 字符）
- 代码可选
- 颜色可选（默认 #409EFF）

### 8.3 性能优化建议

1. **广告表**: 建议定期清理过期广告
2. **标签表**: 建议使用缓存存储热门标签
3. **图片**: 建议使用 CDN 存储广告图片

---

## 九、扩展建议

### 9.1 广告管理扩展

- [ ] 增加广告模板功能
- [ ] 支持上传本地图片（目前仅支持 URL）
- [ ] 增加广告投放时间段控制
- [ ] 增加广告效果统计报表

### 9.2 标签管理扩展

- [ ] 增加标签导入导出功能
- [ ] 支持标签树形结构展示
- [ ] 增加标签使用统计
- [ ] 自动推荐相关标签

---

## 十、故障排查

### 常见问题

**Q1: 访问广告管理页面 404**
- 检查路由配置是否正确添加
- 检查组件文件是否存在

**Q2: 创建标签失败**
- 检查 tags 表是否已执行升级脚本
- 检查必填字段是否完整

**Q3: 图片无法显示**
- 检查 imageUrl 是否为有效 URL
- 检查 CORS 配置

**Q4: 测试用例运行失败**
- 确保后端服务已启动（端口 8080）
- 确保 admin 账号密码正确
- 检查 Token 是否过期

---

## 十一、技术栈

- **后端**: Spring Boot 3.2.0 + MyBatis-Plus
- **前端**: Vue 3 + Element Plus + Vite
- **数据库**: MySQL 8.0
- **认证**: JWT Token

---

## 十二、版本历史

**v1.0.0 - 2026-03-23**
- ✅ 新增广告管理功能
- ✅ 新增标签管理功能
- ✅ 完善数据库结构
- ✅ 编写完整测试用例

---

**维护者**: 校园信息平台开发团队  
**最后更新**: 2026-03-23
