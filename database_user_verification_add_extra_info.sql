-- 为 user_verifications 表添加 extra_info 字段
-- 用于存储完整的身份认证申请材料（JSON 格式）

ALTER TABLE user_verifications 
ADD COLUMN extra_info TEXT COMMENT '完整申请材料 JSON 数据' 
AFTER rejection_reason;
