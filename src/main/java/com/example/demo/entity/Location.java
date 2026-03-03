// entity/Location.java
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("locations")
public class Location {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer campusId;
    private String name;
    private String description;
    private String coordinates; // JSON格式存储经纬度
    private String icon;
    private String locationType; // 教学楼、食堂、宿舍等
    private LocalDateTime createdAt;
}
