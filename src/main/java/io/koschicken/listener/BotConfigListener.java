package io.koschicken.listener;

import com.alibaba.fastjson.JSON;
import io.koschicken.InitConfig;
import io.koschicken.bean.GroupPower;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.annotation.OnPrivate;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.bot.BotManager;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static io.koschicken.constants.Constants.COMMON_CONFIG;
import static io.koschicken.constants.Constants.CONFIG_DIR;
import static io.koschicken.intercept.BotIntercept.GROUP_CONFIG_MAP;

@Slf4j
@Service
public class BotConfigListener {

    private static final String CACHE_DIR = "./cache/";

    @Autowired
    private BotManager botManager;

    @OnGroup
    @Filter("/on")
    public void enableBot(GroupMsg groupMsg, Sender sender) {
        String qq = groupMsg.getAccountInfo().getAccountCode();
        if (!Objects.equals(qq, COMMON_CONFIG.getMasterQQ())) {
            sender.sendGroupMsg(groupMsg, "你没有权限哦");
        } else {
            String groupCode = groupMsg.getGroupInfo().getGroupCode();
            GroupPower groupPower = GROUP_CONFIG_MAP.get(groupCode);
            boolean globalSwitch = groupPower.isGlobalSwitch();
            groupPower.allSwitch(true);
            setJson(GROUP_CONFIG_MAP);
            if (!globalSwitch) {
                sender.sendGroupMsg(groupMsg, "活了");
            }
        }
    }

    @OnGroup
    @Filter("/off")
    public void disableBot(GroupMsg groupMsg, Sender sender) {
        String qq = groupMsg.getAccountInfo().getAccountCode();
        if (!Objects.equals(qq, COMMON_CONFIG.getMasterQQ())) {
            sender.sendGroupMsg(groupMsg, "你没有权限哦");
        } else {
            String groupCode = groupMsg.getGroupInfo().getGroupCode();
            GroupPower groupPower = GROUP_CONFIG_MAP.get(groupCode);
            boolean globalSwitch = groupPower.isGlobalSwitch();
            groupPower.allSwitch(false);
            setJson(GROUP_CONFIG_MAP);
            if (globalSwitch) {
                sender.sendGroupMsg(groupMsg, "死了");
            }
        }
    }

    @OnGroup
    @Filter("/st")
    public void setuSwitch(GroupMsg groupMsg, Sender sender) {
        String qq = groupMsg.getAccountInfo().getAccountCode();
        if (!Objects.equals(qq, COMMON_CONFIG.getMasterQQ())) {
            sender.sendGroupMsg(groupMsg, "你没有权限哦");
        } else {
            String groupCode = groupMsg.getGroupInfo().getGroupCode();
            GroupPower groupPower = GROUP_CONFIG_MAP.get(groupCode);
            groupPower.setSetuSwitch(!groupPower.isSetuSwitch());
            setJson(GROUP_CONFIG_MAP);
            if (groupPower.isSetuSwitch()) {
                sender.sendGroupMsg(groupMsg, "开车！");
            } else {
                sender.sendGroupMsg(groupMsg, "停车！");
            }
        }
    }

    @OnGroup
    @Filter("/hbswitch")
    public void hbswitch(GroupMsg groupMsg, Sender sender) {
        String qq = groupMsg.getAccountInfo().getAccountCode();
        if (!Objects.equals(qq, COMMON_CONFIG.getMasterQQ())) {
            sender.sendGroupMsg(groupMsg, "你没有权限哦");
        } else {
            String groupCode = groupMsg.getGroupInfo().getGroupCode();
            GroupPower groupPower = GROUP_CONFIG_MAP.get(groupCode);
            groupPower.setMaiyaoSwitch(!groupPower.isMaiyaoSwitch());
            setJson(GROUP_CONFIG_MAP);
            if (groupPower.isMaiyaoSwitch()) {
                sender.sendGroupMsg(groupMsg, "定时任务开启");
            } else {
                sender.sendGroupMsg(groupMsg, "定时任务关闭");
            }
        }
    }

    @OnPrivate
    @Filter("/fresh")
    public void fresh(PrivateMsg privateMsg, Sender sender) throws IOException {
        String qq = privateMsg.getAccountInfo().getAccountCode();
        if (!Objects.equals(qq, COMMON_CONFIG.getMasterQQ())) {
            sender.sendPrivateMsg(privateMsg, "你没有权限哦");
        } else {
            FileUtils.deleteDirectory(new File(CACHE_DIR));
            InitConfig.initConfigs();
            sender.sendPrivateMsg(privateMsg, "已删除cache文件夹并重新加载配置");
        }
    }

    private synchronized void setJson(Map<String, GroupPower> map) {
        String jsonObject = JSON.toJSONString(map);
        try {
            File file = new File(CONFIG_DIR + "/config.json");
            if (!file.exists() || !file.isFile()) {
                FileUtils.touch(file);
            }
            FileUtils.write(file, jsonObject, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
