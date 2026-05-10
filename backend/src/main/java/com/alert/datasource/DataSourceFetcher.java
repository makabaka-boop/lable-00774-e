package com.alert.datasource;

import java.util.Map;

public interface DataSourceFetcher {
    String getType();
    Map<String, Object> fetch(Map<String, Object> config);
}
