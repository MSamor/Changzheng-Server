package com.changzheng.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.*;

@Data
@TableName("daily_steps")
public class DailyStep {
    @TableId(type=IdType.AUTO) private Long id;
    @TableField("user_id") private Long userId;
    @TableField("step_date") private LocalDate stepDate;
    @TableField("step_count") private Integer stepCount;
    @TableField("raw_payload") private String rawPayload;
    @TableField("created_at") private LocalDateTime createdAt;
    @TableField("updated_at") private LocalDateTime updatedAt;
}
