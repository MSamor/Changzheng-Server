package com.changzheng.entity;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String openid;
    private String unionid;
    private String nickname;

    @TableField("avatar_url")
    private String avatarUrl;

    private String country;
    private String province;
    private String city;
    private Integer gender;
    private Integer status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
