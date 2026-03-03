package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerificationCode {
    private String code;
    private LocalDateTime expireTime;

    public VerificationCode(String code, int expireMinutes) {
        this.code = code;
        this.expireTime = LocalDateTime.now().plusMinutes(expireMinutes);
    }

    @JsonProperty // 明确指定该方法不是序列化属性
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireTime);
    }
}