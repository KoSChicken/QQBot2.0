package io.koschicken.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Objects;
import io.koschicken.bean.bilibili.Following;
import io.koschicken.utils.HttpUtils;
import io.koschicken.utils.bilibili.BilibiliUtils;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.filter.MatchType;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.koschicken.constants.Constants.CONFIG_DIR;
import static io.koschicken.intercept.BotIntercept.GROUP_BILIBILI_MAP;

@Slf4j
@Service
public class BilibiliListener {

//    @Autowired(required = false)
//    private MiraiMessageContentBuilderFactory factory;

    @OnGroup
    @Filter(value = "/fo", matchType = MatchType.STARTS_WITH)
    public void follow(GroupMsg groupMsg, Sender sender) throws IOException {
        BilibiliUtils.bilibiliJSON();
        Pattern pattern = Pattern.compile("[0-9.]");
        Matcher matcher = pattern.matcher(groupMsg.getMsg());
        StringBuilder stringBuilder = new StringBuilder();
        while (matcher.find()) {
            stringBuilder.append(matcher.group(0));
        }
        String uid = stringBuilder.toString();
        String groupCode = groupMsg.getGroupInfo().getGroupCode();
        if (!isFollowed(groupCode, uid)) {
            follow(groupCode, uid);
            sender.sendGroupMsg(groupMsg, "已添加" + uid + "的开播提示");
        } else {
            sender.sendGroupMsg(groupMsg, uid + "已经添加过开播提示了");
        }
    }

    @OnGroup
    @Filter(value = "/unfo", matchType = MatchType.STARTS_WITH)
    public void unfollow(GroupMsg groupMsg, Sender sender) throws IOException {
        BilibiliUtils.bilibiliJSON();
        Pattern pattern = Pattern.compile("[0-9.]");
        Matcher matcher = pattern.matcher(groupMsg.getMsg());
        StringBuilder stringBuilder = new StringBuilder();
        while (matcher.find()) {
            stringBuilder.append(matcher.group(0));
        }
        String uid = stringBuilder.toString();
        String groupCode = groupMsg.getGroupInfo().getGroupCode();
        if (isFollowed(groupCode, uid)) {
            unfollow(groupCode, uid);
            sender.sendGroupMsg(groupMsg, "已删除" + uid + "的开播提示");
        } else {
            sender.sendGroupMsg(groupMsg, uid + "不存在");
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
//                MiraiMessageContentBuilder messageContentBuilder = factory.getMessageContentBuilder();
//                messageContentBuilder.forwardMessage(forwardBuilder -> forwardBuilder.add(groupMsg.getAccountInfo(), buildMessageContent(stringBuilder.toString())));
//                MessageContent messageContent = messageContentBuilder.build();
//                sender.sendGroupMsg(groupMsg, messageContent);
                sender.sendGroupMsg(groupMsg, stringBuilder.toString());
            } else {
                stringBuilder.append(q).append("没有关注管人");
                sender.sendGroupMsg(groupMsg, stringBuilder.toString());
            }
        } else {
            sender.sendGroupMsg(groupMsg, "查询失败");
        }
    }

//    private MessageContent buildMessageContent(String message) {
//        MiraiMessageContentBuilder messageContentBuilder = factory.getMessageContentBuilder();
//        return messageContentBuilder.text(message).build();
//    }

    private boolean isFollowed(String groupCode, String uid) {
        List<Following> followingList = GROUP_BILIBILI_MAP.get(groupCode);
        return followingList.stream().anyMatch(following -> Objects.equal(uid, following.getUid()));
    }

    private void follow(String groupCode, String uid) throws IOException {
        BilibiliUtils.bilibiliJSON();
        List<Following> followingList = GROUP_BILIBILI_MAP.get(groupCode);
        followingList.add(new Following(uid, true, false));
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
            if (Objects.equal(followingList.get(i).getUid(), uid)) {
                return i;
            }
        }
        return -1;
    }
}
