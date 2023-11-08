package io.koschicken.listener;

import catcode.CatCodeUtil;
import catcode.Neko;
import lombok.extern.slf4j.Slf4j;
import love.forte.common.ioc.annotation.Beans;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnPrivate;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.component.mirai.message.MiraiMessageContent;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilder;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 私聊消息监听的示例类。
 * 所有需要被管理的类都需要标注 {@link Service} 注解。
 * <p>
 * 由于当前是处于springboot环境下，因此强烈建议类上的注释使用：
 * <ul>
 *     <li>{@link org.springframework.stereotype.Component}</li>
 *     <li>{@link Service}</li>
 * </ul>
 * 等注解来代替simbot的 {@link Beans}。
 * <p>
 * 同样的，依赖注入也请使用 {@link Autowired} 等Springboot相关的注解。
 *
 * @author ForteScarlet
 */
@Slf4j
@Service
public class PrivateListener {

    private static final ConcurrentHashMap<String, List<MessageContent>> MSG_MAP = new ConcurrentHashMap<>();

    private final MiraiMessageContentBuilderFactory factory;

    public PrivateListener(MiraiMessageContentBuilderFactory factory) {
        this.factory = factory;
    }

    @OnPrivate
    public void cache(PrivateMsg privateMsg) {
        if (StringUtils.hasText(privateMsg.getMsgContent().getMsg()) && privateMsg.getMsgContent().getMsg().contains("合并")) {
            return;
        }
        log.info("msg cats: {}", privateMsg.getMsgContent().getCats());
        String accountCode = privateMsg.getAccountInfo().getAccountCode();
        List<MessageContent> list = MSG_MAP.get(accountCode);
        if (list == null) {
            list = new ArrayList<>();
        }
        List<MessageContent> messageContents = listContent(privateMsg);
        list.addAll(messageContents);
        MSG_MAP.put(accountCode, list);
        log.info("list {}, size: {}", accountCode, list.size());
    }

    @OnPrivate
    @Filter("合并")
    public void comp(PrivateMsg privateMsg, Sender sender) {
        String accountCode = privateMsg.getAccountInfo().getAccountCode();
        List<MessageContent> list = MSG_MAP.get(accountCode);
        MiraiMessageContentBuilder messageContentBuilder = factory.getMessageContentBuilder();
        messageContentBuilder.forwardMessage(forwardBuilder -> {
            for (MessageContent msg : list) {
                forwardBuilder.add(privateMsg.getAccountInfo(), msg);
            }
        });
        final MiraiMessageContent messageContent = messageContentBuilder.build();
        sender.sendPrivateMsg(privateMsg, messageContent);
        MSG_MAP.remove(accountCode);
    }

    @OnPrivate
    @Filter("xsp")
    public void greetings(PrivateMsg privateMsg, Sender sender) {
        CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
        File image = new File("./resource/image/greeting.jpg");
        if (image.exists()) {
            String cat = catCodeUtil.getStringTemplate().image(image.getAbsolutePath());
            sender.sendPrivateMsg(privateMsg, cat);
        }
    }

    private List<MessageContent> listContent(PrivateMsg msg) {
        List<Neko> cats = msg.getMsgContent().getCats();
        List<MessageContent> list = new ArrayList<>();
        for (Neko cat : cats) {
            MessageContent messageContent;
            String type = cat.getType();
            if ("image".equals(type)) {
                String url = cat.get("url");
                assert url != null;
                messageContent = factory.getMessageContentBuilder().image(url).build();
            } else if ("text".equals(type)) {
                String text = cat.get("text");
                assert text != null;
                messageContent = factory.getMessageContentBuilder().text(text).build();
            } else {
                messageContent = null;
            }
            if (Objects.nonNull(messageContent)) {
                list.add(messageContent);
            }
        }
        log.info("list: {}", list);
        return list;
    }
}
