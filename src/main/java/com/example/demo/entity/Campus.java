package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("campuses")
public class Campus {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String code; // 校区编码（main/pingyuan/innovation）
    private String description;
    private String address; // 校区地址
    private BigDecimal centerLatitude; // 中心点纬度
    private BigDecimal centerLongitude; // 中心点经度
    private Integer zoomLevel; // 默认缩放级别
    private String mapConfig; // JSON 格式存储地图配置
    private Boolean isActive; // 是否启用
    private Integer sortOrder; // 排序顺序
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
