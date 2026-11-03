package com.changzheng.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RankUserVO {
    private Long userId;
    private String avatarUrl;
    private String nickname;
    private String arrivedCityName;
    private BigDecimal totalMileage;
}
