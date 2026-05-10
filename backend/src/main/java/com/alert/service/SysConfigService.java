package com.alert.service;

import com.alert.entity.SysConfig;
import com.alert.mapper.SysConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SysConfigService extends ServiceImpl<SysConfigMapper, SysConfig> {
    private final StringRedisTemplate redisTemplate;
    private final ConcurrentHashMap<String, String> localCache = new ConcurrentHashMap<>();
    private static final String CONFIG_PREFIX = "sys:config:";

    public SysConfigService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void loadConfig() {
        list().forEach(c -> {
            localCache.put(c.getConfigKey(), c.getConfigValue());
            try {
                redisTemplate.opsForValue().set(CONFIG_PREFIX + c.getConfigKey(), c.getConfigValue());
            } catch (Exception ignored) {}
        });
    }

    public String getConfig(String key) {
        String value = localCache.get(key);
        if (value == null) {
            try {
                value = redisTemplate.opsForValue().get(CONFIG_PREFIX + key);
            } catch (Exception ignored) {}
        }
        if (value == null) {
            SysConfig config = getOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
            if (config != null) {
                value = config.getConfigValue();
                localCache.put(key, value);
            }
        }
        return value;
    }

    public void updateConfig(String key, String value) {
        SysConfig config = getOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        if (config != null) {
            config.setConfigValue(value);
            updateById(config);
        } else {
            config = new SysConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            save(config);
        }
        localCache.put(key, value);
        try {
            redisTemplate.opsForValue().set(CONFIG_PREFIX + key, value);
        } catch (Exception ignored) {}
    }

    public List<SysConfig> getAllConfigs() {
        return list();
    }
}
