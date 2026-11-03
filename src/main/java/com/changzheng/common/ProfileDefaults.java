package com.changzheng.common;

public final class ProfileDefaults {
    public static final String NICKNAME = "长征参与者";

    private ProfileDefaults() {}

    public static String nickname(String value) {
        return value == null || value.isBlank() || "微信用户".equals(value.trim())
                ? NICKNAME : value;
    }
}
