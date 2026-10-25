package com.changzheng.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.*;

@Data
@TableName("step_exchanges")
public class StepExchange {
    @TableId(type=IdType.AUTO) private Long id;
    @TableField("user_id") private Long userId;
    @TableField("exchange_date") private LocalDate exchangeDate;
    @TableField("step_count") private Integer stepCount;
    private Integer mileage;
    @TableField("step_unit") private Integer stepUnit;
    @TableField("mileage_unit") private Integer mileageUnit;
    private Integer status;
    @TableField("created_at") private LocalDateTime createdAt;
    @TableField("updated_at") private LocalDateTime updatedAt;
}
