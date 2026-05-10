package com.alert.service;

import com.alibaba.fastjson.JSON;
import com.alert.datasource.DataSourceService;
import com.alert.entity.AlertRecord;
import com.alert.entity.AlertRule;
import com.alert.entity.NotifyChannel;
import com.alert.notify.NotifyService;
import com.alert.rule.RuleEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AlertCheckService {
    private final AlertRuleService alertRuleService;
    private final AlertRecordService alertRecordService;
    private final NotifyChannelService notifyChannelService;
    private final DataSourceService dataSourceService;
    private final NotifyService notifyService;
    private final RuleEngine ruleEngine;
    private final SysConfigService sysConfigService;

    public AlertCheckService(AlertRuleService alertRuleService, AlertRecordService alertRecordService,
                            NotifyChannelService notifyChannelService, DataSourceService dataSourceService,
                            NotifyService notifyService, RuleEngine ruleEngine, SysConfigService sysConfigService) {
        this.alertRuleService = alertRuleService;
        this.alertRecordService = alertRecordService;
        this.notifyChannelService = notifyChannelService;
        this.dataSourceService = dataSourceService;
        this.notifyService = notifyService;
        this.ruleEngine = ruleEngine;
        this.sysConfigService = sysConfigService;
    }

    @Scheduled(fixedDelay = 30000)
    public void check() {
        if (!"true".equals(sysConfigService.getConfig("alert.check.enabled"))) {
            return;
        }
        List<AlertRule> rules = alertRuleService.getActiveRules();
        for (AlertRule rule : rules) {
            try {
                checkRule(rule);
            } catch (Exception e) {
                log.error("规则检查失败: {}", rule.getName(), e);
            }
        }
    }

    public void checkRule(AlertRule rule) {
        Map<String, Object> config = JSON.parseObject(rule.getDataSourceConfig());
        Map<String, Object> data = dataSourceService.fetch(rule.getDataSourceType(), config);
        
        if (ruleEngine.evaluate(rule, data)) {
            String content = ruleEngine.formatMessage(rule.getNotifyTemplate(), data);
            AlertRecord record = new AlertRecord();
            record.setRuleId(rule.getId());
            record.setRuleName(rule.getName());
            record.setSeverity(rule.getSeverity());
            record.setContent(content);
            record.setTriggeredAt(LocalDateTime.now());

            StringBuilder notifyResult = new StringBuilder();
            boolean notifySuccess = false;
            
            if (rule.getChannelIds() != null && !rule.getChannelIds().isEmpty()) {
                String[] channelIds = rule.getChannelIds().split(",");
                for (String channelId : channelIds) {
                    try {
                        NotifyChannel channel = notifyChannelService.getDecrypted(Long.parseLong(channelId.trim()));
                        if (channel != null && channel.getStatus() == 1) {
                            boolean sent = notifyService.send(channel, "[" + rule.getSeverity() + "] " + rule.getName(), content);
                            notifyResult.append(channel.getName()).append(":").append(sent ? "成功" : "失败").append(";");
                            if (sent) notifySuccess = true;
                        }
                    } catch (Exception e) {
                        notifyResult.append(channelId).append(":异常;");
                    }
                }
            }
            
            record.setNotifyStatus(notifySuccess ? 1 : 2);
            record.setNotifyResult(notifyResult.toString());
            alertRecordService.save(record);
            log.info("告警触发: {} - {}", rule.getName(), content);
        }
    }

    public void manualCheck(Long ruleId) {
        AlertRule rule = alertRuleService.getById(ruleId);
        if (rule != null) {
            checkRule(rule);
        }
    }
}
