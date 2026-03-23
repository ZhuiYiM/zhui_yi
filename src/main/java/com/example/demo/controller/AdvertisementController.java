package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.common.ApiResult;
import com.example.demo.entity.Advertisement;
import com.example.demo.mapper.AdvertisementMapper;
import com.example.demo.service.AdvertisementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 广告公开接口控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/advertisements")
@RequiredArgsConstructor
public class AdvertisementController {

    private final AdvertisementMapper advertisementMapper;
    private final AdvertisementService advertisementService;

    /**
     * 获取指定位置的广告列表（公开接口）
     */
    @GetMapping("/{position}")
    public ApiResult getAdvertisementsByPosition(@PathVariable String position) {
        try {
            log.info("获取{}位置的广告列表", position);
            
            // 构建查询条件
            LambdaQueryWrapper<Advertisement> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Advertisement::getPosition, position)
                    .eq(Advertisement::getIsActive, 1) // 只查询启用的广告
                    .le(Advertisement::getStartTime, LocalDateTime.now()) // 开始时间小于等于当前时间
                    .ge(Advertisement::getEndTime, LocalDateTime.now()) // 结束时间大于等于当前时间
                    .orderByAsc(Advertisement::getSortOrder)
                    .orderByDesc(Advertisement::getCreatedAt);
            
            // 执行查询
            List<Advertisement> advertisements = advertisementMapper.selectList(wrapper);
            
            log.info("查询到{}条广告数据", advertisements.size());
            
            return ApiResult.success(advertisements);
        } catch (Exception e) {
            log.error("获取广告列表失败，position={}", position, e);
            return ApiResult.error("获取广告列表失败：" + e.getMessage());
        }
    }

    /**
     * 增加广告点击次数（公开接口）
     */
    @PostMapping("/{id}/click")
    public ApiResult incrementClickCount(@PathVariable Long id) {
        try {
            log.info("增加广告点击次数，id={}", id);
            advertisementService.incrementClickCount(id);
            return ApiResult.success();
        } catch (Exception e) {
            log.error("增加点击次数失败，id={}", id, e);
            return ApiResult.error("增加点击次数失败：" + e.getMessage());
        }
    }

    /**
     * 增加广告展示次数（内部调用）
     */
    public void incrementViewCount(Long id) {
        advertisementService.incrementViewCount(id);
    }
}
