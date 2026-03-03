// entity/dto/PrivacySettingsDTO.java
package com.example.demo.entity.dto;

import lombok.Data;
import java.util.Map;

@Data
public class PrivacySettingsDTO {
    private Map<String, Boolean> privacySettings;
    private String defaultPrivacyLevel;
}
