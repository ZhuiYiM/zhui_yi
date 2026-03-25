-- ========================================
-- 修复用户 ID=4 的身份认证问题
-- ========================================
-- 问题描述：
-- 1. users 表同时存在 is_staff=1 和 is_merchant=1
-- 2. student_id 字段有值（4515），导致前端判断为学生
-- 3. user_identity 表只有 organization（未认证），没有同步已认证身份
--
-- 预期逻辑：
-- - 最新的已通过认证是 merchant（商户）
-- - 应该设置 is_merchant=1，清除 is_staff 和 student_id
-- ========================================

USE campus_db;

-- 1. 查看当前状态
SELECT '===== 修复前：users 表状态 =====' AS message;
SELECT id, username, is_staff, is_merchant, is_organization, student_id, is_real_name_verified 
FROM users WHERE id = 4;

SELECT '===== 修复前：user_identity 表状态 =====' AS message;
SELECT id, user_id, identity_type, identity_name, verified, verified_at 
FROM user_identity WHERE user_id = 4;

SELECT '===== 修复前：user_verifications 表状态（最新 5 条） =====' AS message;
SELECT id, user_id, verification_type, status, real_name, submitted_at, reviewed_at 
FROM user_verifications 
WHERE user_id = 4 
ORDER BY id DESC LIMIT 5;

-- 2. 修复 users 表
-- 根据最新的已通过认证（merchant），设置正确的身份
SELECT '===== 执行修复：更新 users 表 =====' AS message;

UPDATE users 
SET 
    is_staff = NULL,          -- 清除教职工身份（虽然已通过，但用户后续申请了商户）
    is_merchant = 1,          -- 设置商户身份（最新的已通过认证）
    is_organization = 0,      -- 清除团体身份（申请中，未通过）
    student_id = NULL,        -- 清除学号（已不是学生身份）
    is_real_name_verified = 1 -- 保持实名认证状态
WHERE id = 4;

SELECT '✅ users 表更新完成' AS status;

-- 3. 修复 user_identity 表
-- 删除未认证的 organization 记录，添加 merchant 记录
SELECT '===== 执行修复：更新 user_identity 表 =====' AS message;

-- 删除未认证的团体身份
DELETE FROM user_identity WHERE user_id = 4 AND identity_type = 'organization' AND verified = 0;

-- 插入商户身份（取最新的已审核通过的 merchant 认证）
INSERT INTO user_identity (user_id, identity_type, identity_name, verified, verified_at)
SELECT 
    4,
    'merchant',
    '商户',
    1,
    reviewed_at
FROM user_verifications
WHERE user_id = 4 
  AND verification_type = 'merchant' 
  AND status = 'approved'
ORDER BY reviewed_at DESC
LIMIT 1;

SELECT '✅ user_identity 表更新完成' AS status;

-- 4. 验证修复结果
SELECT '===== 修复后：users 表状态 =====' AS message;
SELECT id, username, is_staff, is_merchant, is_organization, student_id, is_real_name_verified 
FROM users WHERE id = 4;

SELECT '===== 修复后：user_identity 表状态 =====' AS message;
SELECT id, user_id, identity_type, identity_name, verified, verified_at 
FROM user_identity WHERE user_id = 4;

-- 5. 测试：验证身份判断逻辑
SELECT '===== 测试：后端逻辑判断用户身份 =====' AS message;
SELECT 
    u.id,
    u.username,
    u.is_staff,
    u.is_merchant,
    u.is_organization,
    u.student_id,
    CASE
        WHEN u.is_staff = 1 THEN 'staff (教职工)'
        WHEN u.is_merchant = 1 THEN 'merchant (商户)'
        WHEN u.is_organization = 1 THEN 'organization (团体)'
        WHEN u.student_id IS NOT NULL AND u.student_id != '' THEN 'student (学生)'
        WHEN u.is_real_name_verified = 1 THEN 'society (社会)'
        ELSE 'society (社会)'
    END AS expected_level1_tag
FROM users u
WHERE u.id = 4;

SELECT '===== 修复完成 =====' AS message;
SELECT '请刷新页面并重新发布话题，验证身份标签是否正确显示为"商户"' AS tip;
