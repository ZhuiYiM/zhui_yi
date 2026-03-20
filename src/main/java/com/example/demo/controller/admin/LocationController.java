package com.example.demo.controller.admin;

import com.example.demo.common.Result;
import com.example.demo.entity.Location;
import com.example.demo.service.LocationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/locations")
@CrossOrigin
public class LocationController {

    @Autowired
    private LocationService locationService;

    /**
     * 分页查询地点列表
     */
    @GetMapping("/list")
    public Result list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer campusId,
            @RequestParam(required = false) String category
    ) {
        try {
            System.out.println("📍 地点列表分页参数：page=" + page + ", limit=" + limit);
            System.out.println("📍 地点列表筛选参数：keyword=" + keyword + ", campusId=" + campusId + ", category=" + category);
            
            QueryWrapper<Location> queryWrapper = new QueryWrapper<>();
            
            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.and(wrapper -> 
                    wrapper.like("name", keyword)
                           .or()
                           .like("description", keyword)
                );
            }
            
            // 按校区筛选
            if (campusId != null) {
                queryWrapper.eq("campus_id", campusId);
            }
            
            // 按分类筛选
            if (category != null && !category.isEmpty()) {
                queryWrapper.eq("category", category);
            }
            
            queryWrapper.orderByDesc("created_at");
            
            Page<Location> locationPage = new Page<>(page, limit);
            Page<Location> result = locationService.page(locationPage, queryWrapper);
            
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
     * 获取地点详情
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        try {
            Location location = locationService.getById(id);
            if (location == null) {
                return Result.error("地点不存在");
            }
            return Result.success(location);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 添加地点
     */
    @PostMapping
    public Result add(@RequestBody Location location) {
        try {
            // 设置默认值
            if (location.getViewCount() == null) {
                location.setViewCount(0);
            }
            if (location.getSortOrder() == null) {
                location.setSortOrder(0);
            }
            if (location.getIsPopular() == null) {
                location.setIsPopular(false);
            }
            
            boolean success = locationService.save(location);
            if (success) {
                return Result.success("添加成功", location);
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 更新地点
     */
    @PutMapping("/{id}")
    public Result update(@PathVariable Integer id, @RequestBody Location location) {
        try {
            location.setId(id);
            boolean success = locationService.updateById(location);
            if (success) {
                return Result.success("更新成功", location);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除地点
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        try {
            boolean success = locationService.removeById(id);
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
     * 批量删除地点
     */
    @DeleteMapping("/batch")
    public Result batchDelete(@RequestBody Map<String, List<Integer>> params) {
        try {
            List<Integer> ids = params.get("ids");
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的地点");
            }
            
            boolean success = locationService.removeByIds(ids);
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
     * 获取所有分类
     */
    @GetMapping("/categories")
    public Result getCategories() {
        try {
            List<String> categories = locationService.list()
                .stream()
                .map(Location::getCategory)
                .filter(category -> category != null && !category.isEmpty())
                .distinct()
                .toList();
            
            return Result.success(categories);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
