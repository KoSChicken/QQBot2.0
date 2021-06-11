package io.koschicken.listener;

import catcode.CatCodeUtil;
import catcode.Neko;
import io.koschicken.intercept.limit.Limit;
import lombok.Data;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RepeaterListener {

    Map<String, Deque<Msg>> stackMap = new HashMap<>(); // 用于存储不同群的消息栈

    @Limit(value = 10, sendMsg = false)
    @OnGroup
    public void startRepeat(GroupMsg msg, MsgSender sender) {
        String groupCode = msg.getGroupInfo().getGroupCode();
        Deque<Msg> stack = stackMap.get(groupCode); // 根据群号获取消息栈
        List<Neko> cats = msg.getMsgContent().getCats();
        // 包含多个非纯文本内容的消息不复读
        if (cats.size() > 1) {
            return;
        }
        Msg currentMsg;
        // 如果没有猫猫码，说明是纯文本消息
        if (cats.size() == 0) {
            currentMsg = new Msg();
            currentMsg.setContent(msg.getText());
        } else {
            currentMsg = new Msg();
            Neko neko = cats.get(0);
            if ("image".equals(neko.getType())) {
                currentMsg.setNeko(neko);
            }
        }

        if (stack == null) {
            stack = new ArrayDeque<>();
            stackMap.put(groupCode, stack);
        }
        stackMsg(groupCode, sender, stack, currentMsg);
    }

    private void stackMsg(String groupCode, MsgSender sender, Deque<Msg> stack, Msg currentMsg) {
        if (repeatFlag(currentMsg)) {
            return;
        }
        if (stack.isEmpty()) { // 如果栈是空的，入栈
            stack.push(currentMsg);
        } else {
            Msg peek = stack.peek();
            String content = currentMsg.getContent();
            Neko neko = currentMsg.getNeko();
            String peekContent = peek.getContent();
            Neko peekNeko = peek.getNeko();
            if (Objects.nonNull(content)) {
                // 文本消息，比较文本是否相同
                if (content.equals(peekContent)) { // 如果当前消息内容和栈顶内容相同则入栈
                    stack.push(currentMsg);
                } else {
                    stack.clear(); // 如果当前消息和栈顶内容不同（复读中断），则清空栈
                }
                if (stack.size() > RandomUtils.nextInt(1, 5)) {
                    sender.SENDER.sendGroupMsg(groupCode, content); // 复读
                    stack.clear(); // 复读之后清空栈
                }
            } else {
                // 非文本消息
                if (compareNeko(neko, peekNeko)) {
                    stack.push(currentMsg);
                } else {
                    stack.clear();
                }
                 if (stack.size() > RandomUtils.nextInt(1, 5)) {
                    CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
                    String image = catCodeUtil.getStringTemplate().image(Objects.requireNonNull(neko.get("url")));
                    sender.SENDER.sendGroupMsg(groupCode, image); // 复读
                    stack.clear(); // 复读之后清空栈
                }
            }
        }
    }

    private boolean repeatFlag(Msg msg) {
        String content = msg.getContent();
        if (content != null) {
            List<String> commandList = new ArrayList<>();
            commandList.add("签到");
            commandList.add("骰子说明");
            commandList.add("叫车");
            commandList.add("余额");
            commandList.add("我有多少钱");
            return content.startsWith("#") || commandList.stream().anyMatch(content::contains);
        }
        return false;
    }

    /**
     * 比较Neko
     * 如果type和id都相同，则相同
     * @param nekoA nekoA
     * @param nekoB nekoB
     */
    private boolean compareNeko(Neko nekoA, Neko nekoB) {
        return Objects.nonNull(nekoA) && Objects.nonNull(nekoB)
                && Objects.equals(nekoA.getType(), nekoB.getType())
                && Objects.equals(nekoA.get("id"), nekoB.get("id"));
    }

    @Data
    class Msg {
        private String content;
        private Neko neko;
    }
}
