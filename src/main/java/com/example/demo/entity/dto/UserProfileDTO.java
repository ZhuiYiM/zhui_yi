// entity/dto/UserProfileDTO.java
package com.example.demo.entity.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class UserProfileDTO {
    private String avatarUrl;      // 头像URL
    private String realName;       // 真实姓名
    private String gender;         // 性别
    private LocalDate birthDate;   // 生日
    private String college;        // 学院
    private String major;          // 专业班级
    private String bio;            // 个人简介
    private List<String> hobbies; // 修改为 List<String>
}
