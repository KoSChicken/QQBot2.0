package io.koschicken.task;

import catcode.CatCodeUtil;
import io.koschicken.bean.bilibili.Following;
import io.koschicken.bean.bilibili.space.LiveRoom;
import io.koschicken.bean.bilibili.space.Space;
import io.koschicken.bot.Bilibili;
import io.koschicken.bot.Groups;
import io.koschicken.config.BotConfig;
import io.koschicken.config.GroupConfig;
import io.koschicken.utils.bilibili.BilibiliUtils;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.BotSender;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.bot.BotManager;
import org.apache.http.HttpException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@EnableScheduling
public class BilibiliTask {
    private static final HashMap<String, Space> LIVE_MAP = new HashMap<>();
    private static final HashMap<String, Space> NOTICED = new HashMap<>();

    private final BotManager botManager;

    public BilibiliTask(BotManager botManager) {
        this.botManager = botManager;
    }

    @Scheduled(cron = "0 * * ? * *")
    public void liveNotice() {
        BilibiliUtils.bilibiliJSON();
        Set<Following> allFollowing = fetchLive();
        if (!LIVE_MAP.isEmpty()) {
            log.info("当前监听的直播间：\n{}", printMap());
        }
        for (Following following : allFollowing) {
            if (following.isNotification()) {
                String uid = following.getUid();
                Space space = LIVE_MAP.get(uid);
                if (Objects.nonNull(space) && Objects.nonNull(space.getLiveRoom())) {
                    int liveStatus = space.getLiveRoom().getLiveStatus();
                    // 直播状态为直播中，且没有提醒过
                    if (liveStatus == 1 && !NOTICED.containsKey(uid)) {
                        NOTICED.putIfAbsent(uid, space); // 标记已提醒
                        notice(space);
                    } else if (liveStatus != 1) {
                        NOTICED.remove(uid); // 从已提醒中移除
                    }
                }
            }
        }
    }

    @OnGroup
    @Filter(".bilibiliLive")
    public void bilibiliLive(GroupMsg groupMsg, Sender sender) {
        if (Objects.equals(BotConfig.getInstance().getMasterQQ(), groupMsg.getAccountInfo().getAccountCode())) {
            liveNotice();
        }
    }

    private Set<Following> allFollowing() {
        Set<Following> followingSet = new HashSet<>();
        Set<String> groupList = Bilibili.getInstance().getGroupMap().keySet();
        for (String groupCode : groupList) {
            List<Following> followingList = Bilibili.getInstance().get(groupCode);
            followingSet.addAll(followingList);
        }
        return followingSet;
    }

    private Set<String> groupCodeByFollowing(String uid) {
        Set<String> group = new HashSet<>();
        Set<String> groupList = Bilibili.getInstance().getGroupMap().keySet();
        for (String groupCode : groupList) {
            List<Following> followingList = Bilibili.getInstance().get(groupCode);
            boolean match = followingList.stream().anyMatch(following -> Objects.equals(following.getUid(), uid));
            if (match) {
                group.add(groupCode);
            }
        }
        return group;
    }

    private Set<Following> fetchLive() {
        Set<Following> followingSet = allFollowing();
        LIVE_MAP.clear();
        followingSet.forEach(following -> {
            try {
                if (following.isNotification()) {
                    String uid = following.getUid();
                    Space space = Space.getSpace(uid);
                    if (Objects.nonNull(space)) {
                        LIVE_MAP.putIfAbsent(uid, space);
                    }
                }
            } catch (IOException | HttpException e) {
                log.error("获取B站用户信息失败", e);
            }
        });
        return followingSet;
    }

    private String printMap() {
        StringBuilder sb = new StringBuilder();
        LIVE_MAP.forEach((k, v) -> {
            boolean streaming = v.getLiveRoom().getLiveStatus() != 0;
            if (streaming) {
                sb.append("up：").append(v.getName()).append("\t")
                        .append("标题：").append(v.getLiveRoom().getTitle()).append("\t")
                        .append("状态：").append("直播中").append("\n");
            }
        });
        return sb.toString();
    }

    private void notice(Space space) {
        LiveRoom liveRoom = space.getLiveRoom();
        CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
        BotSender msgSender = botManager.getDefaultBot().getSender();
        StringBuilder stringBuilder = new StringBuilder();
        String up = "\nUP：";
        String title = "\n标题：";
        String url = "\n链接：";
        stringBuilder.append("开播啦！").append(up).append(space.getName())
                .append(title).append(liveRoom.getTitle()).append(url).append(liveRoom.getUrl()).append("\n")
                .append(catCodeUtil.getStringTemplate().image(liveRoom.getCoverFile().getAbsolutePath()));
        if (!stringBuilder.isEmpty()) {
            Set<String> groups = groupCodeByFollowing(space.getMid());
            for (String groupCode : groups) {
                GroupConfig groupConfig = Groups.getInstance().get(groupCode);
                if (Objects.nonNull(groupConfig) && groupConfig.isGlobalSwitch()) {
                    msgSender.SENDER.sendGroupMsg(groupCode, stringBuilder.toString());
                }
            }
        }
    }
}
