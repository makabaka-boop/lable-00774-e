package com.alert.datasource;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ApiDataSourceFetcher implements DataSourceFetcher {
    @Override
    public String getType() {
        return "API";
    }

    @Override
    public Map<String, Object> fetch(Map<String, Object> config) {
        String url = (String) config.get("url");
        String method = (String) config.getOrDefault("method", "GET");
        try {
            String response = "GET".equals(method) ? HttpUtil.get(url) : HttpUtil.post(url, "");
            return JSON.parseObject(response);
        } catch (Exception e) {
            log.error("API数据获取失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
