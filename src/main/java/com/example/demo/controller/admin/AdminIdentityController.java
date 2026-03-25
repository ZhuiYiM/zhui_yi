package com.example.demo.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
 * 管理员身份认证管理控制器
 */
@RestController
@RequestMapping("/api/admin/identity")
public class AdminIdentityController {
    
    @Autowired
    private UserIdentityService userIdentityService;
    
    /**
     * 获取身份认证列表（分页）
     */
    @RequireRole({"admin"})
    @GetMapping("/list")
    public Result getIdentityList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String identityType,
            @RequestParam(required = false) Integer verified,
            @RequestParam(required = false) String search) {
        try {
            // TODO: 实现分页查询逻辑
            // 暂时返回所有数据
            List<UserIdentity> identities = userIdentityService.list();
            
            // 过滤条件
            if (identityType != null && !identityType.isEmpty()) {
                identities = identities.stream()
                    .filter(i -> identityType.equals(i.getIdentityType()))
                    .toList();
            }
            
            if (verified != null) {
                identities = identities.stream()
                    .filter(i -> verified.equals(i.getVerified()))
                    .toList();
            }
            
            if (search != null && !search.isEmpty()) {
                final String keyword = search.toLowerCase();
                identities = identities.stream()
                    .filter(i -> i.getIdentityName().toLowerCase().contains(keyword))
                    .toList();
            }
            
            // 分页
            int total = identities.size();
            int fromIndex = (page - 1) * size;
            int toIndex = Math.min(fromIndex + size, total);
            
            if (fromIndex >= total) {
                return Result.success(new HashMap<String, Object>() {{
                    put("records", new java.util.ArrayList<>());
                    put("total", 0L);
                    put("current", page.longValue());
                    put("size", size.longValue());
                }});
            }
            
            List<UserIdentity> pageRecords = identities.subList(fromIndex, toIndex);
            
            Map<String, Object> result = new HashMap<>();
            result.put("records", pageRecords);
            result.put("total", (long) total);
            result.put("current", page.longValue());
            result.put("size", size.longValue());
            
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取认证详情
     */
    @RequireRole({"admin"})
    @GetMapping("/{id}")
    public Result getIdentityDetail(@PathVariable Long id) {
        try {
            UserIdentity identity = userIdentityService.getById(id);
            if (identity == null) {
                return Result.error("记录不存在");
            }
            return Result.success(identity);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量审核通过
     */
    @RequireRole({"admin"})
    @PostMapping("/batch-approve")
    public Result batchApprove(
            @RequestBody List<Long> ids,
            HttpServletRequest request) {
        try {
            Long reviewerId = (Long) request.getAttribute("userId");
            
            for (Long id : ids) {
                userIdentityService.verifyIdentity(id, true, null, reviewerId);
            }
            
            return Result.success("批量通过成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量操作失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量拒绝
     */
    @RequireRole({"admin"})
    @PostMapping("/batch-reject")
    public Result batchReject(
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        try {
            Long reviewerId = (Long) request.getAttribute("userId");
            List<Long> ids = (List<Long>) params.get("ids");
            String reason = (String) params.get("reason");
            
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要拒绝的申请");
            }
            
            for (Long id : ids) {
                userIdentityService.verifyIdentity(id, false, reason, reviewerId);
            }
            
            return Result.success("批量拒绝成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量操作失败：" + e.getMessage());
        }
    }
    
    /**
     * 撤销认证
     */
    @RequireRole({"admin"})
    @PostMapping("/{id}/revoke")
    public Result revokeIdentity(@PathVariable Long id) {
        try {
            UserIdentity identity = userIdentityService.getById(id);
            if (identity == null) {
                return Result.error("记录不存在");
            }
            
            identity.setVerified(0);
            identity.setVerifiedAt(null);
            userIdentityService.updateById(identity);
            
            return Result.success("已撤销认证");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("撤销失败：" + e.getMessage());
        }
    }
    
    /**
     * 统计信息
     */
    @RequireRole({"admin"})
    @GetMapping("/stats")
    public Result getStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 总申请数
            long total = userIdentityService.count();
            stats.put("total", total);
            
            // 已通过数
            long verified = userIdentityService.lambdaQuery()
                .eq(UserIdentity::getVerified, 1)
                .count();
            stats.put("verified", verified);
            
            // 待审核数
            long pending = userIdentityService.lambdaQuery()
                .eq(UserIdentity::getVerified, 0)
                .count();
            stats.put("pending", pending);
            
            // 按类型统计
            Map<String, Long> byType = new HashMap<>();
            List<String> types = List.of("student", "staff", "merchant", "organization");
            for (String type : types) {
                long count = userIdentityService.lambdaQuery()
                    .eq(UserIdentity::getIdentityType, type)
                    .count();
                byType.put(type, count);
            }
            stats.put("byType", byType);
            
            return Result.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取统计失败：" + e.getMessage());
        }
    }
}
