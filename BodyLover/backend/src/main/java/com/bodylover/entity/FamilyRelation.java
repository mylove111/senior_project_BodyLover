package com.bodylover.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("family_relations")
public class FamilyRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long requesterId;
    private Long receiverId;
    private String status; // PENDING, ACCEPTED, REJECTED
    private LocalDateTime createdAt;
}
