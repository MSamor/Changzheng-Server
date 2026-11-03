package com.changzheng.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;

import lombok.Data;

@Data
public class SaveUserInfoRequest {
    @NotNull @Positive private Long userId;
    @Size(max = 512, message = "头像地址过长") private String avatarUrl;
    private String city;
    private String country;
    private Integer gender;
    @Size(max = 32, message = "昵称不能超过32个字符") private String nickname;
    private String province;
}
