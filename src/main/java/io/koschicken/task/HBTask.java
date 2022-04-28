package io.koschicken.task;

import io.koschicken.bean.GroupPower;
import io.koschicken.bean.setu.LoliconResponse;
import io.koschicken.bean.setu.Pixiv;
import io.koschicken.listener.setu.SetuRunner;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.results.GroupMemberInfo;
import love.forte.simbot.api.sender.BotSender;
import love.forte.simbot.bot.BotManager;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilder;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.koschicken.intercept.BotIntercept.GROUP_CONFIG_MAP;

@Slf4j
@Component
public class HBTask {

    @Autowired
    private BotManager botManager;
    @Autowired
    private MiraiMessageContentBuilderFactory factory;

    @Scheduled(fixedDelayString = "#{ T(java.util.concurrent.ThreadLocalRandom).current().nextInt(30, 91) * 1000 * 60 }")
    private void heartBeat() throws IOException {
        Set<String> groups = GROUP_CONFIG_MAP.keySet();
        for (String groupCode : groups) {
            GroupPower groupPower = GROUP_CONFIG_MAP.get(groupCode);
            if (groupPower.isGlobalSwitch() && groupPower.isMaiyaoSwitch()) {
                LoliconResponse loliconResponse = Pixiv.get(null, 1, false);
                Pixiv pixiv = loliconResponse.getData().get(0);
                SetuRunner setuRunner = new SetuRunner(factory);
                MessageContent messageContent = setuRunner.buildMessage(pixiv);
                send(messageContent, groupCode);
            }
        }
    }

    private void send(MessageContent messageContent, String groupCode) {
        BotSender sender = botManager.getDefaultBot().getSender();
        MiraiMessageContentBuilder messageContentBuilder = factory.getMessageContentBuilder();
        List<GroupMemberInfo> groupMemberList = sender.GETTER.getGroupMemberList(groupCode).stream().collect(Collectors.toList());
        Collections.shuffle(groupMemberList);
        GroupMemberInfo groupMemberInfo = groupMemberList.get(0);
        messageContentBuilder.forwardMessage(forwardBuilder -> forwardBuilder.add(groupMemberInfo, messageContent));
        sender.SENDER.sendGroupMsg(groupCode, messageContentBuilder.build());
    }
}
