package com.example.demo.service.topic.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.ApiResult;
import com.example.demo.entity.Topics;
import com.example.demo.entity.User;
import com.example.demo.entity.dto.TopicCreateDTO;
import com.example.demo.entity.dto.TopicQueryDTO;
import com.example.demo.mapper.TopicsMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.TopicsService;
import com.example.demo.service.topic.TopicCreateService;
import com.example.demo.utils.JwtUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 话题创建服务实现类
 */
@Service
public class TopicCreateServiceImpl extends ServiceImpl<TopicsMapper, Topics> implements TopicCreateService {
    
    @Autowired
    private TopicsMapper topicsMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TopicsService topicsService; // 用于调用主服务的其他方法
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    @Transactional
    public ApiResult createTopic(TopicCreateDTO topicDTO, HttpServletRequest request) {
        try {
            // 验证用户权限
            Integer userId = extractUserIdFromRequest(request);
            if (userId == null) {
                return ApiResult.error(401, "用户未登录");
            }
            
            // 获取用户并自动设置身份标签
            User user = userMapper.selectById(userId);
            if (user == null) {
                return ApiResult.error(404, "用户不存在");
            }
            
            // 根据用户类型自动确定一级标签代码
            String level1TagCode = determineUserLevel1Tag(user);
            
            // 验证内容长度
            if (topicDTO.getContent() == null || topicDTO.getContent().trim().isEmpty()) {
                return ApiResult.error(400, "话题内容不能为空");
            }
            
            if (topicDTO.getContent().length() > 1000) {
                return ApiResult.error(400, "话题内容不能超过 1000 字符");
            }
            
            // 验证图片数量
            if (topicDTO.getImages() != null && topicDTO.getImages().size() > 9) {
                return ApiResult.error(400, "图片数量不能超过 9 张");
            }
            
            // 验证标签数量
            if (topicDTO.getTags() != null && topicDTO.getTags().size() > 5) {
                return ApiResult.error(400, "标签数量不能超过 5 个");
            }
            
            // 创建话题
            Topics topic = new Topics();
            topic.setUserId(userId.longValue());
            
            // 自动提取标题（取内容的前 50 个字符）
            String content = topicDTO.getContent().trim();
            topic.setTitle(content.length() > 50 ? content.substring(0, 50) : content);
            
            topic.setContent(topicDTO.getContent());
            
            // 处理图片 URL
            List<String> imageUrls = topicDTO.getImages();
            topic.setImages(convertListToJson(imageUrls));
            topic.setTags(convertListToJson(topicDTO.getTags()));
            
            // 设置自动识别的一级标签
            topic.setLevel1TagCode(level1TagCode);
            topic.setLevel2TagCodes(convertListToJson(topicDTO.getLevel2TagCodes()));
            topic.setLevel3TagCodes(convertListToJson(topicDTO.getLevel3TagCodes()));
            topic.setLevel4TagCodes(convertListToJson(topicDTO.getLevel4TagCodes()));
            
            // 处理转发逻辑
            if (topicDTO.getIsForwarded() != null && topicDTO.getIsForwarded()) {
                topic.setIsForwarded(true);
                
                // 检查是商品转发还是话题转发
                if (topicDTO.getForwardedFromProductId() != null) {
                    topic.setForwardedFromProductId(topicDTO.getForwardedFromProductId());
                } else if (topicDTO.getForwardedFromTopicId() != null) {
                    topic.setForwardedFromTopicId(topicDTO.getForwardedFromTopicId());
                }
            } else {
                topic.setIsForwarded(false);
            }
            
            topic.setLikesCount(0);
            topic.setCommentsCount(0);
            topic.setViewsCount(0);
            topic.setIsEssence(0);
            topic.setStatus(1);
            topic.setCreatedAt(LocalDateTime.now());
            topic.setUpdatedAt(LocalDateTime.now());
            
            boolean saveResult = this.save(topic);
            if (!saveResult) {
                return ApiResult.error(500, "话题发布失败");
            }
            
            return ApiResult.success("话题发布成功", topic);
        } catch (Exception e) {
            return ApiResult.error(500, "话题发布失败：" + e.getMessage());
        }
    }
    
    /**
     * 从请求中提取用户 ID
     */
    private Integer extractUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 确定用户的一级身份标签
     */
    private String determineUserLevel1Tag(User user) {
        if (user == null) {
            return "society";
        }
        
        // 1. 检查管理员身份（最高优先级）
        if ("admin".equals(user.getRole()) || 
            (user.getIsAdmin() != null && user.getIsAdmin() == 1)) {
            return "admin";
        }
        
        // 2. 检查学生身份
        if (user.getStudentId() != null && !user.getStudentId().trim().isEmpty()) {
            return "student";
        }
        
        // 3. 检查商户身份
        if (user.getIsMerchant() != null && user.getIsMerchant() == 1) {
            return "merchant";
        }
        
        // 4. 检查团体身份
        if (user.getIsOrganization() != null && user.getIsOrganization() == 1) {
            return "organization";
        }
        
        // 5. 默认为社会
        return "society";
    }
    
    /**
     * 将列表转换为 JSON 字符串
     */
    private String convertListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }
}
