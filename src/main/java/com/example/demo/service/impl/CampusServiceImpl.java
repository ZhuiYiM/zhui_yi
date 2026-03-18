package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.Campus;
import com.example.demo.entity.Location;
import com.example.demo.mapper.CampusMapper;
import com.example.demo.mapper.LocationMapper;
import com.example.demo.service.CampusService;
import com.example.demo.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class CampusServiceImpl extends ServiceImpl<CampusMapper, Campus> implements CampusService {

    @Autowired
    private CampusMapper campusMapper;

    @Autowired
    private LocationMapper locationMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Result getCampuses() {
        List<Campus> campuses = this.list();
        return Result.success(campuses);
    }

    @Override
    public Result getLocationsByCampusId(Integer campusId) {
        QueryWrapper<Location> wrapper = new QueryWrapper<>();
        wrapper.eq("campus_id", campusId);

        List<Location> locations = locationMapper.selectList(wrapper);
        return Result.success(locations);
    }

    @Override
    public Result getLocationById(Integer locationId) {
        Location location = locationMapper.selectById(locationId);
        if (location == null) {
            return Result.error("地点不存在");
        }
        return Result.success(location);
    }

    @Override
    public Result getCampusById(Integer campusId) {
        Campus campus = this.getById(campusId);
        if (campus == null) {
            return Result.error("校区不存在");
        }
        return Result.success(campus);
    }

    @Override
    public Result searchLocations(String keyword, Integer campusId) {
        QueryWrapper<Location> wrapper = new QueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("name", keyword).or().like("description", keyword));
        }
        
        if (campusId != null) {
            wrapper.eq("campus_id", campusId);
        }

        List<Location> locations = locationMapper.selectList(wrapper);
        return Result.success(locations);
    }

    @Override
    public Result getPopularLocationsByCampusId(Integer campusId) {
        QueryWrapper<Location> wrapper = new QueryWrapper<>();
        wrapper.eq("campus_id", campusId)
               .eq("is_popular", true)
               .orderByDesc("sort_order")
               .orderByDesc("view_count");
        
        List<Location> locations = locationMapper.selectList(wrapper);
        return Result.success(locations);
    }

    @Override
    public Result getMapConfig(Integer campusId, String mapType) {
        try {
            Campus campus = this.getById(campusId);
            if (campus == null) {
                return Result.error("校区不存在");
            }

            Map<String, Object> config = new HashMap<>();
            
            // 获取校区的地图配置 JSON
            if (campus.getMapConfig() != null && !campus.getMapConfig().isEmpty()) {
                JsonNode mapConfigJson = objectMapper.readTree(campus.getMapConfig());
                
                // 根据地图类型返回对应配置
                if (mapType != null && !mapType.isEmpty()) {
                    JsonNode typeConfig = mapConfigJson.get(mapType.toLowerCase());
                    if (typeConfig != null) {
                        config.put("latitude", typeConfig.get("lat").asDouble());
                        config.put("longitude", typeConfig.get("lng").asDouble());
                        config.put("zoom", typeConfig.get("zoom").asInt());
                    } else {
                        // 如果指定类型不存在，返回默认配置
                        config.put("latitude", campus.getCenterLatitude() != null ? campus.getCenterLatitude().doubleValue() : 35.307736);
                        config.put("longitude", campus.getCenterLongitude() != null ? campus.getCenterLongitude().doubleValue() : 113.926765);
                        config.put("zoom", campus.getZoomLevel() != null ? campus.getZoomLevel() : 15);
                    }
                } else {
                    // 返回所有地图类型的配置
                    config.put("baidu", mapConfigJson.get("baidu"));
                    config.put("gaode", mapConfigJson.get("gaode"));
                    config.put("tencent", mapConfigJson.get("tencent"));
                }
            } else {
                // 如果没有配置，使用默认值
                config.put("latitude", campus.getCenterLatitude() != null ? campus.getCenterLatitude().doubleValue() : 35.307736);
                config.put("longitude", campus.getCenterLongitude() != null ? campus.getCenterLongitude().doubleValue() : 113.926765);
                config.put("zoom", campus.getZoomLevel() != null ? campus.getZoomLevel() : 15);
            }

            config.put("campusName", campus.getName());
            config.put("campusCode", campus.getCode());
            config.put("address", campus.getAddress());

            return Result.success(config);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取地图配置失败：" + e.getMessage());
        }
    }
}
