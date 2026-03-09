// entity/dto/PrivacySettingsVO.java
package com.example.demo.entity.dto;

import lombok.Data;

/**
 * 隐私设置视图对象
 */
@Data
public class PrivacySettingsVO {
    
    private String phoneVisibility;
    private String emailVisibility;
    private String studentIdVisibility;
    private String realNameVisibility;
}
