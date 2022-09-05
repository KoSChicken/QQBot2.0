package io.koschicken.listener.tool;

import io.koschicken.bean.Magnet;
import io.koschicken.bean.mikanani.Mikanani;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.results.GroupMemberInfo;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.component.mirai.message.MiraiMessageContent;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilder;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilderFactory;
import love.forte.simbot.filter.MatchType;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MikananiListener {

    @Autowired
    private MiraiMessageContentBuilderFactory factory;

    @OnGroup
    @Filter(value = "看番", matchType = MatchType.STARTS_WITH)
    public void netEaseMusic(GroupMsg groupMsg, MsgSender sender) throws Exception {
        String keyword = groupMsg.getMsgContent().getMsg().replace("看番", "").trim();
        List<Mikanani> mikananiList = Mikanani.search(keyword);
        if (!CollectionUtils.isEmpty(mikananiList)) {
            MiraiMessageContentBuilder messageContentBuilder = factory.getMessageContentBuilder();
            for (Mikanani mikanani : mikananiList) {
                ArrayList<MessageContent> msgList = new ArrayList<>();
                List<Magnet> mags = mikanani.getMags();
                mags.subList(0, Math.min(3, mags.size() - 1)).forEach(mag -> msgList.add(buildMessage(mag)));
                messageContentBuilder.forwardMessage(forwardBuilder -> {
                    for (MessageContent messageContent : msgList) {
                        forwardBuilder.add(RandomUtils.nextBoolean() ? groupMsg.getAccountInfo() : randomGroupMember(groupMsg, sender), messageContent);
                    }
                });
                final MiraiMessageContent messageContent = messageContentBuilder.build();
                sender.SENDER.sendGroupMsg(groupMsg, messageContent);
            }
        } else {
            sender.SENDER.sendGroupMsg(groupMsg, "冇");
        }
    }

    private GroupMemberInfo randomGroupMember(GroupMsg groupMsg, MsgSender sender) {
        List<GroupMemberInfo> groupMemberList = sender.GETTER.getGroupMemberList(groupMsg.getGroupInfo().getGroupCode())
                .stream().collect(Collectors.toList());
        Collections.shuffle(groupMemberList);
        return groupMemberList.get(0);
    }

    private MessageContent buildMessage(Magnet magnet) {
        MiraiMessageContentBuilder messageContentBuilder = factory.getMessageContentBuilder();
        StringBuilder message = new StringBuilder();
        message.append("番组名：").append(magnet.getName()).append("\n")
                .append("大小：").append(magnet.getSize()).append("\n")
                .append("更新时间：").append(magnet.getReleaseTime()).append("\n")
                .append("磁链：").append(magnet.getShortMag()).append("\n");
        return messageContentBuilder.text(message).build();
    }
}
