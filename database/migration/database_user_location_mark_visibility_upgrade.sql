-- ========================================
-- 用户位置标记可见性选项升级
-- 创建时间：2026-03-26
-- 说明：更新 visibility 字段枚举值，适配新的可见性规则
-- ========================================

USE campus_db;

-- ========================================
-- 1. 更新现有数据的可见性值
-- ========================================

-- 将原来的 'public' 转换为 'public_active'（主动所有人可见）
UPDATE user_location_mark 
SET visibility = 'public_active' 
WHERE visibility = 'public';

-- 将原来的 'friends' 转换为 'public_passive'（被动可见）
UPDATE user_location_mark 
SET visibility = 'public_passive' 
WHERE visibility = 'friends';

-- 'private' 保持不变
-- UPDATE user_location_mark SET visibility = 'private' WHERE visibility = 'private';

-- ========================================
-- 2. 验证更新结果
-- ========================================

-- 查看各可见性状态的数量
SELECT 
  visibility,
  COUNT(*) as count,
  GROUP_CONCAT(location_name) as locations
FROM user_location_mark 
GROUP BY visibility;

-- ========================================
-- 3. 说明
-- ========================================

-- 新的可见性规则：
-- 1. public_active（主动所有人可见）：
--    - 在地图导引页面可见（所有人都能看到）
--    - 在用户对外展示页面可见
--    - 在个人中心页面可见
--
-- 2. public_passive（被动可见）：
--    - 仅在用户对外展示页面可见
--    - 在个人中心页面可见
--    - 不在地图导引页面显示
--
-- 3. private（仅自己可见）：
--    - 仅在个人中心页面可见
--    - 不在地图导引和用户对外展示页面显示
--
-- 查询示例：
-- 
-- 获取地图导引页面显示的标记（仅 public_active 且已审核通过）：
-- SELECT * FROM user_location_mark 
-- WHERE visibility = 'public_active' 
--   AND verification_status = 'approved' 
--   AND is_active = 1;
--
-- 获取用户对外展示页面的标记（public_active 或 public_passive）：
-- SELECT * FROM user_location_mark 
-- WHERE visibility IN ('public_active', 'public_passive') 
--   AND verification_status = 'approved' 
--   AND is_active = 1;
--
-- 获取个人中心的标记（所有可见性）：
-- SELECT * FROM user_location_mark 
-- WHERE user_id = ?; -- 当前用户 ID
