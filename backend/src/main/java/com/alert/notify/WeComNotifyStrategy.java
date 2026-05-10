package com.alert.notify;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class WeComNotifyStrategy implements NotifyStrategy {
    @Override
    public String getType() {
        return "WECOM";
    }

    @Override
    public boolean send(Map<String, Object> config, String title, String content) {
        String webhook = (String) config.get("webhook");
        Map<String, Object> msg = new HashMap<>();
        msg.put("msgtype", "text");
        Map<String, String> text = new HashMap<>();
        text.put("content", "[" + title + "] " + content);
        msg.put("text", text);
        try {
            HttpUtil.post(webhook, JSON.toJSONString(msg));
            log.info("发送企业微信通知成功 -> webhook:{}", webhook);
            return true;
        } catch (Exception e) {
            log.error("发送企业微信通知失败", e);
            return false;
        }
    }
}
