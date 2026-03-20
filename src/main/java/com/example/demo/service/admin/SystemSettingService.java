package com.example.demo.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.admin.SystemSetting;

import java.util.Map;

/**
 * 系统设置服务接口
 */
public interface SystemSettingService extends IService<SystemSetting> {
    
    /**
     * 获取所有设置
     */
    Map<String, String> getAllSettings();
    
    /**
     * 更新设置
     */
    void updateSettings(Map<String, String> settings);
}
