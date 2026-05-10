package com.alert.notify;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class FeishuNotifyStrategy implements NotifyStrategy {
    @Override
    public String getType() {
        return "FEISHU";
    }

    @Override
    public boolean send(Map<String, Object> config, String title, String content) {
        String webhook = (String) config.get("webhook");
        Map<String, Object> msg = new HashMap<>();
        msg.put("msg_type", "text");
        Map<String, String> msgContent = new HashMap<>();
        msgContent.put("text", "[" + title + "] " + content);
        msg.put("content", msgContent);
        try {
            HttpUtil.post(webhook, JSON.toJSONString(msg));
            log.info("发送飞书通知成功 -> webhook:{}", webhook);
            return true;
        } catch (Exception e) {
            log.error("发送飞书通知失败", e);
            return false;
        }
    }
}
