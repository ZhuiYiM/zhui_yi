-- ========================================
-- 校园信息平台 - 标签体系完整说明
-- 数据库：campus_db
-- 密码：123456
-- 更新时间：2026-03-24
-- ========================================

-- 标签体系对应关系：
-- 
-- 【原标签体系】                    【新标签体系】
-- ─────────────────────────────────────────────────────
-- tag_level_1 (一级标签 - 身份)  →  topic_tag (部分)
--   ├─ admin (管理员)              ├─ admin (管理员)
--   ├─ student (学生)              ├─ student (学生)
--   ├─ merchant (商户)             ├─ merchant (商户)
--   ├─ organization (团体)         ├─ organization (团体)
--   └─ individual (社会)           └─ individual (社会)
--
-- tag_level_2 (二级标签 - 话题分类) → topic_tag (部分)
--   ├─ study (学习)                 ├─ study (学习)
--   ├─ life (生活)                  ├─ life (生活)
--   ├─ activity (活动)              ├─ activity (活动)
--   ├─ question (问答)              ├─ question (问答)
--   ├─ share (分享)                 ├─ share (分享)
--   └─ forward (转发)               └─ forward (转发)
--
-- tag_level_3 (三级标签 - 地点)  →  location_tag
--   ├─ library (图书馆)             ├─ library (图书馆)
--   ├─ cafeteria (食堂)             ├─ cafeteria (食堂)
--   ├─ dormitory (宿舍)             ├─ dormitory (宿舍)
--   ├─ classroom (教室)             ├─ classroom (教室)
--   ├─ playground (操场)            ├─ playground (操场)
--   └─ ... (其他地点)               └─ ... (其他地点)
--
-- tag_level_4 (四级标签 - 自定义话题) → 保持 tag_level_4
--   └─ 用户自定义提交的话题标签      └─ 用户自定义提交的话题标签
--
-- tag_level_5 (五级标签 - 商品)  →  product_tag
--   ├─ secondhand (二手物品)        ├─ secondhand (二手物品)
--   ├─ books (教材书籍)             ├─ books (教材书籍)
--   ├─ digital (数码产品)           ├─ digital (数码产品)
--   ├─ clothing (服装鞋帽)          ├─ clothing (服装鞋帽)
--   ├─ daily (生活用品)             ├─ daily (生活用品)
--   ├─ sports (体育用品)            ├─ sports (体育用品)
--   ├─ service (服务需求)           ├─ service (服务需求)
--   ├─ parttime (兼职信息)          ├─ parttime (兼职信息)
--   ├─ food (美食外卖)              ├─ food (美食外卖)
--   └─ ... (其他商品)               └─ ... (其他商品)
--
-- ========================================
-- 数据库表使用说明
-- ========================================

-- 1. 身份标签和话题分类标签都存储在 topic_tag 表中
--    - 通过 name 字段区分不同的标签
--    - type='system' 表示系统标签
--    - type='custom' 表示用户自定义标签（仅话题分类支持）

-- 2. 商品标签存储在 product_tag 表中
--    - 所有商品相关标签都是系统标签
--    - 用于交易中心的商品分类

-- 3. 地点标签存储在 location_tag 表中
--    - 所有地点标签都是系统标签
--    - 用于话题和商品的位置标记

-- 4. 用户自定义话题标签仍存储在 tag_level_4 表中
--    - 用户发布话题时可以创建自定义标签
--    - 需要管理员审核后才能公开使用

-- ========================================
-- API 调用说明
-- ========================================

-- 前端调用：
-- GET /api/tags/topics      → 获取所有话题标签（包含身份 + 话题分类）
-- GET /api/tags/products    → 获取所有商品标签
-- GET /api/tags/locations   → 获取所有地点标签

-- 后端实现：
-- TopicTagService.getAllTopicTags()   → 查询 topic_tag 表
-- ProductTagService.getAllProductTags() → 查询 product_tag 表
-- LocationTagService.getAllLocationTags() → 查询 location_tag 表

-- ========================================
-- 执行步骤
-- ========================================

-- 1. 首先执行检查脚本查看当前状态
--    source check_and_fix_tags.sql;

-- 2. 如果新表数据不足，执行数据迁移
--    (check_and_fix_tags.sql 会自动从原标签表复制数据)

-- 3. 验证数据是否正确迁移
--    SELECT * FROM topic_tag ORDER BY sort_order;
--    SELECT * FROM product_tag ORDER BY sort_order;
--    SELECT * FROM location_tag ORDER BY sort_order;

-- 4. 刷新浏览器测试发布话题功能
--    - 身份标签应自动选择（根据用户身份）
--    - 话题分类标签应显示 5-6 个选项
--    - 地点标签应显示多个选项
--    - 商品标签在发布商品时使用
