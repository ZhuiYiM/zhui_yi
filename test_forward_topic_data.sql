-- 转发话题嵌套样式测试数据
-- 用于验证 ForwardedTopicCard 组件是否正确显示

-- 步骤 1：先确认表中已有字段
SHOW COLUMNS FROM topics LIKE 'is_forwarded';
SHOW COLUMNS FROM topics LIKE 'forwarded_from_topic_id';

-- 步骤 2：插入一条测试用的转发话题数据
-- 假设已有一个话题 ID=1 作为被转发的原话题
INSERT INTO topics (
    user_id, 
    title, 
    content, 
    images, 
    tags,
    level1_tag_code,
    level2_tag_codes,
    level3_tag_codes,
    level4_tag_codes,
    is_forwarded,
    forwarded_from_topic_id,
    likes_count,
    comments_count,
    views_count,
    collections_count,
    is_essence,
    is_top,
    status,
    created_at,
    updated_at
) VALUES (
    1,  -- 用户 ID，请根据实际情况修改
    '【分享自话题】这是一个测试转发话题',
    '【分享自话题】这是转发内容示例\n\n原话题内容会被自动填充',
    '[]',
    '["测试", "转发"]',
    'student',  -- 一级标签：学生
    '["forward"]',  -- 二级标签：转发
    '[]',
    '[{"name": "分享", "topicId": 1}]',  -- 四级标签：分享，指向话题 ID=1
    1,  -- is_forwarded = true
    1,  -- forwarded_from_topic_id = 1 (假设有 ID=1 的原话题)
    0,
    0,
    0,
    0,
    0,
    0,
    1,
    NOW(),
    NOW()
);

-- 步骤 3：验证数据是否插入成功
SELECT id, title, is_forwarded, forwarded_from_topic_id, created_at 
FROM topics 
WHERE is_forwarded = 1 
ORDER BY created_at DESC 
LIMIT 5;

-- 完成提示
SELECT '转发话题测试数据插入完成！请刷新话题墙查看嵌套样式效果。' AS message;
