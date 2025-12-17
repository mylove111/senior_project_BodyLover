package com.bodylover.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String accountId; // Unique Login ID
    private String username;  // Display Name (Nickname)
    private String password;
    private String mode; // TEENAGER, ADULT, SENIOR
    private Integer age;
    private Integer points; // Reward Points for Game
    private LocalDateTime createdAt;
}
