package com.changzheng.vo;

import com.changzheng.common.ProfileDefaults;
import com.changzheng.entity.User;

public record UserProfileVO(Long userId, String nickname, String avatarUrl) {
    public static UserProfileVO from(User user) {
        return new UserProfileVO(user.getId(), ProfileDefaults.nickname(user.getNickname()), user.getAvatarUrl());
    }
}
