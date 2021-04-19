package io.koschicken.listener;

import io.koschicken.intercept.limit.Limit;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RepeaterListener {

    Map<String, Deque<String>> stackMap = new HashMap<>(); // 用于存储不同群的消息栈

    @Limit(value = 10, sendMsg = false)
    @OnGroup
    public void startRepeat(GroupMsg msg, MsgSender sender) {
        String groupCode = msg.getGroupInfo().getGroupCode();
        Deque<String> stack = stackMap.get(groupCode); // 根据群号获取消息栈
        String currentMsg = msg.getMsg(); // 获取当前消息内容
        if (stack == null) {
            stack = new ArrayDeque<>();
            stackMap.put(groupCode, stack);
        }
        stackMsg(groupCode, sender, stack, currentMsg);
    }

    private void stackMsg(String groupCode, MsgSender sender, Deque<String> stack, String currentMsg) {
        if (repeatFlag(currentMsg)) {
            return;
        }
        if (stack.isEmpty()) { // 如果栈是空的，入栈
            stack.push(currentMsg);
        } else {
            if (currentMsg.equals(stack.peek())) { // 如果当前消息内容和栈顶内容相同则入栈
                stack.push(currentMsg);
            } else {
                stack.clear(); // 如果当前消息和栈顶内容不同（复读中断），则清空栈
            }
        }
        if (stack.size() > RandomUtils.nextInt(1, 5)) {
            sender.SENDER.sendGroupMsg(groupCode, currentMsg); // 复读
            stack.clear(); // 复读之后清空栈
        }
    }

    private boolean repeatFlag(String msg) {
        List<String> commandList = new ArrayList<>();
        commandList.add("签到");
        commandList.add("骰子说明");
        commandList.add("叫车");
        commandList.add("余额");
        commandList.add("我有多少钱");
        boolean containsSharp = msg.contains("#");
        return containsSharp || commandList.contains(msg);
    }
}
