package com.changzheng.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wechat_sessions")
public class WechatSession {
    @TableId(type=IdType.AUTO) private Long id;
    @TableField("user_id") private Long userId;
    private String code;
    private String openid;
    @TableField("session_key") private String sessionKey;
    @TableField("expires_at") private LocalDateTime expiresAt;
    @TableField("created_at") private LocalDateTime createdAt;
    @TableField("updated_at") private LocalDateTime updatedAt;
}
