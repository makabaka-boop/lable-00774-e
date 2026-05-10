package com.alert.notify;

import com.alibaba.fastjson.JSON;
import com.alert.entity.NotifyChannel;
import com.alert.util.EncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NotifyService {
    private final List<NotifyStrategy> strategies;
    private final EncryptUtil encryptUtil;
    private final Map<String, NotifyStrategy> strategyMap = new HashMap<>();

    public NotifyService(List<NotifyStrategy> strategies, EncryptUtil encryptUtil) {
        this.strategies = strategies;
        this.encryptUtil = encryptUtil;
    }

    @PostConstruct
    public void init() {
        strategies.forEach(s -> strategyMap.put(s.getType(), s));
        log.info("已注册通知策略: {}", strategyMap.keySet());
    }

    public boolean send(NotifyChannel channel, String title, String content) {
        NotifyStrategy strategy = strategyMap.get(channel.getType());
        if (strategy == null) {
            log.error("未找到通知策略: {}", channel.getType());
            return false;
        }
        String decryptedConfig = encryptUtil.decrypt(channel.getConfig());
        Map<String, Object> config = JSON.parseObject(decryptedConfig);
        return strategy.send(config, title, content);
    }
}
