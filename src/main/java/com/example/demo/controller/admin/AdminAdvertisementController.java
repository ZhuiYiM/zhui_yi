package com.example.demo.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.ApiResult;
import com.example.demo.dto.AdvertisementDTO;
import com.example.demo.dto.AdvertisementQueryDTO;
import com.example.demo.entity.Advertisement;
import com.example.demo.entity.User;
import com.example.demo.service.AdvertisementService;
import com.example.demo.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 广告管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/advertisements")
@RequiredArgsConstructor
public class AdminAdvertisementController {

    private final AdvertisementService advertisementService;
    private final JwtUtil jwtUtil;

    /**
     * 分页查询广告列表
     */
    @GetMapping
    public ApiResult getAdvertisements(AdvertisementQueryDTO queryDTO) {
        log.info("管理员查询广告列表，query={}", queryDTO);
        return advertisementService.getAdvertisements(queryDTO);
    }

    /**
     * 根据 ID 获取广告详情
     */
    @GetMapping("/{id}")
    public ApiResult getAdvertisement(@PathVariable Long id) {
        log.info("管理员获取广告详情，id={}", id);
        return advertisementService.getAdvertisementById(id);
    }

    /**
     * 创建广告
     */
    @PostMapping
    public ApiResult createAdvertisement(
            @RequestBody AdvertisementDTO advertisementDTO,
            HttpServletRequest request) {
        log.info("管理员创建广告，title={}", advertisementDTO.getTitle());
        
        // 从 JWT Token 中获取用户 ID
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // TODO: 从 token 中获取用户 ID
        Long userId = 1L; // 默认使用 admin
        
        return advertisementService.createAdvertisement(advertisementDTO, userId);
    }

    /**
     * 更新广告
     */
    @PutMapping("/{id}")
    public ApiResult updateAdvertisement(
            @PathVariable Long id,
            @RequestBody AdvertisementDTO advertisementDTO,
            HttpServletRequest request) {
        log.info("管理员更新广告，id={}, title={}", id, advertisementDTO.getTitle());
        
        // 从 JWT Token 中获取用户 ID
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // TODO: 从 token 中获取用户 ID
        Long userId = 1L; // 默认使用 admin
        
        return advertisementService.updateAdvertisement(id, advertisementDTO, userId);
    }

    /**
     * 删除广告
     */
    @DeleteMapping("/{id}")
    public ApiResult deleteAdvertisement(@PathVariable Long id) {
        log.info("管理员删除广告，id={}", id);
        return advertisementService.deleteAdvertisement(id);
    }

    /**
     * 批量删除广告
     */
    @DeleteMapping("/batch")
    public ApiResult batchDeleteAdvertisements(@RequestParam String ids) {
        log.info("管理员批量删除广告，ids={}", ids);
        return advertisementService.batchDeleteAdvertisements(ids);
    }

    /**
     * 更新广告状态
     */
    @PutMapping("/{id}/status")
    public ApiResult updateAdvertisementStatus(
            @PathVariable Long id,
            @RequestParam Integer isActive) {
        log.info("管理员更新广告状态，id={}, isActive={}", id, isActive);
        return advertisementService.updateAdvertisementStatus(id, isActive);
    }
}
