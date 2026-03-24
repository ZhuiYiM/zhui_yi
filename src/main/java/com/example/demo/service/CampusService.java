package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.Campus;
import com.example.demo.common.Result;
import java.util.List;
import java.util.Map;

public interface CampusService extends IService<Campus> {
    Result getCampuses();
    Result getLocationsByCampusId(Integer campusId);
    Result getLocationById(Integer locationId);
    Result getCampusById(Integer campusId);
    Result searchLocations(String keyword, Integer campusId);
    Result getPopularLocationsByCampusId(Integer campusId);
    Result getMapConfig(Integer campusId, String mapType);
    
    // 创建自定义地点
    Result createCustomLocation(String name, String description, String category, Integer campusId);
}
