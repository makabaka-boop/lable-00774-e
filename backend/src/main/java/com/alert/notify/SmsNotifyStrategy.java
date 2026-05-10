package com.alert.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
public class SmsNotifyStrategy implements NotifyStrategy {
    @Override
    public String getType() {
        return "SMS";
    }

    @Override
    public boolean send(Map<String, Object> config, String title, String content) {
        String phone = (String) config.get("phone");
        log.info("发送短信通知 -> phone:{}, content:{}", phone, content);
        return true;
    }
}
