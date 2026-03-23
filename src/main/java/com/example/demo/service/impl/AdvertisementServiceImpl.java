package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.ApiResult;
import com.example.demo.dto.AdvertisementDTO;
import com.example.demo.dto.AdvertisementQueryDTO;
import com.example.demo.entity.Advertisement;
import com.example.demo.mapper.AdvertisementMapper;
import com.example.demo.service.AdvertisementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 广告服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdvertisementServiceImpl implements AdvertisementService {

    private final AdvertisementMapper advertisementMapper;

    @Override
    public ApiResult getAdvertisements(AdvertisementQueryDTO queryDTO) {
        try {
            // 构建分页对象
            Page<Advertisement> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
            
            // 构建查询条件
            LambdaQueryWrapper<Advertisement> wrapper = new LambdaQueryWrapper<>();
            
            // 模糊查询标题
            if (StringUtils.hasText(queryDTO.getTitle())) {
                wrapper.like(Advertisement::getTitle, queryDTO.getTitle());
            }
            
            // 精确查询位置
            if (StringUtils.hasText(queryDTO.getPosition())) {
                wrapper.eq(Advertisement::getPosition, queryDTO.getPosition());
            }
            
            // 精确查询状态
            if (queryDTO.getIsActive() != null) {
                wrapper.eq(Advertisement::getIsActive, queryDTO.getIsActive());
            }
            
            // 按排序顺序排序
            wrapper.orderByAsc(Advertisement::getSortOrder)
                    .orderByDesc(Advertisement::getCreatedAt);
            
            // 执行分页查询
            Page<Advertisement> resultPage = advertisementMapper.selectPage(page, wrapper);
            
            return ApiResult.success(resultPage);
        } catch (Exception e) {
            log.error("查询广告列表失败", e);
            return ApiResult.error("查询广告列表失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResult getAdvertisementById(Long id) {
        try {
            Advertisement advertisement = advertisementMapper.selectById(id);
            if (advertisement == null) {
                return ApiResult.error("广告不存在");
            }
            return ApiResult.success(advertisement);
        } catch (Exception e) {
            log.error("获取广告详情失败，id={}", id, e);
            return ApiResult.error("获取广告详情失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult createAdvertisement(AdvertisementDTO advertisementDTO, Long userId) {
        try {
            // 参数校验
            if (!StringUtils.hasText(advertisementDTO.getTitle())) {
                return ApiResult.error("广告标题不能为空");
            }
            if (!StringUtils.hasText(advertisementDTO.getPosition())) {
                return ApiResult.error("广告位置不能为空");
            }
            
            // 转换为实体
            Advertisement advertisement = new Advertisement();
            BeanUtils.copyProperties(advertisementDTO, advertisement);
            advertisement.setCreatedBy(userId);
            advertisement.setUpdatedBy(userId);
            advertisement.setClickCount(0);
            advertisement.setViewCount(0);
            advertisement.setCreatedAt(LocalDateTime.now());
            advertisement.setUpdatedAt(LocalDateTime.now());
            
            // 插入数据库
            advertisementMapper.insert(advertisement);
            
            log.info("创建广告成功，title={}", advertisementDTO.getTitle());
            return ApiResult.success(advertisement);
        } catch (Exception e) {
            log.error("创建广告失败", e);
            return ApiResult.error("创建广告失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult updateAdvertisement(Long id, AdvertisementDTO advertisementDTO, Long userId) {
        try {
            // 检查广告是否存在
            Advertisement existing = advertisementMapper.selectById(id);
            if (existing == null) {
                return ApiResult.error("广告不存在");
            }
            
            // 更新字段
            BeanUtils.copyProperties(advertisementDTO, existing, "id", "createdBy", "createdAt", "clickCount", "viewCount");
            existing.setUpdatedBy(userId);
            existing.setUpdatedAt(LocalDateTime.now());
            
            // 更新数据库
            advertisementMapper.updateById(existing);
            
            log.info("更新广告成功，id={}", id);
            return ApiResult.success(existing);
        } catch (Exception e) {
            log.error("更新广告失败，id={}", id, e);
            return ApiResult.error("更新广告失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult deleteAdvertisement(Long id) {
        try {
            // 检查广告是否存在
            Advertisement existing = advertisementMapper.selectById(id);
            if (existing == null) {
                return ApiResult.error("广告不存在");
            }
            
            // 删除广告
            advertisementMapper.deleteById(id);
            
            log.info("删除广告成功，id={}", id);
            return ApiResult.success();
        } catch (Exception e) {
            log.error("删除广告失败，id={}", id, e);
            return ApiResult.error("删除广告失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult batchDeleteAdvertisements(String ids) {
        try {
            if (!StringUtils.hasText(ids)) {
                return ApiResult.error("请指定要删除的广告 ID");
            }
            
            // 解析 ID 列表
            List<Long> idList = Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .toList();
            
            // 批量删除
            for (Long id : idList) {
                advertisementMapper.deleteById(id);
            }
            
            log.info("批量删除广告成功，ids={}", ids);
            return ApiResult.success();
        } catch (Exception e) {
            log.error("批量删除广告失败，ids={}", ids, e);
            return ApiResult.error("批量删除广告失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult updateAdvertisementStatus(Long id, Integer isActive) {
        try {
            // 检查广告是否存在
            Advertisement existing = advertisementMapper.selectById(id);
            if (existing == null) {
                return ApiResult.error("广告不存在");
            }
            
            // 更新状态
            existing.setIsActive(isActive);
            existing.setUpdatedAt(LocalDateTime.now());
            
            // 更新数据库
            advertisementMapper.updateById(existing);
            
            log.info("更新广告状态成功，id={}", id);
            return ApiResult.success(existing);
        } catch (Exception e) {
            log.error("更新广告状态失败，id={}", id, e);
            return ApiResult.error("更新广告状态失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementClickCount(Long id) {
        try {
            Advertisement advertisement = advertisementMapper.selectById(id);
            if (advertisement != null) {
                advertisement.setClickCount(advertisement.getClickCount() + 1);
                advertisementMapper.updateById(advertisement);
            }
        } catch (Exception e) {
            log.error("增加点击次数失败，id={}", id, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long id) {
        try {
            Advertisement advertisement = advertisementMapper.selectById(id);
            if (advertisement != null) {
                advertisement.setViewCount(advertisement.getViewCount() + 1);
                advertisementMapper.updateById(advertisement);
            }
        } catch (Exception e) {
            log.error("增加展示次数失败，id={}", id, e);
        }
    }
}
