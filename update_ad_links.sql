-- 更新广告数据，清空 linkUrl 字段，让前端根据 adType 自动生成正确的链接
UPDATE advertisements 
SET `link_url` = NULL, 
    `updated_at` = NOW()
WHERE `position` = 'mall' 
  AND `ad_type` IN ('activity', 'merchant');

-- 验证更新结果
SELECT id, title, ad_type, related_id, link_url, filter_tags 
FROM advertisements 
WHERE `position` = 'mall';
