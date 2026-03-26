package com.example.demo.controller.admin;

import com.example.demo.common.Result;
import com.example.demo.entity.UserLocationMark;
import com.example.demo.service.UserLocationMarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 管理员 - 用户地点标记管理
 */
@RestController
@RequestMapping("/api/admin/user-location-marks")
@CrossOrigin
public class AdminUserLocationMarkController {

    @Autowired
    private UserLocationMarkService userLocationMarkService;

    /**
     * 分页查询用户地点标记列表
     */
    @GetMapping("/list")
    public Result list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer campusId,
            @RequestParam(required = false) String markType,
            @RequestParam(required = false) String verificationStatus
    ) {
        try {
            QueryWrapper<UserLocationMark> queryWrapper = new QueryWrapper<>();
            
            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.and(wrapper -> 
                    wrapper.like("location_name", keyword)
                           .or()
                           .like("description", keyword)
                           .or()
                           .like("address_detail", keyword)
                );
            }
            
            // 按校区筛选
            if (campusId != null) {
                queryWrapper.eq("campus_id", campusId);
            }
            
            // 按标记类型筛选
            if (markType != null && !markType.isEmpty()) {
                queryWrapper.eq("mark_type", markType);
            }
            
            // 按审核状态筛选
            if (verificationStatus != null && !verificationStatus.isEmpty()) {
                queryWrapper.eq("verification_status", verificationStatus);
            }
            
            queryWrapper.orderByDesc("created_at");
            
            Page<UserLocationMark> markPage = new Page<>(page, limit);
            Page<UserLocationMark> result = userLocationMarkService.page(markPage, queryWrapper);
            
            return Result.success(Map.of(
                "list", result.getRecords(),
                "total", result.getTotal(),
                "page", page,
                "limit", limit
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户地点标记详情
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        try {
            UserLocationMark mark = userLocationMarkService.getById(id);
            if (mark == null) {
                return Result.error("标记不存在");
            }
            return Result.success(mark);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 审核用户地点标记
     */
    @PostMapping("/{id}/review")
    public Result review(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String reason
    ) {
        try {
            UserLocationMark mark = userLocationMarkService.getById(id);
            if (mark == null) {
                return Result.error("标记不存在");
            }
            
            // 更新审核状态
            mark.setVerificationStatus(status);
            if ("rejected".equals(status) && reason != null) {
                mark.setReviewComment(reason);
            } else if ("approved".equals(status)) {
                mark.setReviewComment("管理员审核通过");
            }
            mark.setVerifiedAt(java.time.LocalDateTime.now());
            
            userLocationMarkService.updateById(mark);
            
            return Result.success("审核成功", mark);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    /**
     * 删除用户地点标记
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            boolean success = userLocationMarkService.removeById(id);
            if (success) {
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
     * 批量删除用户地点标记
     */
    @DeleteMapping("/batch")
    public Result batchDelete(@RequestBody Map<String, List<Long>> params) {
        try {
            List<Long> ids = params.get("ids");
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的标记");
            }
            
            boolean success = userLocationMarkService.removeByIds(ids);
            if (success) {
                return Result.success("批量删除成功", null);
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有标记类型
     */
    @GetMapping("/mark-types")
    public Result getMarkTypes() {
        try {
            List<String> markTypes = userLocationMarkService.list()
                .stream()
                .map(UserLocationMark::getMarkType)
                .filter(markType -> markType != null && !markType.isEmpty())
                .distinct()
                .toList();
            
            return Result.success(markTypes);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
