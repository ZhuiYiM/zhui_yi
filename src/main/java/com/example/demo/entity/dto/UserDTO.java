package com.example.demo.entity.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Integer id;
    private String username;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private String realName;
    private String studentId;
    private String gender;
    private LocalDate birthDate;
    private String college;
    private String major;
    private String bio;
    private String hobbies;
    private Integer status;
    private Integer isVerified;
    private Integer isRealNameVerified;
    private String role;
    private Integer isAdmin;
    private Integer isMerchant;
    private Integer isOrganization;
}
