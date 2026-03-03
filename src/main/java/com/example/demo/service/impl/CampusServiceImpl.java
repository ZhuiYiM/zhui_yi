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

import java.util.List;

@Service
public class CampusServiceImpl extends ServiceImpl<CampusMapper, Campus> implements CampusService {

    @Autowired
    private CampusMapper campusMapper;

    @Autowired
    private LocationMapper locationMapper;

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

        java.util.List<Location> locations = locationMapper.selectList(wrapper);
        return Result.success(locations);
    }
}
