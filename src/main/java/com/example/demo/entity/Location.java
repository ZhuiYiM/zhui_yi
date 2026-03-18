// entity/Location.java
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("locations")
public class Location {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer campusId;
    private String name;
    private String description;
    private String category; // 地点分类
    private BigDecimal latitude; // 纬度
    private BigDecimal longitude; // 经度
    private String icon;
    private String imageUrl; // 地点图片 URL
    private String openingHours; // 开放时间
    private String contactInfo; // 联系方式
    private String facilities; // JSON 格式存储设施标签
    private Boolean isPopular; // 是否热门
    private Integer viewCount; // 浏览次数
    private Integer sortOrder; // 排序顺序
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
