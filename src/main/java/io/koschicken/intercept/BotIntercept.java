package io.koschicken.intercept;

import com.alibaba.fastjson.JSON;
import io.koschicken.bean.GroupPower;
import io.koschicken.constants.Constants;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.MsgGet;
import love.forte.simbot.intercept.InterceptionType;
import love.forte.simbot.listener.MsgInterceptContext;
import love.forte.simbot.listener.MsgInterceptor;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static io.koschicken.constants.Constants.CONFIG_DIR;

@Service
public class BotIntercept implements MsgInterceptor {

    public static Map<String, GroupPower> GROUP_CONFIG_MAP = new ConcurrentHashMap<>(10);

    @NotNull
    @Override
    public InterceptionType intercept(@NotNull MsgInterceptContext context) {
        MsgGet msgGet = context.getMsgGet();
        if (msgGet instanceof GroupMsg) {
            String groupCode = ((GroupMsg) msgGet).getGroupInfo().getGroupCode();
            String msg = ((GroupMsg) msgGet).getMsg();
            GroupPower groupPower = GROUP_CONFIG_MAP.get(groupCode);
            if (Objects.isNull(groupPower)) {
                initGroupPower(groupCode);
            }
            //总体开关
            if (!groupPower.isGlobalSwitch()) {
                String masterQQ = Constants.COMMON_CONFIG.getMasterQQ();
                String qq = ((GroupMsg) msgGet).getAccountInfo().getAccountCode();
                if (isOpen(msg) && Objects.equals(masterQQ, qq)) return InterceptionType.ALLOW;
                return InterceptionType.BLOCK;
            }
            //抽卡消息过滤
            if (isChouKa(msg)) {
                if (groupPower.isGachaSwitch()) return InterceptionType.ALLOW;
                return InterceptionType.BLOCK;
            }
            //赛马消息过滤
            if (isHorse(msg)) {
                if (groupPower.isHorseSwitch()) return InterceptionType.ALLOW;
                return InterceptionType.BLOCK;
            }
            //骰子消息过滤
            if (isDice(msg)) {
                if (groupPower.isDiceSwitch()) return InterceptionType.ALLOW;
                return InterceptionType.BLOCK;
            }
            //setu消息过滤
            if (isSetu(msg)) {
                if (groupPower.isSetuSwitch()) return InterceptionType.ALLOW;
                return InterceptionType.BLOCK;
            }
        }
        return InterceptionType.ALLOW;
    }

    private void initGroupPower(String groupCode) {
        GroupPower groupPower = new GroupPower();
        groupPower.setGlobalSwitch(Constants.COMMON_CONFIG.isGlobalSwitch());
        groupPower.setMaiyaoSwitch(Constants.COMMON_CONFIG.isMaiyaoSwitch());
        groupPower.setGachaSwitch(Constants.COMMON_CONFIG.isGachaSwitch());
        groupPower.setHorseSwitch(Constants.COMMON_CONFIG.isHorseSwitch());
        groupPower.setHorseSwitch(Constants.COMMON_CONFIG.isDiceSwitch());
        groupPower.setHorseSwitch(Constants.COMMON_CONFIG.isSetuSwitch());
        GROUP_CONFIG_MAP.put(groupCode, groupPower);
        setJson(GROUP_CONFIG_MAP);
    }

    private boolean isChouKa(String msg) {
        return msg.startsWith("#十连") || msg.startsWith("#up十连") || msg.startsWith("#井")
                || msg.startsWith("#up井") || msg.startsWith("#抽卡") || msg.startsWith("#up抽卡");
    }

    private boolean isHorse(String msg) {
        return msg.startsWith("#赛") || msg.startsWith("#开始赛") || msg.startsWith("押马");
    }

    private boolean isDice(String msg) {
        return msg.startsWith("#骰子") || msg.startsWith("骰子说明") || msg.startsWith("押骰子")
                || msg.startsWith("#投掷骰子") || msg.startsWith("#豹？") || msg.startsWith("#roll");
    }

    private boolean isSetu(String msg) {
        return msg.startsWith("叫车") || msg.startsWith("叫車") || msg.startsWith("#抽奖") || msg.startsWith("#mjx")
                || msg.contains("色图") || msg.contains("涩图")|| msg.contains("色圖") || msg.contains("澀圖");
    }

    private boolean isOpen(String msg) {
        return "#启用Bot".equals(msg);
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
