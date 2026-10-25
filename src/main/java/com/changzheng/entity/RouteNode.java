package com.changzheng.entity;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("route_nodes")
public class RouteNode {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("node_order")
    private Integer nodeOrder;

    @TableField("local_id")
    private String localId;

    @TableField("city_name")
    private String cityName;

    private BigDecimal longitude;
    private BigDecimal latitude;

    @TableField("required_mileage")
    private Integer requiredMileage;

    @TableField("badge_code")
    private String badgeCode;

    @TableField("city_image")
    private String cityImage;

    @TableField("city_detail")
    private String cityDetail;

    private Integer enabled;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
