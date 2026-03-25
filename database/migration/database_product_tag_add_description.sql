-- 为 product_tag 表添加 description 字段
-- 用于支持用户提交自定义标签时的补充说明

ALTER TABLE product_tag 
ADD COLUMN description VARCHAR(500) NULL COMMENT '补充说明（用户提交自定义标签时使用）';

-- 查看表结构
DESC product_tag;
