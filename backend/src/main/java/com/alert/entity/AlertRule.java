package com.alert.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("alert_rule")
public class AlertRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String dataSourceType; // API/DATABASE/PROMETHEUS
    private String dataSourceConfig;
    private String ruleExpression; // SpEL表达式
    private String severity; // INFO/WARNING/CRITICAL
    private String channelIds;
    private String notifyTemplate;
    private Integer checkInterval;
    private Integer status;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
