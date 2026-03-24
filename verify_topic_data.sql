-- 验证话题数据的查询脚本
-- 检查话题表中的图片和标签数据是否正确存储

SELECT 
    t.id,
    t.user_id,
    t.title,
    t.content,
    t.images,
    t.topic_tag_codes,
    t.level1_tag_code,
    tt.name AS topic_tag_name,
    u.username,
    u.student_id
FROM topics t
LEFT JOIN topic_tag tt ON JSON_CONTAINS(t.topic_tag_codes, CONCAT('"', tt.code, '"'))
LEFT JOIN user u ON t.user_id = u.id
WHERE t.id = 84;

-- 检查图片文件是否存在
-- 这个需要在操作系统层面验证
-- SELECT 'uploads/images/1774373467353_fc475f5ee2e140bda2cec7fc6903c0e2.jpg' AS image_path;
