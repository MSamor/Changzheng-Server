package com.changzheng.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix="wechat")
public class WechatProperties {
    private String appId;
    private String appSecret;
    private String sessionUrl = "https://api.weixin.qq.com/sns/jscode2session";
    private boolean mock = false;
}
