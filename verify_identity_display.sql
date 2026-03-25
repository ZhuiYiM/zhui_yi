-- 验证用户身份显示修复
-- 用于测试不同身份类型的用户数据

USE campus_db;

-- 查看当前用户身份状态
SELECT 
    id,
    username,
    student_id,
    is_verified,
    is_staff,
    is_merchant,
    is_organization,
    CASE
        WHEN is_verified = 0 THEN '未认证'
        WHEN is_staff = 1 THEN '教职工'
        WHEN is_merchant = 1 THEN '商户'
        WHEN is_organization = 1 THEN '组织'
        WHEN student_id IS NOT NULL AND student_id != '' THEN '学生'
        ELSE '已认证'
    END AS identity_label
FROM users
ORDER BY id DESC
LIMIT 10;

-- 测试数据：用户 4 应该是教职工
UPDATE users 
SET 
    is_verified = 1,
    is_staff = 1,
    student_id = '54115',
    department = '16515'
WHERE id = 4;

-- 验证更新后的结果
SELECT 
    id,
    username,
    student_id,
    is_verified,
    is_staff,
    is_merchant,
    is_organization,
    CASE
        WHEN is_verified = 0 THEN '未认证'
        WHEN is_staff = 1 THEN '教职工'
        WHEN is_merchant = 1 THEN '商户'
        WHEN is_organization = 1 THEN '组织'
        WHEN student_id IS NOT NULL AND student_id != '' THEN '学生'
        ELSE '已认证'
    END AS identity_label
FROM users
WHERE id = 4;
