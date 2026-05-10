package com.alert.notify;

import java.util.Map;

public interface NotifyStrategy {
    String getType();
    boolean send(Map<String, Object> config, String title, String content);
}
