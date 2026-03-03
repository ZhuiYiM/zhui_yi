package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;

import java.time.LocalDateTime;

@Data
@TableName("campuses")
public class Campus {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String description;
    private String locationInfo; // 经纬度等位置信息
    private LocalDateTime createdAt;
}
