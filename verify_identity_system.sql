-- ============================================
-- 身份认证系统快速验证 SQL
-- ============================================

USE campus_db;

-- 1. 查看所有用户身份
SELECT '=== 所有用户身份 ===' AS '';
SELECT 
    ui.id,
    ui.user_id,
    u.username,
    ui.identity_type,
    ui.identity_name,
    ui.verified,
    ui.extra_info,
    ui.created_at
FROM user_identity ui
LEFT JOIN users u ON ui.user_id = u.id
ORDER BY ui.verified DESC, ui.created_at DESC;

-- 2. 查看待审核的申请
SELECT '' AS '';
SELECT '=== 待审核申请 ===' AS '';
SELECT 
    ui.id,
    u.username,
    u.email,
    ui.identity_type,
    ui.identity_name,
    ui.extra_info,
    ui.created_at
FROM user_identity ui
LEFT JOIN users u ON ui.user_id = u.id
WHERE ui.verified = 0
ORDER BY ui.created_at DESC;

-- 3. 查看已认证的身份
SELECT '' AS '';
SELECT '=== 已认证身份 ===' AS '';
SELECT 
    u.username,
    ui.identity_type,
    ui.identity_name,
    ui.verified_at
FROM user_identity ui
LEFT JOIN users u ON ui.user_id = u.id
WHERE ui.verified = 1
ORDER BY ui.verified_at DESC;

-- 4. 统计信息
SELECT '' AS '';
SELECT '=== 统计信息 ===' AS '';
SELECT 
    identity_type AS '类型',
    COUNT(*) AS '总数',
    SUM(CASE WHEN verified = 1 THEN 1 ELSE 0 END) AS '已认证',
    SUM(CASE WHEN verified = 0 THEN 1 ELSE 0 END) AS '待审核'
FROM user_identity
GROUP BY identity_type;

-- 5. 测试查询 - 用户的多个身份
SELECT '' AS '';
SELECT '=== 用户多身份示例 ===' AS '';
SELECT 
    u.username,
    GROUP_CONCAT(ui.identity_name SEPARATOR ', ') AS identities,
    COUNT(ui.id) AS identity_count
FROM users u
LEFT JOIN user_identity ui ON u.id = ui.user_id AND ui.verified = 1
GROUP BY u.id, u.username
HAVING COUNT(ui.id) > 1;

-- 6. 视图测试
SELECT '' AS '';
SELECT '=== 视图测试结果 ===' AS '';
SELECT * FROM v_user_identity_summary WHERE verified = 1 LIMIT 5;
