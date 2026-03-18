// entity/dto/BlockListDTO.java
package com.example.demo.entity.dto;

import lombok.Data;

/**
 * 拉黑列表用户信息 DTO
 */
@Data
public class BlockListDTO {
    private Integer id;           // 拉黑记录 ID
    private Integer blockedId;    // 被拉黑者的用户 ID
    private String blockedName;   // 被拉黑者的用户名
    private String blockedAvatar; // 被拉黑者的头像
    private String createdAt;     // 拉黑时间
}
