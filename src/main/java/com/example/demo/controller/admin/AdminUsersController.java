package com.example.demo.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.entity.User;
import com.example.demo.entity.admin.OperationLog;
import com.example.demo.service.admin.OperationLogService;
import com.example.demo.service.UserService;
import com.example.demo.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin
public class AdminUsersController {

    @Autowired
    private UserService userService;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private HttpServletRequest request;

    /**
     * 记录操作日志
     */
    private void logOperation(String operation, String module, Long targetId, String detail) {
        try {
            OperationLog log = new OperationLog();
            log.setAdminId(jwtUtil.getCurrentAdminId());
            log.setAdminName(jwtUtil.getCurrentAdminUsername());
            log.setOperation(operation);
            log.setModule(module);
            log.setTargetId(targetId);
            log.setDetail(detail);
            log.setIpAddress(request.getRemoteAddr());
            log.setCreatedAt(LocalDateTime.now());
            operationLogService.save(log);
        } catch (Exception e) {
            log.error("记录操作日志失败：{}", e.getMessage());
        }
    }

    /**
     * 分页查询用户列表
     */
    @GetMapping
    public Result list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer verified,
            @RequestParam(required = false) Integer status
    ) {
        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            
            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.and(wrapper -> 
                    wrapper.like("username", keyword)
                           .or()
                           .like("real_name", keyword)
                           .or()
                           .like("student_id", keyword)
                );
            }
            
            // 认证状态筛选
            if (verified != null) {
                queryWrapper.eq("is_verified", verified);
            }
            
            // 用户状态筛选
            if (status != null) {
                queryWrapper.eq("status", status);
            }
            
            // 按创建时间倒序
            queryWrapper.orderByDesc("create_time");
            
            Page<User> userPage = new Page<>(page, size);
            Page<User> result = userService.page(userPage, queryWrapper);
            
            return Result.success(Map.of(
                "records", result.getRecords(),
                "total", result.getTotal(),
                "page", page,
                "size", size
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            return Result.success(user);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> params) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            Integer status = params.get("status");
            if (status == null) {
                return Result.error("状态不能为空");
            }
            
            user.setStatus(status);
            boolean success = userService.updateById(user);
            if (success) {
                // 记录操作日志
                logOperation("update", "user", Long.valueOf(id), "更新用户状态：" + (status == 1 ? "启用" : "禁用"));
                return Result.success("更新成功", user);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            boolean success = userService.removeById(id);
            if (success) {
                // 记录操作日志
                logOperation("delete", "user", Long.valueOf(id), "删除用户，ID: " + id);
                return Result.success("删除成功", null);
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 审核用户身份证认证
     */
    @PutMapping("/{id}/idcard-verify")
    public Result verifyIdCard(@PathVariable Integer id, @RequestBody Map<String, Object> params) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            Boolean pass = (Boolean) params.get("pass");
            String reason = (String) params.get("reason");
            
            if (pass != null && pass) {
                user.setIsVerified(1);
            } else {
                user.setIsVerified(0);
            }
            
            boolean success = userService.updateById(user);
            if (success) {
                // 记录操作日志
                logOperation("audit", "verification", Long.valueOf(id), "审核用户身份证认证：" + (pass ? "通过" : "拒绝") + (reason != null ? ", 原因：" + reason : ""));
                return Result.success("审核成功", user);
            } else {
                return Result.error("审核失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    /**
     * 审核用户实名认证
     */
    @PutMapping("/{id}/realname-verify")
    public Result verifyRealName(@PathVariable Integer id, @RequestBody Map<String, Object> params) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            Boolean pass = (Boolean) params.get("pass");
            String reason = (String) params.get("reason");
            
            if (pass != null && pass) {
                user.setIsRealNameVerified(1);
            } else {
                user.setIsRealNameVerified(0);
            }
            
            boolean success = userService.updateById(user);
            if (success) {
                // 记录操作日志
                logOperation("audit", "verification", Long.valueOf(id), "审核用户实名认证：" + (pass ? "通过" : "拒绝") + (reason != null ? ", 原因：" + reason : ""));
                return Result.success("审核成功", user);
            } else {
                return Result.error("审核失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("审核失败：" + e.getMessage());
        }
    }
}
