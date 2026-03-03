package com.example.demo.entity.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private Integer userId;
    private String username;
    private String email;

    public LoginResponseDTO(String token, Integer userId, String username, String email) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
}
