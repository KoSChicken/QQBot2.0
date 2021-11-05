package io.koschicken.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import io.koschicken.bean.bilibili.BiliUser;
import io.koschicken.bean.bilibili.Following;
import io.koschicken.bean.bilibili.space.Space;
import io.koschicken.utils.HttpUtils;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.koschicken.constants.Constants.CONFIG_DIR;
import static io.koschicken.intercept.BotIntercept.GROUP_BILIBILI_MAP;

@Slf4j
@Service
public class BilibiliListener {

    @Autowired
    private MessageContentBuilderFactory factory;

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
    public void followingList(GroupMsg groupMsg, Sender sender) throws IOException {
        BilibiliUtils.bilibiliJSON();
        String groupCode = groupMsg.getGroupInfo().getGroupCode();
        List<Following> followings = GROUP_BILIBILI_MAP.get(groupCode);
        dealName(followings, groupCode);
        if (CollectionUtils.isEmpty(followings)) {
            sender.sendGroupMsg(groupMsg, "本群没有关注任何直播间");
        }
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
        MessageContent messageContent = messageContentBuilder.build();
        sender.sendGroupMsg(groupMsg, messageContent);
    }

    private void dealName(List<Following> followings, String groupCode) throws IOException {
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
            GROUP_BILIBILI_MAP.remove(groupCode);
            GROUP_BILIBILI_MAP.put(groupCode, followings);
            String jsonString = JSON.toJSONString(GROUP_BILIBILI_MAP);
            File jsonFile = new File(CONFIG_DIR + "/bilibili.json");
            FileUtils.deleteQuietly(jsonFile);
            FileUtils.writeStringToFile(jsonFile, jsonString, StandardCharsets.UTF_8);
            log.info("使用API获取了{}个B站数据", callAPICount);
        }
    }

    @OnGroup
    @Filter(value = "/cf", matchType = MatchType.STARTS_WITH)
    public void cdq(GroupMsg groupMsg, Sender sender) throws IOException {
        String url = "https://tools.asoulfan.com/api/cfj/?name=";
        String q = groupMsg.getMsg().substring(3).trim();
        String s = HttpUtils.get(url + q);
        log.info(s);
        JSONObject jsonObject = JSON.parseObject(s);
        Integer code = jsonObject.getInteger("code");
        if (code == 0) {
            StringBuilder stringBuilder = new StringBuilder();
            JSONArray data = jsonObject.getJSONObject("data").getJSONArray("list");
            if (data.size() > 0) {
                stringBuilder.append(q).append("关注的管人有：\n");
                for (int i = 0; i < data.size(); i++) {
                    JSONObject jo = data.getJSONObject(i);
                    stringBuilder.append(jo.getString("uname")).append("\n");
                }
                stringBuilder.append("共").append(jsonObject.getJSONObject("data").getInteger("total")).append("个");
                MiraiMessageContentBuilder messageContentBuilder = (MiraiMessageContentBuilder) factory.getMessageContentBuilder();
                messageContentBuilder.forwardMessage(forwardBuilder -> forwardBuilder.add(groupMsg.getAccountInfo(), stringBuilder.toString()));
                MessageContent messageContent = messageContentBuilder.build();
                sender.sendGroupMsg(groupMsg, messageContent);
//                sender.sendGroupMsg(groupMsg, stringBuilder.toString());
            } else {
                stringBuilder.append(q).append("没有关注管人");
                sender.sendGroupMsg(groupMsg, stringBuilder.toString());
            }
        } else {
            sender.sendGroupMsg(groupMsg, "查询失败");
        }
    }

    private boolean isFollowed(String groupCode, String uid) {
        List<Following> followingList = GROUP_BILIBILI_MAP.get(groupCode);
        if (CollectionUtils.isEmpty(followingList)) {
            return false;
        }
        return followingList.stream().anyMatch(following -> Objects.equals(uid, following.getUid()));
    }

    private void follow(String groupCode, String uid, String name) throws IOException {
        BilibiliUtils.bilibiliJSON();
        List<Following> followingList = GROUP_BILIBILI_MAP.get(groupCode);
        if (CollectionUtils.isEmpty(followingList)) {
            followingList = new ArrayList<>();
        }
        followingList.add(new Following(uid, name, true, false, System.currentTimeMillis()));
        GROUP_BILIBILI_MAP.remove(groupCode);
        GROUP_BILIBILI_MAP.put(groupCode, followingList);
        String jsonString = JSON.toJSONString(GROUP_BILIBILI_MAP);
        File jsonFile = new File(CONFIG_DIR + "/bilibili.json");
        FileUtils.deleteQuietly(jsonFile);
        FileUtils.writeStringToFile(jsonFile, jsonString, StandardCharsets.UTF_8);
    }

    private void unfollow(String groupCode, String uid) throws IOException {
        BilibiliUtils.bilibiliJSON();
        List<Following> followingList = GROUP_BILIBILI_MAP.get(groupCode);
        int index = indexOf(followingList, uid);
        if (index >= 0) {
            followingList.remove(index);
        }
        GROUP_BILIBILI_MAP.remove(groupCode);
        GROUP_BILIBILI_MAP.put(groupCode, followingList);
        String jsonString = JSON.toJSONString(GROUP_BILIBILI_MAP);
        File jsonFile = new File(CONFIG_DIR + "/bilibili.json");
        FileUtils.deleteQuietly(jsonFile);
        FileUtils.writeStringToFile(jsonFile, jsonString, StandardCharsets.UTF_8);
    }

    private int indexOf(List<Following> followingList, String uid) {
        for (int i = 0; i < followingList.size(); i++) {
            if (Objects.equals(followingList.get(i).getUid(), uid)) {
                return i;
            }
        }
        return -1;
    }
}
