package com.bodylover.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("health_records")
public class HealthRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal height; // cm
    private BigDecimal weight; // kg
    private BigDecimal bmi;
    private LocalDate recordDate;
    private LocalDateTime createdAt;
}
