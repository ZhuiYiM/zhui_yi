package com.example.demo.entity.enums;

/**
 * 认证状态枚举
 */
public enum VerificationStatus {
    PENDING("pending", "待审核"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "已拒绝");

    private final String code;
    private final String description;

    VerificationStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static VerificationStatus fromCode(String code) {
        for (VerificationStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid verification status code: " + code);
    }
}