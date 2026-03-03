package com.example.demo.entity.enums;

/**
 * 认证类型枚举
 */
public enum VerificationType {
    STUDENT_ID("student_id", "学生身份认证"),
    ID_CARD("id_card", "身份证实名认证"),
    REAL_NAME("real_name", "真实姓名认证");

    private final String code;
    private final String description;

    VerificationType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static VerificationType fromCode(String code) {
        for (VerificationType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid verification type code: " + code);
    }
}