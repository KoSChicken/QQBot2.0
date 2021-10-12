package io.koschicken.listener;

import com.alibaba.fastjson.JSON;
import io.koschicken.bean.GroupPower;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.Sender;
import org.apache.commons.io.FileUtils;
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
