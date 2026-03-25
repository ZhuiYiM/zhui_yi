package com.example.demo.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.entity.UserVerification;
import com.example.demo.mapper.UserVerificationMapper;
import com.example.demo.service.admin.impl.AdminUserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/verifications")
@CrossOrigin
public class AdminVerificationsController {

    @Autowired
    private UserVerificationMapper userVerificationMapper;
    
    @Autowired
    private AdminUserServiceImpl adminUserService;

    /**
     * 分页查询认证申请列表
     */
    @GetMapping
    public Result list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String status
    ) {
        try {
            QueryWrapper<UserVerification> queryWrapper = new QueryWrapper<>();
            
            // 状态筛选
            if (status != null && !status.isEmpty()) {
                queryWrapper.eq("status", status);
            } else {
                // 默认只显示待审核的
                queryWrapper.eq("status", "pending");
            }
            
            queryWrapper.orderByDesc("submitted_at");
            
            Page<UserVerification> verificationPage = new Page<>(page, size);
            Page<UserVerification> result = userVerificationMapper.selectPage(verificationPage, queryWrapper);
            
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
     * 获取认证申请详情
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        try {
            UserVerification verification = userVerificationMapper.selectById(id);
            if (verification == null) {
                return Result.error("认证申请不存在");
            }
            return Result.success(verification);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 通过认证申请
     */
    @PutMapping("/{id}/approve")
    public Result approve(@PathVariable Integer id, @RequestBody Map<String, String> params) {
        try {
            String adminRemark = params.get("adminRemark");
            
            // 调用 Service 层处理审核逻辑（包含更新用户身份）
            Result result = adminUserService.approveApplication(id, adminRemark);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    /**
     * 拒绝认证申请
     */
    @PutMapping("/{id}/reject")
    public Result reject(@PathVariable Integer id, @RequestBody Map<String, String> params) {
        try {
            String reason = params.get("reason");
            
            // 调用 Service 层处理拒绝逻辑
            Result result = adminUserService.rejectApplication(id, reason);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("操作失败：" + e.getMessage());
        }
    }
}
