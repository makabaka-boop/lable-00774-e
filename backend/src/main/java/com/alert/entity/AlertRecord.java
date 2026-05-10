package com.alert.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("alert_record")
public class AlertRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ruleId;
    private String ruleName;
    private String severity;
    private String content;
    private Integer notifyStatus;
    private String notifyResult;
    private LocalDateTime triggeredAt;
}
