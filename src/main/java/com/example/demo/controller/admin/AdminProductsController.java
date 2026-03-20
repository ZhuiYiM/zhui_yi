package com.example.demo.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin
public class AdminProductsController {

    @Autowired
    private ProductService productService;

    /**
     * 分页查询商品列表
     */
    @GetMapping
    public Result list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword
    ) {
        try {
            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
            
            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.and(wrapper -> 
                    wrapper.like("title", keyword)
                           .or()
                           .like("description", keyword)
                );
            }
            
            queryWrapper.orderByDesc("created_at");
            
            Page<Product> productPage = new Page<>(page, size);
            Page<Product> result = productService.page(productPage, queryWrapper);
            
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
     * 下架商品
     */
    @PutMapping("/{id}/remove")
    public Result remove(@PathVariable Integer id) {
        try {
            Product product = productService.getById(id);
            if (product == null) {
                return Result.error("商品不存在");
            }
            
            // 下架：设置状态为 0
            product.setStatus(0);
            boolean success = productService.updateById(product);
            if (success) {
                return Result.success("下架成功", product);
            } else {
                return Result.error("下架失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("下架失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品状态
     */
    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> params) {
        try {
            Product product = productService.getById(id);
            if (product == null) {
                return Result.error("商品不存在");
            }
            
            Integer status = params.get("status");
            if (status == null || (status != 0 && status != 1)) {
                return Result.error("状态参数错误");
            }
            
            product.setStatus(status);
            boolean success = productService.updateById(product);
            if (success) {
                return Result.success("状态更新成功", product);
            } else {
                return Result.error("状态更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("状态更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        try {
            boolean success = productService.removeById(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}
