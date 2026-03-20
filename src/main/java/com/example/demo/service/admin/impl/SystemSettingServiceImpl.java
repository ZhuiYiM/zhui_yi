package com.example.demo.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.admin.SystemSetting;
import com.example.demo.mapper.admin.SystemSettingMapper;
import com.example.demo.service.admin.SystemSettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统设置服务实现类
 */
@Service
public class SystemSettingServiceImpl extends ServiceImpl<SystemSettingMapper, SystemSetting> implements SystemSettingService {
    
    @Override
    public Map<String, String> getAllSettings() {
        Map<String, String> settings = new HashMap<>();
        List<SystemSetting> settingList = list();
        for (SystemSetting setting : settingList) {
            settings.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return settings;
    }
    
    @Override
    @Transactional
    public void updateSettings(Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            QueryWrapper<SystemSetting> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("setting_key", entry.getKey());
            
            SystemSetting setting = getOne(queryWrapper);
            if (setting != null) {
                setting.setSettingValue(entry.getValue());
                updateById(setting);
            } else {
                setting = new SystemSetting();
                setting.setSettingKey(entry.getKey());
                setting.setSettingValue(entry.getValue());
                save(setting);
            }
        }
    }
}
