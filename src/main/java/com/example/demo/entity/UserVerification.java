package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_verifications")
public class UserVerification {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String verificationType;    // student_id, id_card, real_name
    private String status;              // pending, approved, rejected
    private String studentId;
    private String realName;
    private String idCard;
    private String college;
    private String rejectionReason;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private Integer reviewerId;
    
    // Getter and Setter methods
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    
    public String getVerificationType() { return verificationType; }
    public void setVerificationType(String verificationType) { this.verificationType = verificationType; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    
    public Integer getReviewerId() { return reviewerId; }
    public void setReviewerId(Integer reviewerId) { this.reviewerId = reviewerId; }
}
