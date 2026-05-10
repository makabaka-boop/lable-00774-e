package com.alert.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DataSourceService {
    private final List<DataSourceFetcher> fetchers;
    private final Map<String, DataSourceFetcher> fetcherMap = new HashMap<>();

    public DataSourceService(List<DataSourceFetcher> fetchers) {
        this.fetchers = fetchers;
    }

    @PostConstruct
    public void init() {
        fetchers.forEach(f -> fetcherMap.put(f.getType(), f));
        log.info("已注册数据源: {}", fetcherMap.keySet());
    }

    public Map<String, Object> fetch(String type, Map<String, Object> config) {
        DataSourceFetcher fetcher = fetcherMap.get(type);
        if (fetcher == null) {
            log.error("未找到数据源类型: {}", type);
            return new HashMap<>();
        }
        return fetcher.fetch(config);
    }
}
