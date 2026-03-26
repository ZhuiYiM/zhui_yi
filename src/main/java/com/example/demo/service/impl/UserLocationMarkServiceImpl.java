package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.Result;
import com.example.demo.entity.User;
import com.example.demo.entity.UserLocationMark;
import com.example.demo.entity.UserVerification;
import com.example.demo.entity.dto.UserLocationMarkCreateDTO;
import com.example.demo.entity.enums.IdentityLevel1;
import com.example.demo.mapper.UserLocationMarkMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.UserVerificationMapper;
import com.example.demo.service.UserLocationMarkService;
import com.example.demo.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户位置标记服务实现
 */
@Service
public class UserLocationMarkServiceImpl extends ServiceImpl<UserLocationMarkMapper, UserLocationMark> 
        implements UserLocationMarkService {
    
    @Autowired
    private UserLocationMarkMapper userLocationMarkMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserVerificationMapper userVerificationMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createMark(UserLocationMarkCreateDTO dto, HttpServletRequest request) {
        try {
            // 从 Token 获取用户 ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Integer userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 查询用户的认证身份
            String identityType = null;
            QueryWrapper<UserVerification> verificationWrapper = new QueryWrapper<>();
            verificationWrapper.eq("user_id", userId)
                              .eq("status", "approved")
                              .in("verification_type", "student", "staff", "merchant", "organization")
                              .last("LIMIT 1"); // 只取一条记录
            UserVerification verification = userVerificationMapper.selectOne(verificationWrapper);
            
            if (verification != null) {
                // 有认证身份的用户
                identityType = verification.getVerificationType();
            }
            
            // 验证身份和标记类型的匹配
            if ("merchant_shop".equals(dto.getMarkType())) {
                if (!"merchant".equals(identityType)) {
                    return Result.error("只有认证商家才能标记店铺位置");
                }
            } else if ("organization_activity".equals(dto.getMarkType())) {
                if (!"organization".equals(identityType)) {
                    return Result.error("只有认证团体才能标记活动地点");
                }
            }
            
            // 创建标记对象
            UserLocationMark mark = new UserLocationMark();
            mark.setUserId(userId.longValue());
            mark.setCampusId(dto.getCampusId());
            mark.setLocationName(dto.getLocationName());
            mark.setLatitude(dto.getLatitude());
            mark.setLongitude(dto.getLongitude());
            mark.setAddressDetail(dto.getAddressDetail());
            mark.setMarkType(dto.getMarkType());
            mark.setMarkCategory(dto.getMarkCategory());
            mark.setUserIdentityType(identityType);
            mark.setDescription(dto.getDescription());
            mark.setContactInfo(dto.getContactInfo());
            
            // 处理图片（转换为 JSON）
            if (dto.getImages() != null && !dto.getImages().isEmpty()) {
                mark.setImages(objectMapper.writeValueAsString(dto.getImages()));
            }
            
            mark.setStartTime(dto.getStartTime());
            mark.setEndTime(dto.getEndTime());
            mark.setIsPermanent(dto.getIsPermanent() ? 1 : 0);
            mark.setVisibility(dto.getVisibility());
            
            // 根据用户身份决定是否需要审核
            // 有认证身份的用户（学生、教职、商户、组织）直接通过，无身份认证的需要管理员审核
            if (identityType != null && List.of("student", "staff", "merchant", "organization").contains(identityType)) {
                // 有认证身份，直接审核通过
                mark.setVerificationStatus("approved");
                mark.setVerifiedAt(LocalDateTime.now());
                mark.setReviewComment("系统自动审核通过（已认证用户）");
            } else {
                // 无身份认证，需要管理员审核
                mark.setVerificationStatus("pending");
                mark.setReviewComment("待管理员审核（未认证用户）");
            }
            mark.setIsActive(1);
            mark.setCreatedAt(LocalDateTime.now());
            
            // 保存
            userLocationMarkMapper.insert(mark);
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", mark.getId());
            if ("approved".equals(mark.getVerificationStatus())) {
                data.put("message", "标记创建成功");
            } else {
                data.put("message", "标记创建成功，请等待管理员审核");
            }
            
            return Result.success(data);
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("创建标记失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result getMyMarks(Integer page, Integer size, HttpServletRequest request) {
        try {
            // 从 Token 获取用户 ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Integer userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 分页查询
            Page<UserLocationMark> markPage = new Page<>(page, size);
            QueryWrapper<UserLocationMark> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                   .orderByDesc("created_at");
            
            Page<UserLocationMark> result = userLocationMarkMapper.selectPage(markPage, wrapper);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("list", result.getRecords());
            response.put("total", result.getTotal());
            response.put("page", page);
            response.put("size", size);
            response.put("pages", result.getPages());
            
            return Result.success(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取标记列表失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result getAllMyMarks(HttpServletRequest request) {
        try {
            // 从 Token 获取用户 ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Integer userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 查询当前用户的所有标记（包含所有可见性）
            QueryWrapper<UserLocationMark> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                   .orderByDesc("created_at");
            
            List<UserLocationMark> marks = userLocationMarkMapper.selectList(wrapper);
            
            return Result.success(marks);
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取标记列表失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result getCampusMarks(Integer campusId, String markType) {
        try {
            QueryWrapper<UserLocationMark> wrapper = new QueryWrapper<>();
            wrapper.eq("campus_id", campusId)
                   .eq("verification_status", "approved")
                   .eq("visibility", "public_active")  // 只显示主动公开的标记
                   .eq("is_active", 1);
            
            if (markType != null && !markType.isEmpty()) {
                wrapper.eq("mark_type", markType);
            }
            
            List<UserLocationMark> marks = userLocationMarkMapper.selectList(wrapper);
            
            return Result.success(marks);
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取校区标记失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result getUserPublicMarks(Integer userId) {
        try {
            // 查询指定用户的公开地点标记（public_active 或 public_passive，且审核通过）
            QueryWrapper<UserLocationMark> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                   .eq("verification_status", "approved")
                   .in("visibility", "public_active", "public_passive")  // 包括主动公开和被动公开
                   .eq("is_active", 1)
                   .orderByDesc("created_at");
            
            List<UserLocationMark> marks = userLocationMarkMapper.selectList(wrapper);
            
            return Result.success(marks);
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取用户公开标记失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result getMarkDetail(Long markId) {
        try {
            UserLocationMark mark = userLocationMarkMapper.selectById(markId);
            if (mark == null) {
                return Result.error("标记不存在");
            }
            
            // 增加浏览次数
            mark.setViewCount(mark.getViewCount() != null ? mark.getViewCount() + 1 : 1);
            userLocationMarkMapper.updateById(mark);
            
            // 获取用户信息
            Map<String, Object> result = new HashMap<>();
            result.put("mark", mark);
            
            // 查询用户的基本信息
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.eq("id", mark.getUserId());
            User user = userMapper.selectOne(userWrapper);
            
            if (user != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("avatarUrl", user.getAvatarUrl());
                userInfo.put("college", user.getCollege());
                userInfo.put("studentId", user.getStudentId());
                result.put("publisher", userInfo);
            } else {
                // 用户不存在，返回默认信息
                Map<String, Object> defaultPublisher = new HashMap<>();
                defaultPublisher.put("userId", mark.getUserId());
                defaultPublisher.put("username", "匿名用户");
                defaultPublisher.put("avatarUrl", null);
                result.put("publisher", defaultPublisher);
            }
            
            return Result.success(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取标记详情失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateMark(Long markId, UserLocationMarkCreateDTO dto, HttpServletRequest request) {
        try {
            // 从 Token 获取用户 ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Integer userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 查询标记
            UserLocationMark mark = userLocationMarkMapper.selectById(markId);
            if (mark == null) {
                return Result.error("标记不存在");
            }
            
            // 检查是否是自己的标记
            if (!mark.getUserId().equals(userId.longValue())) {
                return Result.error("无权修改他人的标记");
            }
            
            // 如果是已审核通过的标记，修改后需要重新审核
            if ("approved".equals(mark.getVerificationStatus())) {
                mark.setVerificationStatus("pending");
                mark.setVerifiedAt(null);
            }
            
            // 更新字段
            mark.setLocationName(dto.getLocationName());
            mark.setLatitude(dto.getLatitude());
            mark.setLongitude(dto.getLongitude());
            mark.setAddressDetail(dto.getAddressDetail());
            mark.setMarkCategory(dto.getMarkCategory());
            mark.setDescription(dto.getDescription());
            mark.setContactInfo(dto.getContactInfo());
            
            if (dto.getImages() != null) {
                mark.setImages(objectMapper.writeValueAsString(dto.getImages()));
            }
            
            mark.setStartTime(dto.getStartTime());
            mark.setEndTime(dto.getEndTime());
            mark.setIsPermanent(dto.getIsPermanent() ? 1 : 0);
            mark.setVisibility(dto.getVisibility());
            mark.setUpdatedAt(LocalDateTime.now());
            
            userLocationMarkMapper.updateById(mark);
            
            return Result.success("标记更新成功");
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新标记失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteMark(Long markId, HttpServletRequest request) {
        try {
            // 从 Token 获取用户 ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Integer userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 查询标记
            UserLocationMark mark = userLocationMarkMapper.selectById(markId);
            if (mark == null) {
                return Result.error("标记不存在");
            }
            
            // 检查是否是自己的标记（管理员可以删除任何标记）
            if (!mark.getUserId().equals(userId.longValue())) {
                // TODO: 检查是否是管理员
                return Result.error("无权删除他人的标记");
            }
            
            // 软删除
            mark.setIsActive(0);
            mark.setUpdatedAt(LocalDateTime.now());
            userLocationMarkMapper.updateById(mark);
            
            return Result.success("标记删除成功");
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除标记失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result verifyMark(Long markId, String status, String comment, HttpServletRequest request) {
        try {
            // TODO: 需要添加管理员权限检查
            
            UserLocationMark mark = userLocationMarkMapper.selectById(markId);
            if (mark == null) {
                return Result.error("标记不存在");
            }
            
            mark.setVerificationStatus(status);
            mark.setReviewComment(comment);
            
            if ("approved".equals(status)) {
                mark.setVerifiedAt(LocalDateTime.now());
                // TODO: 从 request 获取审核员 ID
                // mark.setReviewerId(reviewerId);
            }
            
            mark.setUpdatedAt(LocalDateTime.now());
            userLocationMarkMapper.updateById(mark);
            
            return Result.success("审核成功");
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("审核失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result likeMark(Long markId, HttpServletRequest request) {
        try {
            UserLocationMark mark = userLocationMarkMapper.selectById(markId);
            if (mark == null) {
                return Result.error("标记不存在");
            }
            
            // 点赞数 +1
            mark.setLikeCount(mark.getLikeCount() != null ? mark.getLikeCount() + 1 : 1);
            userLocationMarkMapper.updateById(mark);
            
            return Result.success("点赞成功");
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("点赞失败：" + e.getMessage());
        }
    }
}
