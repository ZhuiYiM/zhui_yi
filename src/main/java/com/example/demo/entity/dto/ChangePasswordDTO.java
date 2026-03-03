// entity/dto/ChangePasswordDTO.java
package com.example.demo.entity.dto;

import lombok.Data;

@Data
public class ChangePasswordDTO {
    private String oldPassword;
    private String newPassword;
}
