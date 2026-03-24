package com.example.demo.service.topic.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.ApiResult;
import com.example.demo.entity.Topics;
import com.example.demo.entity.User;
import com.example.demo.mapper.TopicsMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.topic.TopicForwardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 话题转发服务实现类
 */
@Service
public class TopicForwardServiceImpl extends ServiceImpl<TopicsMapper, Topics> implements TopicForwardService {

    @Autowired
    private TopicsMapper topicsMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public ApiResult forwardTopic(Integer topicId, String content, List<String> images, HttpServletRequest request) {
        try {
            // 1. 获取原话题
            Topics originalTopic = topicsMapper.selectById(topicId);
            if (originalTopic == null || originalTopic.getStatus() != 1) {
                return ApiResult.error(404, "原话题不存在");
            }
            
            // 2. 获取当前用户
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return ApiResult.error(401, "请先登录");
            }
            
            User user = userMapper.selectById(userId);
            if (user == null) {
                return ApiResult.error(404, "用户不存在");
            }
            
            // 3. 创建新话题（转发）
            Topics newTopic = new Topics();
            newTopic.setUserId(userId);
            newTopic.setTitle(originalTopic.getTitle()); // 继承原标题
            newTopic.setContent(content != null ? content : originalTopic.getContent()); // 使用补充内容或原内容
            
            // 根据转发类型决定是否继承图片
            if (originalTopic.getForwardedFromProductId() != null) {
                // 商品分享：不继承图片，仅允许文字说明
                newTopic.setImages(null);
            } else {
                // 话题转发或地点转发：不继承原话题的图片，只使用转发时上传的图片
                if (images != null && !images.isEmpty()) {
                    // 使用转发时上传的图片
                    newTopic.setImages(convertListToJson(images));
                } else {
                    // 没有上传图片，则不设置图片
                    newTopic.setImages(null);
                }
            }
            
            newTopic.setTags(originalTopic.getTags()); // 继承标签
            
            // 设置身份标签（继承原话题的身份标签）
            newTopic.setLevel1TagCode(originalTopic.getLevel1TagCode());
            
            // 设置话题标签（根据来源添加不同的标签）
            if (originalTopic.getForwardedFromProductId() != null) {
                // 商品分享：添加“商品分享”标签
                String productShareTag = "[\"product_share\"]"; // JSON 数组格式
                if (originalTopic.getTopicTagCodes() != null && !originalTopic.getTopicTagCodes().isEmpty()) {
                    // 如果原话题有话题标签，保留并添加“商品分享”
                    String originalTags = originalTopic.getTopicTagCodes();
                    // 去掉末尾的 ] 并添加，"product_share"]
                    productShareTag = originalTags.substring(0, originalTags.length() - 1) + ",\"product_share\"]";
                }
                newTopic.setTopicTagCodes(productShareTag);
            } else if (originalTopic.getForwardedFromTopicId() != null && originalTopic.getLocationTagCodes() != null && !originalTopic.getLocationTagCodes().isEmpty()) {
                // 地点转发：添加“地点转发”标签
                String locationShareTag = "[\"location_share\"]"; // JSON 数组格式
                if (originalTopic.getTopicTagCodes() != null && !originalTopic.getTopicTagCodes().isEmpty()) {
                    // 如果原话题有话题标签，保留并添加“地点转发”
                    String originalTags = originalTopic.getTopicTagCodes();
                    // 去掉末尾的 ] 并添加，"location_share"]
                    locationShareTag = originalTags.substring(0, originalTags.length() - 1) + ",\"location_share\"]";
                }
                newTopic.setTopicTagCodes(locationShareTag);
            } else {
                // 话题转发：添加“话题转发”标签
                String topicForwardTag = "[\"topic_forward\"]"; // JSON 数组格式
                if (originalTopic.getTopicTagCodes() != null && !originalTopic.getTopicTagCodes().isEmpty()) {
                    // 如果原话题有话题标签，保留并添加“话题转发”
                    String originalTags = originalTopic.getTopicTagCodes();
                    // 去掉末尾的 ] 并添加，"topic_forward"]
                    topicForwardTag = originalTags.substring(0, originalTags.length() - 1) + ",\"topic_forward\"]";
                }
                newTopic.setTopicTagCodes(topicForwardTag);
            }
            
            // 继承商品标签和地点标签
            newTopic.setProductTagCodes(originalTopic.getProductTagCodes());
            newTopic.setLocationTagCodes(originalTopic.getLocationTagCodes());
            
            // 设置转发标记
            newTopic.setIsForwarded(true);
            newTopic.setForwardedFromTopicId((long) topicId);
            newTopic.setForwardedFromProductId(originalTopic.getForwardedFromProductId());
            
            // 初始化统计数据
            newTopic.setLikesCount(0);
            newTopic.setCommentsCount(0);
            newTopic.setViewsCount(0);
            newTopic.setCollectionsCount(0);
            newTopic.setIsEssence(0);
            newTopic.setIsTop(0);
            newTopic.setStatus(1);
            newTopic.setCreatedAt(LocalDateTime.now());
            newTopic.setUpdatedAt(LocalDateTime.now());
            
            // 4. 插入数据库
            topicsMapper.insert(newTopic);
            
            // 5. 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("id", newTopic.getId());
            result.put("forwardedTopicId", newTopic.getId());
            result.put("originalTopicId", topicId);
            
            return ApiResult.success("转发成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResult.error(500, "转发失败：" + e.getMessage());
        }
    }
    
    /**
     * 将列表转换为 JSON 字符串
     */
    private String convertListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 分享商品到话题墙
     * @param productId 商品 ID
     * @param content 分享内容
     * @param request HTTP 请求
     * @return API 结果
     */
    @Override
    public ApiResult getForwardedTopicDetail(Integer topicId) {
        try {
            Topics topic = this.getById(topicId);
            if (topic == null) {
                return ApiResult.error(404, "话题不存在");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("topic", topic);
            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error(500, "获取转发话题失败：" + e.getMessage());
        }
    }
}
