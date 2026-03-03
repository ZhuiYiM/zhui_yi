package com.example.demo.entity.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String email;
    private String verificationCode;
    private String password;
}
