package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.CampusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campuses")
@CrossOrigin
public class CampusController {

    @Autowired
    private CampusService campusService;

    // 获取校区列表
    @GetMapping
    public Result getCampuses() {
        return campusService.getCampuses();
    }

    // 获取地点信息
    @GetMapping("/locations")
    public Result getLocations(@RequestParam Integer campusId) {
        return campusService.getLocationsByCampusId(campusId);
    }

    // 获取特定地点详情
    @GetMapping("/locations/{locationId}")
    public Result getLocationDetail(@PathVariable Integer locationId) {
        return campusService.getLocationById(locationId);
    }
}
