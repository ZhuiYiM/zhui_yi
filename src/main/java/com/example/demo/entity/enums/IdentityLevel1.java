package com.example.demo.entity.enums;

/**
 * 一级身份标签枚举
 */
public enum IdentityLevel1 {
    ADMIN("admin", "管理员", "🛡️"),
    STUDENT("student", "学生", "👨‍🎓"),
    MERCHANT("merchant", "校外商户", "🏪"),
    ORGANIZATION("organization", "团体", "👥"),
    INDIVIDUAL("individual", "校外个人", "👤"),
    SOCIETY("society", "社会", "🌍");

    private final String code;
    private final String name;
    private final String icon;

    IdentityLevel1(String code, String name, String icon) {
        this.code = code;
        this.name = name;
        this.icon = icon;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    /**
     * 根据代码获取身份标签
     */
    public static IdentityLevel1 fromCode(String code) {
        for (IdentityLevel1 identity : values()) {
            if (identity.getCode().equals(code)) {
                return identity;
            }
        }
        // 默认返回社会
        return SOCIETY;
    }
}
