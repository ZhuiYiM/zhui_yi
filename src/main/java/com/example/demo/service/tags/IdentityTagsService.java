package com.example.demo.service.tags;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.common.ApiResult;
import com.example.demo.entity.IdentityTags;

import java.util.List;

/**
 * 身份标签服务接口
 */
public interface IdentityTagsService extends IService<IdentityTags> {
    
    /**
     * 获取所有身份标签
     */
    ApiResult getAllIdentityTags();
}
