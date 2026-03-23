package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.Result;
import com.example.demo.entity.User;
import com.example.demo.entity.UserLocationMark;
import com.example.demo.entity.dto.UserLocationMarkCreateDTO;
import com.example.demo.entity.enums.IdentityLevel1;
import com.example.demo.mapper.UserLocationMarkMapper;
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
            
            // 获取用户身份（简化处理，实际应该查询数据库）
            User user = new User(); // TODO: 需要从数据库获取完整用户信息
            String identityType = "student"; // 默认学生身份
            
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
            
            // 默认为待审核状态
            mark.setVerificationStatus("pending");
            mark.setIsActive(1);
            mark.setCreatedAt(LocalDateTime.now());
            
            // 保存
            userLocationMarkMapper.insert(mark);
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", mark.getId());
            data.put("message", "标记创建成功，请等待管理员审核");
            
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
    public Result getCampusMarks(Integer campusId, String markType) {
        try {
            QueryWrapper<UserLocationMark> wrapper = new QueryWrapper<>();
            wrapper.eq("campus_id", campusId)
                   .eq("verification_status", "approved")
                   .eq("visibility", "public")
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
    public Result getMarkDetail(Long markId) {
        try {
            UserLocationMark mark = userLocationMarkMapper.selectById(markId);
            if (mark == null) {
                return Result.error("标记不存在");
            }
            
            // 增加浏览次数
            mark.setViewCount(mark.getViewCount() != null ? mark.getViewCount() + 1 : 1);
            userLocationMarkMapper.updateById(mark);
            
            return Result.success(mark);
            
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
