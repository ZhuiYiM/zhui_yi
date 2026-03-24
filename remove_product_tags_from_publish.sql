-- ========================================
-- 校园信息平台 - 移除商品发布页面的商品标签功能
-- 创建时间：2026-03-24
-- 说明：清理商品发布页面中与商品标签相关的代码和数据库字段
-- ========================================

USE campus_db;

-- ========================================
-- 数据库影响分析
-- ========================================

-- 1. products 表的 tags 字段保留
--    原因：交易中心（Mall.vue）的商品筛选功能仍在使用 tags 字段
--    位置：database_tag_level_5.sql 第 49 行添加该字段

-- 2. product_tag 表保留
--    原因：自定义商品标签提交功能（ProductCustomTagModal.vue）仍在使用
--    位置：database_product_tag_add_description.sql 添加了 description 字段

-- 3. tag_level_5 表保留
--    原因：商业中心一级标签分类仍在使用（学生、商户、团体、社会）

-- ========================================
-- 不需要修改数据库的原因
-- ========================================

-- 经过检查发现：
-- 1. 商品发布接口（ProductController.createProduct）从未处理过 tags 字段
-- 2. 前端 PublishProduct.vue 中的标签功能只是前端交互，没有实际调用后端保存
-- 3. tags 字段在商品查询和筛选中仍然使用，需要保留
-- 4. 交易中心的商品标签筛选功能（Mall.vue）仍需正常工作

-- ========================================
-- 前端代码变更
-- ========================================

-- 已删除的文件和内容：
-- 1. PublishProduct.vue 第 174-210 行：两个重复的商品标签表单项
-- 2. PublishProduct.vue 第 228-305 行：商品标签选择模态框
-- 3. PublishProduct.vue 第 337-363 行：标签相关的变量和计算属性
-- 4. PublishProduct.vue 第 446-521 行：标签相关的方法
-- 5. PublishProduct.vue 第 664-690 行：商品标签相关的 CSS 样式
-- 6. PublishProduct.vue 第 480 行：提交数据中的 tags 字段映射

-- ========================================
-- 后端代码保持不变
-- ========================================

-- 保留的后端功能：
-- 1. ProductController - 商品 CRUD 接口
-- 2. ProductServiceImpl.getProducts - 商品查询时的 tags 筛选（第 135、167-176 行）
-- 3. SearchServiceImpl.searchProductsInternal - 搜索时的 tags 筛选（第 161-209 行）
-- 4. TagController - 商品标签管理接口
-- 5. ProductTagService - 商品标签服务（包括自定义标签提交）

-- ========================================
-- 验证步骤
-- ========================================

-- 1. 验证商品发布页面正常显示
SELECT '验证商品发布页面' as check_item;
-- 访问 http://localhost:5173/mall/publish 应该能看到完整的表单，没有标签选择器

-- 2. 验证交易中心筛选功能正常
SELECT '验证交易中心筛选' as check_item;
-- 访问 http://localhost:5173/mall 应该能正常使用标签筛选商品

-- 3. 验证自定义标签提交功能正常
SELECT '验证自定义标签提交' as check_item;
-- 在交易中心点击"🏷️ 自定义标签"按钮应该能打开弹窗并提交新标签

-- 4. 验证数据库表结构完整
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'campus_db'
  AND TABLE_NAME IN ('products', 'product_tag', 'tag_level_5')
ORDER BY TABLE_NAME, ORDINAL_POSITION;

-- ========================================
-- 总结
-- ========================================

-- 本次变更仅涉及前端代码清理，不影响数据库结构和后端逻辑
-- 商品发布页面不再提供标签选择功能，简化了用户操作流程
-- 交易中心的标签筛选和自定义标签提交功能保持正常工作
