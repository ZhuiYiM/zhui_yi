// entity/UserBlock.java
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户拉黑关系实体类
 */
@Data
@TableName("user_blocks")
public class UserBlock {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer blockerId;        // 拉黑者的用户 ID
    private Integer blockedId;        // 被拉黑者的用户 ID
    private LocalDateTime createdAt;  // 创建时间
    private LocalDateTime updatedAt;  // 更新时间
}
