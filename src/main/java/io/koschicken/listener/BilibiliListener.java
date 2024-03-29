package io.koschicken.listener;

import catcode.CatCodeUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.koschicken.bean.bilibili.BiliUser;
import io.koschicken.bean.bilibili.Following;
import io.koschicken.bean.bilibili.Video;
import io.koschicken.bean.bilibili.space.Space;
import io.koschicken.bot.Bilibili;
import io.koschicken.bot.Groups;
import io.koschicken.config.GroupConfig;
import io.koschicken.utils.URLUtils;
import io.koschicken.utils.bilibili.BilibiliUtils;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilder;
import love.forte.simbot.filter.MatchType;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.koschicken.constants.Constants.CONFIG_DIR;

@Slf4j
@Service
public class BilibiliListener {

    private final MessageContentBuilderFactory factory;

    public BilibiliListener(MessageContentBuilderFactory factory) {
        this.factory = factory;
    }

    @OnGroup
    @Filter(value = "/fo", matchType = MatchType.STARTS_WITH)
    public void follow(GroupMsg groupMsg, Sender sender) throws IOException {
        BilibiliUtils.bilibiliJSON();
        String q = groupMsg.getMsg().substring(3).trim();
        BiliUser user = BilibiliUtils.searchByName(q);
        if (Objects.nonNull(user)) {
            String groupCode = groupMsg.getGroupInfo().getGroupCode();
            String uid = user.getMid();
            String name = user.getUname() + "(" + uid + ")";
            if (!isFollowed(groupCode, uid)) {
                follow(groupCode, uid, name);
                sender.sendGroupMsg(groupMsg, "已添加" + name + "的开播提示");
            } else {
                sender.sendGroupMsg(groupMsg, name + "已经添加过开播提示了");
            }
        } else {
            sender.sendGroupMsg(groupMsg, "未搜索到名为" + q + "的B站用户");
        }
    }

    @OnGroup
    @Filter(value = "/unfo", matchType = MatchType.STARTS_WITH)
    public void unfollow(GroupMsg groupMsg, Sender sender) throws IOException {
        BilibiliUtils.bilibiliJSON();
        String q = groupMsg.getMsg().substring(5).trim();
        BiliUser user = BilibiliUtils.searchByName(q);
        if (Objects.nonNull(user)) {
            String groupCode = groupMsg.getGroupInfo().getGroupCode();
            String uid = user.getMid();
            String name = user.getUname() + "(" + uid + ")";
            if (isFollowed(groupCode, uid)) {
                unfollow(groupCode, uid);
                sender.sendGroupMsg(groupMsg, "已删除" + name + "的开播提示");
            } else {
                sender.sendGroupMsg(groupMsg, name + "不存在");
            }
        } else {
            sender.sendGroupMsg(groupMsg, "未搜索到名为" + q + "的B站用户");
        }
    }

    @OnGroup
    @Filter(value = "/lsfo", matchType = MatchType.EQUALS)
    public void followingList(GroupMsg groupMsg, Sender sender) throws IOException, HttpException {
        BilibiliUtils.bilibiliJSON();
        String groupCode = groupMsg.getGroupInfo().getGroupCode();
        List<Following> followings = Bilibili.getInstance().get(groupCode);
        dealName(followings, groupCode);
        if (CollectionUtils.isEmpty(followings)) {
            sender.sendGroupMsg(groupMsg, "本群没有关注任何直播间");
        }
        MiraiMessageContentBuilder messageContentBuilder = getMiraiMessageContentBuilder(groupMsg, followings);
        MessageContent messageContent = messageContentBuilder.build();
        sender.sendGroupMsg(groupMsg, messageContent);
    }

    @NotNull
    private MiraiMessageContentBuilder getMiraiMessageContentBuilder(GroupMsg groupMsg, List<Following> followings) {
        List<List<Following>> partition = Lists.partition(followings, 10);
        MiraiMessageContentBuilder messageContentBuilder = (MiraiMessageContentBuilder) factory.getMessageContentBuilder();
        messageContentBuilder.forwardMessage(forwardBuilder -> {
            forwardBuilder.add(groupMsg.getAccountInfo(), "本群关注的直播间有：\n");
            partition.forEach(list -> {
                StringBuilder stringBuilder = new StringBuilder();
                list.forEach(f -> stringBuilder.append(f.getName()).append("\n"));
                forwardBuilder.add(groupMsg.getAccountInfo(), stringBuilder.toString());
            });
        });
        return messageContentBuilder;
    }

    @OnGroup
    @Filter(value = "bilibili.com/video/", matchType = MatchType.CONTAINS)
    public void videoInfo(GroupMsg groupMsg, Sender sender) {
        String baseUrl = "https://www.bilibili.com/video/";
        String msg = groupMsg.getMsg();
        Pattern pattern = Pattern.compile("https?://(|www.)bilibili.com/video/\\S+");
        Matcher m = pattern.matcher(msg);
        while (m.find()) {
            try {
                String group = m.group();
                URL url = new URL(group);
                String t = getTimestamp(url);
                String path = url.getPath().endsWith("/") ? url.getPath().substring(0, url.getPath().lastIndexOf("/")) : url.getPath();
                String bv = path.substring(path.lastIndexOf("/") + 1);
                Video video = bv.startsWith("BV") ? new Video(bv, true) : new Video(bv, false);
                CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(catCodeUtil.getStringTemplate().image(video.getPic().getAbsolutePath()))
                        .append("\nUP：").append(video.getOwner()).append("\n标题：").append(video.getTitle())
                        .append("\n链接：").append(baseUrl).append(video.getBv()).append(t);
                if (!stringBuilder.isEmpty()) {
                    String groupCode = groupMsg.getGroupInfo().getGroupCode();
                    GroupConfig groupConfig = Groups.getInstance().get(groupCode);
                    if (Objects.nonNull(groupConfig) && groupConfig.isGlobalSwitch()) {
                        sender.sendGroupMsg(groupCode, stringBuilder.toString());
                    }
                }
            } catch (MalformedURLException e) {
                log.error("解析url失败");
            }
        }
    }

    private String getTimestamp(URL url) {
        Map<String, String> queryMap = URLUtils.getQueryMap(url.getQuery());
        if (!CollectionUtils.isEmpty(queryMap)) {
            String t = queryMap.get("t");
            if (Objects.nonNull(t)) {
                return "?t=" + URLUtils.string2Float(t);
            }
        }
        return "";
    }

    private boolean isFollowed(String groupCode, String uid) {
        List<Following> followingList = Bilibili.getInstance().get(groupCode);
        if (CollectionUtils.isEmpty(followingList)) {
            return false;
        }
        return followingList.stream().anyMatch(following -> Objects.equals(uid, following.getUid()));
    }

    private void follow(String groupCode, String uid, String name) throws IOException {
        BilibiliUtils.bilibiliJSON();
        List<Following> followingList = Bilibili.getInstance().get(groupCode);
        if (CollectionUtils.isEmpty(followingList)) {
            followingList = new ArrayList<>();
        }
        followingList.add(new Following(uid, name, true, false, System.currentTimeMillis()));
        updateConfig(groupCode, followingList);
    }

    private void unfollow(String groupCode, String uid) throws IOException {
        BilibiliUtils.bilibiliJSON();
        List<Following> followingList = Bilibili.getInstance().get(groupCode);
        int index = indexOf(followingList, uid);
        if (index >= 0) {
            followingList.remove(index);
        }
        updateConfig(groupCode, followingList);
    }

    private int indexOf(List<Following> followingList, String uid) {
        for (int i = 0; i < followingList.size(); i++) {
            if (Objects.equals(followingList.get(i).getUid(), uid)) {
                return i;
            }
        }
        return -1;
    }

    private void dealName(List<Following> followings, String groupCode) throws IOException, HttpException {
        int callAPICount = 0;
        for (Following following : followings) {
            // 只有在没有存昵称或者上次昵称获取时间超过3天才会获取昵称
            Long lastModifiedTime = following.getLastModifiedTime();
            if (lastModifiedTime == null) {
                lastModifiedTime = 0L;
            }
            if (Objects.isNull(following.getName()) ||
                    lastModifiedTime + 1000 * 60 * 60 * 24 * 3 < System.currentTimeMillis()) {
                Space space = Space.getSpace(following.getUid());
                if (Objects.nonNull(space)) {
                    following.setName(space.getName() + "(" + space.getMid() + ")");
                    following.setLastModifiedTime(System.currentTimeMillis());
                }
                callAPICount++;
            }
        }
        if (callAPICount > 0) {
            updateConfig(groupCode, followings);
            log.info("使用API获取了{}个B站数据", callAPICount);
        }
    }

    private static void updateConfig(String groupCode, List<Following> followingList) throws IOException {
        Bilibili.getInstance().put(groupCode, followingList);
        String jsonString = JSON.toJSONString(Bilibili.getInstance());
        File jsonFile = new File(CONFIG_DIR + "/bilibili.json");
        FileUtils.deleteQuietly(jsonFile);
        FileUtils.writeStringToFile(jsonFile, jsonString, StandardCharsets.UTF_8);
    }
}
