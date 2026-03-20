package com.example.demo.controller.admin;

import com.example.demo.common.Result;
import com.example.demo.entity.admin.SystemSetting;
import com.example.demo.service.admin.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统设置控制器
 */
@RestController
@RequestMapping("/admin/settings")
@CrossOrigin
public class AdminSettingsController {

    @Autowired
    private SystemSettingService systemSettingService;

    /**
     * 获取所有系统设置
     */
    @GetMapping
    public Result getSettings() {
        try {
            List<SystemSetting> settings = systemSettingService.list();
            return Result.success(settings);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 更新系统设置
     */
    @PutMapping
    public Result updateSettings(@RequestBody Map<String, String> settings) {
        try {
            systemSettingService.updateSettings(settings);
            return Result.success("更新成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败：" + e.getMessage());
        }
    }
}
