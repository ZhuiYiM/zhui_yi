package com.example.demo.entity.dto.admin;

import lombok.Data;

@Data
public class AdminLoginDTO {
    private String username;
    private String password;
    private String captcha;
}
