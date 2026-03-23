package com.example.demo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.ApiResult;
import com.example.demo.dto.AdvertisementDTO;
import com.example.demo.dto.AdvertisementQueryDTO;
import com.example.demo.entity.Advertisement;

/**
 * 广告服务接口
 */
public interface AdvertisementService {

    /**
     * 分页查询广告列表
     */
    ApiResult getAdvertisements(AdvertisementQueryDTO queryDTO);

    /**
     * 根据 ID 获取广告详情
     */
    ApiResult getAdvertisementById(Long id);

    /**
     * 创建广告
     */
    ApiResult createAdvertisement(AdvertisementDTO advertisementDTO, Long userId);

    /**
     * 更新广告
     */
    ApiResult updateAdvertisement(Long id, AdvertisementDTO advertisementDTO, Long userId);

    /**
     * 删除广告
     */
    ApiResult deleteAdvertisement(Long id);

    /**
     * 批量删除广告
     */
    ApiResult batchDeleteAdvertisements(String ids);

    /**
     * 更新广告状态
     */
    ApiResult updateAdvertisementStatus(Long id, Integer isActive);

    /**
     * 增加点击次数
     */
    void incrementClickCount(Long id);

    /**
     * 增加展示次数
     */
    void incrementViewCount(Long id);
}
