# 管理员标签管理系统 - 快速上手指南

## 📋 系统概述

管理员标签管理系统提供了对四大类标签的全面管理功能，支持系统标签的维护和用户自定义标签的审核。

**支持的标签类型：**
1. **身份标签**（一级）- 学生、商户、部门等身份标识
2. **话题标签**（二级）- 学习、生活、活动等话题分类
3. **商品标签**（三/五级）- 二手物品、图书、数码等商品分类
4. **地点标签**（四级）- 图书馆、教学楼、宿舍等位置标记

## 🚀 快速开始

### 1. 数据库准备

执行初始化脚本：
```bash
Get-Content database_admin_tag_management.sql | mysql -u root -p123456
```

验证系统状态：
```bash
Get-Content verify_admin_tag_system.sql | mysql -u root -p123456
```

### 2. 启动服务

**后端启动：**
```bash
mvn spring-boot:run
```

**前端启动：**
```bash
npm install
npm run dev
```

### 3. 访问管理后台

浏览器访问：`http://localhost:5173/admin/tags`

使用管理员账号登录后即可开始管理标签。

## 📊 主要功能

### 3.1 标签分类管理

在管理页面顶部选择标签类型：
- **身份标签** - 仅支持系统标签，无需审核
- **话题标签** - 支持系统标签和用户自定义标签
- **商品标签** - 支持系统标签和用户自定义标签（需要审核）
- **地点标签** - 支持系统标签和用户自定义标签

### 3.2 搜索和筛选

- **关键词搜索** - 输入标签名称或代码
- **状态筛选** - 启用/禁用/待审核
- **类型筛选** - 系统标签/用户自定义

### 3.3 标签操作

#### 新增标签
1. 点击"新增标签"按钮
2. 填写标签信息：
   - 标签代码（英文唯一标识）
   - 标签名称（中文显示名）
   - 图标（可选）
   - 颜色（可选）
   - 排序顺序
   - 是否启用
3. 点击"确定"提交

#### 编辑标签
1. 找到要编辑的标签
2. 点击"编辑"按钮
3. 修改相关信息
4. 点击"确定"保存

#### 启用/禁用标签
- 点击标签行的"启用"或"禁用"按钮
- 确认操作即可

#### 删除标签
1. 点击标签行的"删除"按钮
2. 确认删除

#### 批量删除
1. 勾选多个标签
2. 点击"批量删除"按钮
3. 确认删除

### 3.4 审核用户自定义标签

**查看待审核标签：**
1. 切换到对应的标签分类
2. 状态筛选选择"待审核"
3. 点击"查询"

**审核操作：**
- **通过审核** - 点击"通过"按钮，标签变为"启用"状态
- **拒绝审核** - 点击"禁用"按钮，标签变为"禁用"状态

## 📈 数据统计

### 当前数据量（截至 2026-03-25）

| 标签类型 | 总记录数 | 已启用 | 待审核 | 已禁用 |
|---------|---------|--------|--------|--------|
| 身份标签 | 6 | 6 | - | - |
| 话题标签 | 21 | 19 | 4 | - |
| 商品标签 | 8 | 7 | 1 | - |
| 地点标签 | 16 | 16 | - | - |

### 热门标签示例

**热门话题标签：**
- study（学习）- usage_count: 0
- life（生活）- usage_count: 0
- work（工作）- usage_count: 0

**热门商品标签：**
- secondhand（二手物品）- usage_count: 0
- books（图书教材）- usage_count: 0
- digital（数码产品）- usage_count: 0

**热门地点标签：**
- library（图书馆）- usage_count: 0
- teaching_building（教学楼）- usage_count: 0
- dormitory（宿舍楼）- usage_count: 0

## 🔧 API 接口

### 基础路径
```
/api/admin/tags
```

### 常用接口示例

#### 1. 查询话题标签列表
```bash
GET /api/admin/tags/topic?pageNum=1&pageSize=10&keyword=学习
```

#### 2. 创建商品标签
```bash
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
  "isActive": true
}
```

#### 3. 更新商品标签状态
```bash
PUT /api/admin/tags/product/8/status
Content-Type: application/json

{
  "status": "banned"
}
```

#### 4. 删除地点标签
```bash
DELETE /api/admin/tags/location/5
```

## ❓ 常见问题

### Q1: 如何区分系统标签和用户自定义标签？

**答：** 
- 系统标签：`type='system'`，由管理员创建
- 用户自定义标签：`type='custom'`，由用户创建，需要审核

### Q2: 商品标签的状态和其他标签不一样？

**答：** 
- 身份/话题/地点标签：使用 `isActive` 字段（true/false）
- 商品标签：使用 `status` 字段（active/pending/banned）

原因：商品标签需要支持三态（通过/待审核/禁用），其他标签只需要两态（启用/禁用）。

### Q3: 用户创建的标签在哪里审核？

**答：** 
1. 访问标签管理页面
2. 选择对应的标签分类
3. 状态筛选选择"待审核"
4. 点击"查询"查看待审核标签
5. 点击"通过"或"禁用"进行审核

### Q4: 如何将用户自定义标签转为系统标签？

**答：** 
目前需要通过 SQL 手动转换：
```sql
UPDATE topic_tag 
SET type = 'system', 
    status = 'active', 
    is_active = 1,
    created_by = NULL
WHERE id = 标签 ID;
```

后续版本将添加一键转换功能。

### Q5: 标签的使用次数和趋势分数如何计算？

**答：** 
- **使用次数** - 每次标签被使用时自动 +1
- **趋势分数** - 根据最近使用时间计算，越近使用分数越高

具体实现参考：
- `TopicTagService.increaseUsage()` - 增加使用次数
- `TopicTagService.getHotTopicTags()` - 获取热门标签

## 📝 最佳实践

### 1. 标签命名规范

**系统标签：**
- 代码：使用英文小写，下划线分隔（如：`study_exchange`）
- 名称：使用简洁的中文（如：`学习交流`）
- 图标：使用 emoji 或 iconfont 图标
- 颜色：使用十六进制颜色码（如：`#4A90E2`）

**用户自定义标签：**
- 名称：不能与现有标签重复
- 描述：建议填写用途说明
- 审核：必须通过审核才能公开使用

### 2. 标签分类建议

**身份标签（按用户类型）：**
- student（学生）
- merchant（商户）
- organization（组织/社团）
- individual（个人）

**话题标签（按内容）：**
- study（学习）
- life（生活）
- activity（活动）
- question（提问）
- share（分享）

**商品标签（按类别）：**
- secondhand（二手物品）
- books（图书教材）
- digital（数码产品）
- clothing（服装饰品）
- daily（生活用品）

**地点标签（按位置）：**
- library（图书馆）
- classroom（教学楼）
- dormitory（宿舍楼）
- cafeteria（食堂）
- playground（操场）

### 3. 审核注意事项

**审核前检查：**
- 标签名称是否合适
- 是否与现有标签重复
- 是否违反社区规范

**审核建议：**
- 优先处理待审核时间较长的标签
- 对于模糊的标签，可以要求用户补充说明
- 定期清理长期未使用的标签

### 4. 性能优化

**数据库索引：**
```sql
-- 为常用查询字段添加索引
ALTER TABLE topic_tag ADD INDEX idx_status(status);
ALTER TABLE topic_tag ADD INDEX idx_created_by(created_by);
ALTER TABLE product_tag ADD INDEX idx_category(category);
```

**查询优化：**
- 使用分页查询，避免一次性加载所有数据
- 只查询需要的字段
- 利用缓存减少数据库访问

## 🔮 后续优化方向

### 1. 批量审核功能
支持一次性通过/拒绝多个待审核标签

### 2. 标签推荐
用户创建标签时，自动推荐相似标签，减少重复

### 3. 标签合并
将多个相似标签合并为一个，自动更新关联内容

### 4. 标签分析
统计标签的活跃度、增长趋势，辅助决策

### 5. 标签层级
支持父子标签关系，形成标签树

### 6. 标签举报
用户可以举报不当标签，管理员优先处理

## 📚 相关文档

- **完整文档** - `ADMIN_TAG_MANAGEMENT_README.md`
- **完成报告** - `ADMIN_TAG_SYSTEM_COMPLETE_REPORT.md`
- **测试脚本** - `test_admin_tag_management.sql`
- **验证脚本** - `verify_admin_tag_system.sql`

## 🎯 总结

管理员标签管理系统现已全面上线，提供：

✅ 四大类标签的统一管理  
✅ 用户自定义标签的提交和审核  
✅ 灵活的标签状态控制  
✅ 热门标签统计分析  
✅ 完善的 API 接口  
✅ 友好的管理界面  

**系统已就绪，立即开始管理您的标签体系！**

---

**最后更新：** 2026-03-25  
**版本：** v1.0
