-- 初始化话题标签和地点标签数据
-- 用于修复现有话题数据的标签显示问题

USE campus_db;

-- 1. 插入缺失的话题标签
INSERT INTO topic_tag (name, code, type, status, is_active, icon, color, sort_order) VALUES
('second_hand', 'second_hand', 'system', 'active', 1, '💰', '#FF9800', 10),
('club', 'club', 'system', 'active', 1, '👥', '#2196F3', 11),
('campus_life', 'campus_life', 'system', 'active', 1, '🏫', '#4CAF50', 12),
('lost_found', 'lost_found', 'system', 'active', 1, '📢', '#FFC107', 13),
('study', 'study', 'system', 'active', 1, '📚', '#4A90E2', 1),
('life', 'life', 'system', 'active', 1, '', '#50E3C2', 2),
('activity', 'activity', 'system', 'active', 1, '🎉', '#F5A623', 3),
('question', 'question', 'system', 'active', 1, '❓', '#D0021B', 4),
('share', 'share', 'system', 'active', 1, '📢', '#9013FE', 5),
('food', 'food', 'system', 'active', 1, '🍔', '#E74C3C', 6);

-- 2. 插入缺失的地点标签 (location_tag 表没有 code 字段)
INSERT INTO location_tag (name, type, status, is_active, icon, color, sort_order) VALUES
('textbook', 'system', 'active', 1, '📖', '#4A90E2', 10),
('book', 'system', 'active', 1, '📚', '#50E3C2', 11),
('recruitment', 'system', 'active', 1, '📝', '#F5A623', 12),
('tech_share', 'system', 'active', 1, '💡', '#D0021B', 13),
('recommend', 'system', 'active', 1, '⭐', '#9013FE', 14),
('delicious', 'system', 'active', 1, '😋', '#E74C3C', 15),
('basketball', 'system', 'active', 1, '🏀', '#FF9800', 16),
('sports', 'system', 'active', 1, '⚽', '#2196F3', 17),
('phone', 'system', 'active', 1, '📱', '#4CAF50', 18),
('found', 'system', 'active', 1, '✅', '#FFC107', 19),
('library', 'system', 'active', 1, '📚', '#4A90E2', 1),
('teaching_building', 'system', 'active', 1, '🏫', '#50E3C2', 2),
('dormitory', 'system', 'active', 1, '🏠', '#F5A623', 3),
('cafeteria', 'system', 'active', 1, '🍽️', '#D0021B', 4),
('playground', 'system', 'active', 1, '🏟️', '#9013FE', 5),
('computer_room', 'system', 'active', 1, '💻', '#E74C3C', 6);

-- 3. 验证数据
SELECT '话题标签插入完成' AS status;
SELECT COUNT(*) AS topic_tag_count FROM topic_tag WHERE is_active = 1;

SELECT '地点标签插入完成' AS status;
SELECT COUNT(*) AS location_tag_count FROM location_tag WHERE is_active = 1;

-- 4. 检查现有话题的标签关联
SELECT 
    t.id AS topic_id,
    t.content AS topic_content,
    t.topic_tag_codes,
    t.location_tag_codes,
    tt.name AS matched_topic_tag,
    lt.name AS matched_location_tag
FROM topics t
LEFT JOIN topic_tag tt ON JSON_CONTAINS(t.topic_tag_codes, JSON_QUOTE(tt.name))
LEFT JOIN location_tag lt ON JSON_CONTAINS(t.location_tag_codes, JSON_QUOTE(lt.name))
WHERE t.topic_tag_codes IS NOT NULL OR t.location_tag_codes IS NOT NULL
LIMIT 10;
