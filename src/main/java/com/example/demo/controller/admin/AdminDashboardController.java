package com.example.demo.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.common.ApiResult;
import com.example.demo.entity.Report;
import com.example.demo.service.UserService;
import com.example.demo.service.TopicsService;
import com.example.demo.service.ProductService;
import com.example.demo.service.LocationService;
import com.example.demo.service.ReportService;
import com.example.demo.service.TopicCommentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin
public class AdminDashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private TopicsService topicsService;

    @Autowired
    private ProductService productService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private TopicCommentsService topicCommentsService;

    /**
     * 获取首页统计数据
     */
    @GetMapping("/stats")
    public ApiResult getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 用户总数
            Long userCount = userService.lambdaQuery().count();
            stats.put("userCount", userCount);
            
            // 话题总数
            Long topicCount = topicsService.lambdaQuery().count();
            stats.put("topicCount", topicCount);
            
            // 商品总数
            Long productCount = productService.lambdaQuery().count();
            stats.put("productCount", productCount);
            
            // 地点总数
            Long locationCount = locationService.lambdaQuery().count();
            stats.put("locationCount", locationCount);
            
            // 待处理举报数
            Long pendingReportCount = reportService.lambdaQuery().eq(Report::getStatus, 0).count();
            stats.put("pendingReportCount", pendingReportCount);
            
            // 评论总数
            Long commentCount = topicCommentsService.lambdaQuery().count();
            stats.put("commentCount", commentCount);
            
            return ApiResult.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResult.error(500, "获取统计数据失败：" + e.getMessage());
        }
    }
}
