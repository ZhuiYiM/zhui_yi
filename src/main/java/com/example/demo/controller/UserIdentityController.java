package com.example.demo.controller;

import com.example.demo.annotation.RequireIdentity;
import com.example.demo.annotation.RequireRole;
import com.example.demo.common.Result;
import com.example.demo.entity.UserIdentity;
import com.example.demo.service.UserIdentityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户身份认证控制器
 */
@RestController
@RequestMapping("/api/user/identity")
public class UserIdentityController {
    
    @Autowired
    private UserIdentityService userIdentityService;
    
    /**
     * 获取用户的身份列表
     */
    @GetMapping
    public Result getUserIdentities(HttpServletRequest request) {
        try {
            // 从请求中获取用户 ID（由拦截器设置）
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("请先登录");
            }
            
            List<UserIdentity> identities = userIdentityService.getUserIdentities(userId);
            return Result.success(identities);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取身份信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 申请身份认证
     */
    @PostMapping("/apply")
    public Result applyIdentity(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("请先登录");
            }
            
            String identityType = (String) params.get("identityType");
            if (identityType == null || identityType.trim().isEmpty()) {
                return Result.error("请选择身份类型");
            }
            
            // 移除 userId，避免前端传递
            params.remove("userId");
            
            Result result = userIdentityService.applyIdentity(userId, identityType, params);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("申请失败：" + e.getMessage());
        }
    }
    
    /**
     * 审核身份认证（管理员专用）
     */
    @RequireRole({"admin"})
    @PutMapping("/{id}/verify")
    public Result verifyIdentity(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        try {
            Long reviewerId = (Long) request.getAttribute("userId");
            Boolean approved = (Boolean) params.get("approved");
            String reason = (String) params.get("reason");
            
            if (approved == null) {
                return Result.error("请指定是否通过审核");
            }
            
            Result result = userIdentityService.verifyIdentity(id, approved, reason, reviewerId);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("审核失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取身份类型选项
     */
    @GetMapping("/types")
    public Result getIdentityTypes() {
        Map<String, String> types = new HashMap<>();
        types.put("student", "学生");
        types.put("staff", "教职工");
        types.put("merchant", "商户");
        types.put("organization", "团体/部门");
        
        return Result.success(types);
    }
}
