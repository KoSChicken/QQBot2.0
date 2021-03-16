package io.koschicken.intercept.limit;

import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.MsgGet;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.sender.BotSender;
import love.forte.simbot.bot.BotManager;
import love.forte.simbot.intercept.InterceptionType;
import love.forte.simbot.listener.ListenerFunction;
import love.forte.simbot.listener.ListenerInterceptContext;
import love.forte.simbot.listener.ListenerInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LimitIntercept implements ListenerInterceptor {

    @Autowired
    private BotManager botManager;

    /**
     * limit map
     */
    private final Map<String, ListenLimit> limitMap = new ConcurrentHashMap<>();

    /**
     * 预估一个StringBuilder的长度
     */
    private static int estimatedLength(ListenerFunction listenerFunction, boolean group, boolean code, boolean bot) {
        int len = 100 + listenerFunction.toString().length();
        if (group) {
            len += 10;
        }
        if (code) {
            len += 10;
        }
        if (bot) {
            len += 10;
        }
        return len;
    }

    @NotNull
    @Override
    public InterceptionType intercept(@NotNull ListenerInterceptContext context) {
        ListenerFunction listenerFunction = context.getListenerFunction();
        Limit limit = listenerFunction.getAnnotation(Limit.class);
        if (limit == null) {
            return InterceptionType.ALLOW;
        } else {
            final MsgGet msgGet = context.getMsgGet();
            final long time = limit.timeUnit().toMillis(limit.value());
            final boolean isGroup = limit.group() && msgGet instanceof GroupMsg;
            final boolean isCode = limit.code() && msgGet instanceof PrivateMsg;
            final boolean isBot = limit.bot();
            String groupCode = null;
            String code = null;

            final StringBuilder keyStringBuilder = new StringBuilder(estimatedLength(listenerFunction, isGroup, isCode, isBot));
            keyStringBuilder.append(limit.toString()).append(listenerFunction.toString());
            if (isGroup) {
                groupCode = ((GroupMsg) msgGet).getGroupInfo().getGroupCode();
                keyStringBuilder.append(groupCode);
            }
            if (isCode) {
                code = msgGet.getAccountInfo().getAccountCode();
                keyStringBuilder.append(code);
            }
            if (isBot) {
                keyStringBuilder.append(msgGet.getBotInfo().getAccountCode());
            }
            final String key = keyStringBuilder.toString();
            final ListenLimit listenLimit = limitMap.computeIfAbsent(key, h -> new ListenLimit(time));
            if (!listenLimit.expired()) {
                sendMessage(isGroup, isCode, groupCode, code, limit.message());
                return InterceptionType.BLOCK;
            } else {
                return InterceptionType.ALLOW;
            }
        }
    }

    private void sendMessage(boolean isGroup, boolean isCode, String groupCode, String code, String message) {
        BotSender sender = botManager.getDefaultBot().getSender();
        if (isGroup) {
            sender.SENDER.sendGroupMsg(groupCode, message);
            return;
        }
        if (isCode) {
            sender.SENDER.sendPrivateMsg(code, message);
        }
    }
}
