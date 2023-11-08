package io.koschicken.intercept;

import io.koschicken.bot.Groups;
import io.koschicken.config.BotConfig;
import io.koschicken.config.GroupConfig;
import io.koschicken.utils.ConfigFileUtils;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.MsgGet;
import love.forte.simbot.intercept.InterceptionType;
import love.forte.simbot.listener.MsgInterceptContext;
import love.forte.simbot.listener.MsgInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class BotIntercept implements MsgInterceptor {

    @NotNull
    @Override
    public InterceptionType intercept(@NotNull MsgInterceptContext context) {
        MsgGet msgGet = context.getMsgGet();
        if (msgGet instanceof GroupMsg groupMsg) {
            String groupCode = groupMsg.getGroupInfo().getGroupCode();
            String msg = groupMsg.getMsg();
            GroupConfig groupConfig = Groups.getInstance().get(groupCode);
            // 当该群组配置不存在时，初始化并保存到配置文件中
            if (Objects.isNull(groupConfig)) {
                groupConfig = initGroupConfig(groupCode);
            }
            // 总体开关
            if (!groupConfig.isGlobalSwitch()) {
                BotConfig botConfig = BotConfig.getInstance();
                String masterQQ = botConfig.getMasterQQ();
                String qq = groupMsg.getAccountInfo().getAccountCode();
                if (isOpen(msg) && Objects.equals(masterQQ, qq)) return InterceptionType.ALLOW;
                return InterceptionType.BLOCK;
            }
            // 骰子消息过滤
            if (isDice(msg)) {
                if (groupConfig.isDiceSwitch()) return InterceptionType.ALLOW;
                return InterceptionType.BLOCK;
            }
            // setu消息过滤
            if (isSetu(msg)) {
                if (groupConfig.isSetuSwitch()) return InterceptionType.ALLOW;
                return InterceptionType.BLOCK;
            }
        }
        return InterceptionType.ALLOW;
    }

    private GroupConfig initGroupConfig(String groupCode) {
        GroupConfig groupConfig = new GroupConfig();
        Groups groups = Groups.getInstance();
        groups.put(groupCode, groupConfig);
        ConfigFileUtils.updateGroupConfig(groups);
        return groupConfig;
    }

    private boolean isDice(String msg) {
        return msg.startsWith("#骰子") || msg.startsWith("骰子说明") || msg.startsWith("押骰子")
                || msg.startsWith("#投掷骰子") || msg.startsWith("#豹？") || msg.startsWith("#roll");
    }

    private boolean isSetu(String msg) {
        return msg.startsWith("叫车") || msg.startsWith("叫車") || msg.startsWith("#抽奖") || msg.startsWith("#mjx")
                || msg.contains("色图") || msg.contains("涩图") || msg.contains("色圖") || msg.contains("澀圖");
    }

    private boolean isOpen(String msg) {
        return "/on".equals(msg);
    }
}
