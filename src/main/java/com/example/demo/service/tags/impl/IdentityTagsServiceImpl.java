package com.example.demo.service.tags.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.ApiResult;
import com.example.demo.entity.IdentityTags;
import com.example.demo.mapper.IdentityTagsMapper;
import com.example.demo.service.tags.IdentityTagsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 身份标签服务实现类
 */
@Service
public class IdentityTagsServiceImpl extends ServiceImpl<IdentityTagsMapper, IdentityTags> implements IdentityTagsService {
    
    @Override
    public ApiResult getAllIdentityTags() {
        try {
            List<IdentityTags> tags = this.list();
            return ApiResult.success(tags);
        } catch (Exception e) {
            return ApiResult.error(500, "获取身份标签失败：" + e.getMessage());
        }
    }
}
