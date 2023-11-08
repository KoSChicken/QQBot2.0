package io.koschicken.listener;

import io.koschicken.bot.Groups;
import io.koschicken.config.BotConfig;
import io.koschicken.config.ExternalProperties;
import io.koschicken.config.GroupConfig;
import io.koschicken.utils.ConfigFileUtils;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.annotation.OnPrivate;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.sender.Sender;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Slf4j
@Service
public class BotConfigListener {

    private static final String CACHE_DIR = "./cache/";

    @OnGroup
    @Filter("/on")
    public void enableBot(GroupMsg groupMsg, Sender sender) {
        String qq = groupMsg.getAccountInfo().getAccountCode();
        if (!Objects.equals(qq, BotConfig.getInstance().getMasterQQ())) {
            sender.sendGroupMsg(groupMsg, "你没有权限哦");
        } else {
            String groupCode = groupMsg.getGroupInfo().getGroupCode();
            GroupConfig groupConfig = Groups.getInstance().get(groupCode);
            boolean globalSwitch = groupConfig.isGlobalSwitch();
            groupConfig.allSwitch(true);
            ConfigFileUtils.updateGroupConfig(Groups.getInstance());
            if (!globalSwitch) {
                sender.sendGroupMsg(groupMsg, "活了");
            }
        }
    }

    @OnGroup
    @Filter("/off")
    public void disableBot(GroupMsg groupMsg, Sender sender) {
        String qq = groupMsg.getAccountInfo().getAccountCode();
        if (!Objects.equals(qq, BotConfig.getInstance().getMasterQQ())) {
            sender.sendGroupMsg(groupMsg, "你没有权限哦");
        } else {
            String groupCode = groupMsg.getGroupInfo().getGroupCode();
            GroupConfig groupConfig = Groups.getInstance().get(groupCode);
            boolean globalSwitch = groupConfig.isGlobalSwitch();
            groupConfig.allSwitch(false);
            ConfigFileUtils.updateGroupConfig(Groups.getInstance());
            if (globalSwitch) {
                sender.sendGroupMsg(groupMsg, "死了");
            }
        }
    }

    @OnGroup
    @Filter("/st")
    public void setuSwitch(GroupMsg groupMsg, Sender sender) {
        String qq = groupMsg.getAccountInfo().getAccountCode();
        if (!Objects.equals(qq, BotConfig.getInstance().getMasterQQ())) {
            sender.sendGroupMsg(groupMsg, "你没有权限哦");
        } else {
            String groupCode = groupMsg.getGroupInfo().getGroupCode();
            GroupConfig groupConfig = Groups.getInstance().get(groupCode);
            groupConfig.setSetuSwitch(!groupConfig.isSetuSwitch());
            ConfigFileUtils.updateGroupConfig(Groups.getInstance());
            if (groupConfig.isSetuSwitch()) {
                sender.sendGroupMsg(groupMsg, "开车！");
            } else {
                sender.sendGroupMsg(groupMsg, "停车！");
            }
        }
    }

    @OnPrivate
    @Filter("/fresh")
    public void fresh(PrivateMsg privateMsg, Sender sender) throws IOException {
        String qq = privateMsg.getAccountInfo().getAccountCode();
        if (!Objects.equals(qq, BotConfig.getInstance().getMasterQQ())) {
            sender.sendPrivateMsg(privateMsg, "你没有权限哦");
        } else {
            FileUtils.deleteDirectory(new File(CACHE_DIR));
            ExternalProperties externalProperties = new ExternalProperties();
            externalProperties.refresh();
            sender.sendPrivateMsg(privateMsg, "已删除cache文件夹并重新加载配置");
        }
    }
}
