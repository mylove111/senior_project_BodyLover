package com.bodylover.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("plans")
public class Plan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private BigDecimal durationHours;
    private Integer actualMinutes; // Actual duration in minutes
    private String planType; // EXERCISE, DIET
    private String status; // PENDING, COMPLETED
    private LocalDate scheduledDate;
    private LocalDateTime createdAt;
}
