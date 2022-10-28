package io.koschicken.listener;

import catcode.CatCodeUtil;
import catcode.Neko;
import lombok.extern.slf4j.Slf4j;
import love.forte.common.ioc.annotation.Beans;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnPrivate;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.MessageContentBuilder;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
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

    /**
     * 通过依赖注入获取一个 "消息正文构建器工厂"。
     */
    private final MessageContentBuilderFactory messageContentBuilderFactory;
    private final MiraiMessageContentBuilderFactory factory;

    @Autowired
    public PrivateListener(MessageContentBuilderFactory messageContentBuilderFactory, MiraiMessageContentBuilderFactory factory) {
        this.messageContentBuilderFactory = messageContentBuilderFactory;
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
                messageContent = factory.getMessageContentBuilder().image(cat.get("url")).build();
            } else if ("text".equals(type)) {
                messageContent = factory.getMessageContentBuilder().text(cat.get("text")).build();
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

    /**
     * 此监听函数监听一个私聊消息，并会复读这个消息，然后再发送一个表情。
     * 此方法上使用的是一个模板注解{@link OnPrivate}，
     * 其代表监听私聊。
     * 由于你监听的是私聊消息，因此参数中要有个 {@link PrivateMsg} 来接收这个消息实体。
     * <p>
     * 其次，由于你要“复读”这句话，因此你需要发送消息，
     * 因此参数中你需要一个 "消息发送器" {@link Sender}。
     * <p>
     * 当然，你也可以使用 {@link love.forte.simbot.api.sender.MsgSender}，
     * 然后 {@code msgSender.SENDER}.
     */
    // @OnPrivate
    public void replyPrivateMsg1(PrivateMsg privateMsg, Sender sender) {
        // 获取消息正文。
        MessageContent msgContent = privateMsg.getMsgContent();

        // 向 privateMsg 的账号发送消息，消息为当前接收到的消息。
        sender.sendPrivateMsg(privateMsg, msgContent);

        // 再发送一个表情ID为'9'的表情。
        // 方法1：使用消息构建器构建消息并发送
        // 在绝大多数情况下，使用消息构建器所构建的消息正文 'MessageContent'
        // 是用来发送消息最高效的选择。
        // 相对的，MessageContentBuilder所提供的构建方法是十分有限的。

        // 获取消息构建器
        MessageContentBuilder msgBuilder = messageContentBuilderFactory.getMessageContentBuilder();
        // 通过.text(...) 向builder中追加一句话。
        // 通过.face(ID) 向builder中追加一个表情。
        // 通过.build() 构建出最终消息。
        MessageContent msg = msgBuilder.text("表情：").face(9).build();

        // 直接通过这个msg发送。
        sender.sendPrivateMsg(privateMsg, msg);

        // 方法2：使用CAT码发送消息。
        // 使用CAT码构建一个需要解析的消息是最灵活的，
        // 但是相对的，它的效率并不是十分的可观，毕竟在这其中可能会涉及到很多的'解析'操作。

        // 获取CAT码工具类实例
        CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();

        // 构建一个类型为 'face', 参数为 'id=9' 的CAT码。
        // 有很多方法。

        // 1. 通过 codeBuilder 构建CAT码
        // String cat1 = catCodeUtil.getStringCodeBuilder("face", false).key("id").value(9).build();

        // 2. 通过CatCodeUtil.toCat 构建CAT码
        // String cat2 = catCodeUtil.toCat("face", "id=9");

        // 3. 通过模板构建CAT码
        String cat3 = catCodeUtil.getStringTemplate().face(9);

        // 在cat码前增加一句 '表情' 并发送
        sender.sendPrivateMsg(privateMsg, "表情：" + cat3);
    }
}
