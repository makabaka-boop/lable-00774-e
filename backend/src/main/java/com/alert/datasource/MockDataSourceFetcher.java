package com.alert.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@Component
public class MockDataSourceFetcher implements DataSourceFetcher {
    private final Random random = new Random();

    @Override
    public String getType() {
        return "MOCK";
    }

    @Override
    public Map<String, Object> fetch(Map<String, Object> config) {
        Map<String, Object> data = new HashMap<>();
        data.put("cpu", random.nextInt(100));
        data.put("memory", random.nextInt(100));
        data.put("disk", random.nextInt(100));
        data.put("responseTime", random.nextInt(5000));
        log.debug("Mock数据: {}", data);
        return data;
    }
}
