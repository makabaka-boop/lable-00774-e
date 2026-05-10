package com.alert.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
public class EmailNotifyStrategy implements NotifyStrategy {
    @Override
    public String getType() {
        return "EMAIL";
    }

    @Override
    public boolean send(Map<String, Object> config, String title, String content) {
        // 实际项目中使用JavaMailSender发送邮件
        String host = (String) config.get("host");
        String to = (String) config.get("to");
        log.info("发送邮件通知 -> host:{}, to:{}, title:{}, content:{}", host, to, title, content);
        return true;
    }
}
