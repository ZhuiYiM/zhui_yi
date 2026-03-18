// entity/dto/BlockCreateDTO.java
package com.example.demo.entity.dto;

import lombok.Data;

/**
 * 拉黑用户请求 DTO
 */
@Data
public class BlockCreateDTO {
    private Integer blockedId;  // 被拉黑者的用户 ID
}
