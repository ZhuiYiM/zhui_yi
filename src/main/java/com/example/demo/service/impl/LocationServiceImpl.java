package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.Location;
import com.example.demo.mapper.LocationMapper;
import com.example.demo.service.LocationService;
import org.springframework.stereotype.Service;

@Service
public class LocationServiceImpl extends ServiceImpl<LocationMapper, Location> implements LocationService {
}
