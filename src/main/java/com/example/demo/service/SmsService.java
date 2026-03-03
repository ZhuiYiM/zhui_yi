package com.example.demo.service;

public interface SmsService {
    void sendVerificationCode(String phone);
    VerificationResult validateCode(String phone, String code);

    enum VerificationResult {
        CODE_VALID("验证码有效"),
        CODE_INVALID("验证码错误"),
        CODE_EXPIRED("验证码已过期"),
        CODE_NOT_FOUND("未找到验证码");

        private final String message;

        VerificationResult(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}