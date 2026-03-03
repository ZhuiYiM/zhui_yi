// entity/dto/DeviceDTO.java
package com.example.demo.entity.dto;

import lombok.Data;

@Data
public class DeviceDTO {
    private Integer id;
    private String deviceType;
    private String browserInfo;
    private String ipAddress;
    private String lastLoginTime;
    private Boolean isCurrent;
}
