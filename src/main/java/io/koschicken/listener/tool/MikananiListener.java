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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MikananiListener {

    private final MiraiMessageContentBuilderFactory factory;

    public MikananiListener(MiraiMessageContentBuilderFactory factory) {
        this.factory = factory;
    }

    @OnGroup
    @Filter(value = "看番", matchType = MatchType.STARTS_WITH)
    public void mikanani(GroupMsg groupMsg, MsgSender sender) throws IOException {
        String keyword = groupMsg.getMsgContent().getMsg().replace("看番", "").trim();
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        List<Mikanani> mikananiList = Mikanani.search(keyword);
        if (!CollectionUtils.isEmpty(mikananiList)) {
            MiraiMessageContentBuilder messageContentBuilder = factory.getMessageContentBuilder();
            List<MessageContent> parentList = new ArrayList<>();
            for (Mikanani mikanani : mikananiList) {
                List<MessageContent> msgList = new ArrayList<>();
                List<Magnet> mags = mikanani.getMags();
                mags.forEach(mag -> msgList.add(buildMessage(mag)));
                messageContentBuilder.forwardMessage(forwardBuilder -> {
                    GroupMemberInfo groupMemberInfo = randomGroupMember(groupMsg, sender);
                    for (MessageContent messageContent : msgList) {
                        forwardBuilder.add(groupMemberInfo, messageContent);
                    }
                });
                final MiraiMessageContent messageContent = messageContentBuilder.build();
                parentList.add(messageContent);
            }
            messageContentBuilder.forwardMessage(forwardBuilder -> {
                GroupMemberInfo groupMemberInfo = randomGroupMember(groupMsg, sender);
                for (MessageContent messageContent : parentList) {
                    forwardBuilder.add(groupMemberInfo, messageContent);
                }
            });
            sender.SENDER.sendGroupMsg(groupMsg, messageContentBuilder.build());
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
        message.append("番组：").append(magnet.getName()).append("\n")
                .append("大小：").append(magnet.getSize()).append("\n")
                .append("时间：").append(magnet.getReleaseTime()).append("\n")
                .append("磁链：").append(magnet.getShortMag()).append("\n");
        return messageContentBuilder.text(message).build();
    }
}
