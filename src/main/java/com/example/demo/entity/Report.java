package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报记录实体类
 */
@Data
@TableName("reports")
public class Report {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long reporterId; // 举报人 ID
    
    private Long targetId; // 被举报对象 ID（话题 ID、评论 ID、商品 ID、地点 ID）
    
    private String targetType; // 被举报类型：topic-话题、comment-评论、product-商品、location-地点
    
    private String reportType; // 举报类型：pornography-色情低俗、illegal-违法犯罪、political-政治敏感、spam-垃圾广告、fake_info-虚假信息、copyright-侵权内容、harassment-人身攻击、other-其他
    
    private String reason; // 举报原因描述
    
    private String evidence; // 证据（包含图片和其他证据）
    
    private Integer status; // 处理状态：0-待处理、1-已处理、2-已忽略
    
    private LocalDateTime createdAt;
    
    private LocalDateTime processedAt;
    
    private Long processorId; // 处理人 ID（管理员）
    
    private String processResult; // 处理结果说明
}
