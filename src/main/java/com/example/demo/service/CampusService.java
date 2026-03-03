package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.Campus;
import com.example.demo.common.Result;

public interface CampusService extends IService<Campus> {
    Result getCampuses();
    Result getLocationsByCampusId(Integer campusId);
    Result getLocationById(Integer locationId);
    Result getCampusById(Integer campusId);
    Result searchLocations(String keyword, Integer campusId);
}
