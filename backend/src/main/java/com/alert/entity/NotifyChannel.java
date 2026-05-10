package com.alert.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notify_channel")
public class NotifyChannel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type; // EMAIL/SMS/DINGTALK/WECOM/FEISHU
    private String config; // 加密存储的配置JSON
    private Integer status;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
