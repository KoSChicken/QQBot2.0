package io.koschicken.bot;

import io.koschicken.config.GroupConfig;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Groups {

    private static Groups instance;

    private final Map<String, GroupConfig> groupConfigMap;

    private Groups() {
        groupConfigMap = new HashMap<>();
    }

    public static synchronized Groups getInstance() {
        if (instance == null) {
            instance = new Groups();
        }
        return instance;
    }

    public void put(String key, GroupConfig groupConfig) {
        groupConfigMap.put(key, groupConfig);
    }

    public GroupConfig get(String groupCode) {
        return groupConfigMap.get(groupCode);
    }
}
