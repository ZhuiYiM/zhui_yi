package com.example.demo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.entity.UserLocationMark;
import com.example.demo.entity.dto.UserLocationMarkCreateDTO;
import com.example.demo.common.Result;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户位置标记服务接口
 */
public interface UserLocationMarkService {
    
    /**
     * 创建位置标记
     */
    Result createMark(UserLocationMarkCreateDTO dto, HttpServletRequest request);
    
    /**
     * 获取我的标记列表
     */
    Result getMyMarks(Integer page, Integer size, HttpServletRequest request);
    
    /**
     * 获取校区的公开标记
     */
    Result getCampusMarks(Integer campusId, String markType);
    
    /**
     * 获取标记详情
     */
    Result getMarkDetail(Long markId);
    
    /**
     * 更新标记（仅自己的标记）
     */
    Result updateMark(Long markId, UserLocationMarkCreateDTO dto, HttpServletRequest request);
    
    /**
     * 删除标记（仅自己的标记）
     */
    Result deleteMark(Long markId, HttpServletRequest request);
    
    /**
     * 审核标记（管理员）
     */
    Result verifyMark(Long markId, String status, String comment, HttpServletRequest request);
    
    /**
     * 点赞标记
     */
    Result likeMark(Long markId, HttpServletRequest request);
}
