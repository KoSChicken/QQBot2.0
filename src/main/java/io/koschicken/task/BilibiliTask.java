package io.koschicken.task;

import catcode.CatCodeUtil;
import io.koschicken.bean.bilibili.Following;
import io.koschicken.bean.bilibili.Live;
import io.koschicken.utils.bilibili.BilibiliUtils;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.BotSender;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.bot.BotManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

import static io.koschicken.constants.Constants.COMMON_CONFIG;
import static io.koschicken.intercept.BotIntercept.GROUP_BILIBILI_MAP;
import static io.koschicken.intercept.BotIntercept.GROUP_CONFIG_MAP;

@Component
@EnableScheduling
public class BilibiliTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(BilibiliTask.class);
    private static final HashMap<String, Live> LIVE_MAP = new HashMap<>();
    private static final HashMap<String, Live> NOTICED = new HashMap<>();

    @Autowired
    BotManager botManager;

    @Scheduled(cron = "0 */1 * * * ?")
    public void liveNotice() {
        BilibiliUtils.bilibiliJSON();
        Set<Following> allFollowing = fetchLive();
        LOGGER.info("当前监听的直播间：\n{}", GROUP_BILIBILI_MAP.isEmpty() ? "无" : printMap());
        for (Following following : allFollowing) {
            if (following.isNotification()) {
                String uid = following.getUid();
                Live live = LIVE_MAP.get(uid);
                int liveStatus = live.getLiveStatus();
                // 直播状态为直播中，且没有提醒过
                if (liveStatus == 1 && !NOTICED.containsKey(uid)) {
                    NOTICED.putIfAbsent(uid, live); // 标记已提醒
                    notice(live);
                } else if (liveStatus != 1){
                    NOTICED.remove(uid); // 从已提醒中移除
                }
            }
        }
    }

    @OnGroup
    @Filter(".bilibiliLive")
    public void bilibiliLive(GroupMsg groupMsg, Sender sender) {
        if (Objects.equals(COMMON_CONFIG.getMasterQQ(), groupMsg.getAccountInfo().getAccountCode())) {
            liveNotice();
        }
    }

    private Set<Following> allFollowing() {
        Set<Following> followingSet = new HashSet<>();
        Set<String> groupList = GROUP_BILIBILI_MAP.keySet();
        for (String groupCode : groupList) {
            List<Following> followingList = GROUP_BILIBILI_MAP.get(groupCode);
            followingSet.addAll(followingList);
        }
        return followingSet;
    }

    private Set<String> groupCodeByFollowing(String uid) {
        Set<String> group = new HashSet<>();
        Set<String> groupList = GROUP_BILIBILI_MAP.keySet();
        for (String groupCode : groupList) {
            List<Following> followingList = GROUP_BILIBILI_MAP.get(groupCode);
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
                    Live biliLive = new Live(uid);
                    LIVE_MAP.putIfAbsent(uid, biliLive);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return followingSet;
    }

    private String printMap() {
        StringBuilder sb = new StringBuilder();
        LIVE_MAP.forEach((k, v) -> sb.append("up：").append(v.getUser().getUname()).append("\t")
                .append("标题：").append(v.getTitle()).append("\t")
                .append("状态：").append(v.getLiveStatus() == 0 ? "未直播" : "直播中").append("\n"));
        return sb.toString();
    }

    private void notice(Live live) {
        CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
        BotSender msgSender = botManager.getDefaultBot().getSender();
        StringBuilder stringBuilder = new StringBuilder();
        String up = "\nUP：";
        String title = "\n标题：";
        String url = "\n链接：";
        stringBuilder.append("开播啦！").append(up).append(live.getUser().getUname())
                .append(title).append(live.getTitle()).append(url).append(live.getUrl()).append("\n")
                .append(catCodeUtil.getStringTemplate().image(live.getCover().getAbsolutePath()));
        if (stringBuilder.length() > 0) {
            Set<String> groups = groupCodeByFollowing(live.getMid());
            for (String groupCode : groups) {
                if (GROUP_CONFIG_MAP.get(groupCode).isGlobalSwitch()) {
                    msgSender.SENDER.sendGroupMsg(groupCode, stringBuilder.toString());
                }
            }
        }
    }
}
