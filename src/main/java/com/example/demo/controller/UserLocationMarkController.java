package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.dto.UserLocationMarkCreateDTO;
import com.example.demo.service.UserLocationMarkService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户位置标记控制器
 */
@RestController
@RequestMapping("/api/user/location-marks")
@CrossOrigin(origins = "*")
public class UserLocationMarkController {
    
    @Autowired
    private UserLocationMarkService userLocationMarkService;
    
    /**
     * 创建位置标记
     */
    @PostMapping
    public Result createMark(@RequestBody UserLocationMarkCreateDTO dto, 
                             HttpServletRequest request) {
        return userLocationMarkService.createMark(dto, request);
    }
    
    /**
     * 获取我的标记列表
     */
    @GetMapping("/my")
    public Result getMyMarks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        return userLocationMarkService.getMyMarks(page, size, request);
    }
    
    /**
     * 获取当前用户的所有标记（包含所有可见性）
     */
    @GetMapping("/my/all")
    public Result getAllMyMarks(HttpServletRequest request) {
        return userLocationMarkService.getAllMyMarks(request);
    }
    
    /**
     * 获取校区的公开标记
     */
    @GetMapping("/campus/{campusId}")
    public Result getCampusMarks(
            @PathVariable Integer campusId,
            @RequestParam(required = false) String markType) {
        return userLocationMarkService.getCampusMarks(campusId, markType);
    }
    
    /**
     * 获取标记详情
     */
    @GetMapping("/{markId}")
    public Result getMarkDetail(@PathVariable Long markId) {
        return userLocationMarkService.getMarkDetail(markId);
    }
    
    /**
     * 更新标记
     */
    @PutMapping("/{markId}")
    public Result updateMark(
            @PathVariable Long markId,
            @RequestBody UserLocationMarkCreateDTO dto,
            HttpServletRequest request) {
        return userLocationMarkService.updateMark(markId, dto, request);
    }
    
    /**
     * 删除标记
     */
    @DeleteMapping("/{markId}")
    public Result deleteMark(
            @PathVariable Long markId,
            HttpServletRequest request) {
        return userLocationMarkService.deleteMark(markId, request);
    }
    
    /**
     * 审核标记（管理员）
     */
    @PutMapping("/{markId}/verify")
    public Result verifyMark(
            @PathVariable Long markId,
            @RequestParam String status,
            @RequestParam(required = false) String comment,
            HttpServletRequest request) {
        return userLocationMarkService.verifyMark(markId, status, comment, request);
    }
    
    /**
     * 点赞标记
     */
    @PostMapping("/{markId}/like")
    public Result likeMark(@PathVariable Long markId, HttpServletRequest request) {
        return userLocationMarkService.likeMark(markId, request);
    }
}
