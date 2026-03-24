-- 修复标签体系数据库表结构
-- 为 topic_tag 和 product_tag 表添加缺失的 code 和 category 字段

USE campus_db;

-- 1. 为 topic_tag 表添加 code 字段
ALTER TABLE topic_tag 
ADD COLUMN code VARCHAR(50) COMMENT '标签代码 (用于系统标签)' AFTER name;

-- 2. 为 product_tag 表添加 code 和 category 字段
ALTER TABLE product_tag 
ADD COLUMN code VARCHAR(50) COMMENT '标签代码 (用于系统标签)' AFTER name,
ADD COLUMN category VARCHAR(50) COMMENT '分类 (用于商品标签)' AFTER type;

-- 3. 检查表结构
DESC topic_tag;
DESC product_tag;

-- 4. 验证修改成功
SELECT 'topic_tag 表结构验证成功' AS status;
SELECT 'product_tag 表结构验证成功' AS status;
