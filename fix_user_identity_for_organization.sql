-- 修复用户 ID=4 的身份数据
-- 问题：user_identity 表缺少 organization 记录

-- 1. 删除旧的 merchant 身份记录
DELETE FROM user_identity WHERE user_id = 4 AND identity_type = 'merchant';

-- 2. 插入正确的 organization 身份记录
INSERT INTO user_identity (user_id, identity_type, identity_name, verified, verified_at, created_at, updated_at)
SELECT 
    4,
    'organization',
    '团体/部门',
    1,
    reviewed_at,
    NOW(),
    NOW()
FROM user_verifications
WHERE user_id = 4 
  AND verification_type = 'organization' 
  AND status = 'approved'
ORDER BY reviewed_at DESC
LIMIT 1;

-- 3. 验证修复结果
SELECT '=== 修复后的 user_identity 表 ===' AS info;
SELECT id, user_id, identity_type, identity_name, verified, verified_at 
FROM user_identity WHERE user_id = 4;
