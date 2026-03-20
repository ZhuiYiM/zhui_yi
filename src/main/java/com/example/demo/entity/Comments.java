// entity/Comments.java
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comments {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer topicId;
    private Integer userId;
    private Integer parentId; // 父评论 ID，用于嵌套回复
    private String content;
    private Integer likeCount;
    private Integer status; // 状态：0-待审核，1-正常，-1-已删除
    private LocalDateTime createdAt;
}
