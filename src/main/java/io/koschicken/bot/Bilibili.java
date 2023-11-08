package io.koschicken.bot;

import io.koschicken.bean.bilibili.Following;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Bilibili {

    private static Bilibili instance;

    private final Map<String, List<Following>> groupMap;

    private Bilibili() {
        groupMap = new HashMap<>();
    }

    public static synchronized Bilibili getInstance() {
        if (instance == null) {
            instance = new Bilibili();
        }
        return instance;
    }

    public void put(String key, List<Following> followingList) {
        groupMap.put(key, followingList);
    }

    public List<Following> get(String groupCode) {
        return groupMap.get(groupCode);
    }
}
