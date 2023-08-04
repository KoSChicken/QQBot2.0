package io.koschicken.listener;

import catcode.CatCodeUtil;
import io.koschicken.utils.URLUtils;
import lombok.extern.slf4j.Slf4j;
import love.forte.common.ioc.annotation.Beans;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.containers.GroupAccountInfo;
import love.forte.simbot.api.message.containers.GroupInfo;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.Sender;
import org.apache.commons.lang3.RandomUtils;
import org.apache.http.client.fluent.Request;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import static io.koschicken.intercept.BotIntercept.GROUP_CONFIG_MAP;

/**
 * 群消息监听的示例类。
 * 所有需要被管理的类都需要标注 {@link Service} 注解。
 * <p>
 * 由于当前是处于springboot环境下，因此强烈建议类上的注释使用：
 * <ul>
 *     <li>{@link org.springframework.stereotype.Component}</li>
 *     <li>{@link Service}</li>
 * </ul>
 * 等注解来代替simbot的 {@link Beans}。
 * <p>
 * 同样的，依赖注入也请使用 {@link org.springframework.beans.factory.annotation.Autowired} 等Springboot相关的注解。
 *
 * @author ForteScarlet
 */
@Slf4j
@Service
public class GroupListener {

    @OnGroup
    @Filter("xsp")
    public void greetings(GroupMsg groupMsg, Sender sender) {
        CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
        int index = RandomUtils.nextInt(0, 4);
        File image = new File("./resource/image/greeting" + index + ".jpg");
        if (image.exists()) {
            String cat = catCodeUtil.getStringTemplate().image(image.getAbsolutePath());
            sender.sendGroupMsg(groupMsg, cat);
        }
    }

    @OnGroup
    @Filter(atBot = true)
    public void zuichou(GroupMsg groupMsg, Sender sender) throws IOException {
        String api = "http://81.70.100.130/api/Ridicule.php?msg=";
        String string = Request.Get(api + RandomUtils.nextInt(1,6)).execute().returnContent().asString();
        sender.sendGroupMsg(groupMsg, string);
    }

    @OnGroup
    public void pageDescription(GroupMsg groupMsg, Sender sender) {
        String msg = groupMsg.getMsg();
        try {
            URL url = new URL(msg);// Test msg is url or not
            if (url.getHost().contains("bilibili")) {
                return; // pass bilibili
            }
            String description = URLUtils.pageDescription(msg);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(msg).append("\n").append(description);
            if (!stringBuilder.isEmpty()) {
                String groupCode = groupMsg.getGroupInfo().getGroupCode();
                if (Objects.nonNull(GROUP_CONFIG_MAP.get(groupCode)) && GROUP_CONFIG_MAP.get(groupCode).isGlobalSwitch()) {
                    sender.sendGroupMsg(groupCode, stringBuilder.toString());
                }
            }
        } catch (MalformedURLException ignore) {
        } catch (IOException e) {
            log.error("获取网页简介失败");
        }
    }

    /**
     * 此监听函数代表，收到消息的时候，将消息的各种信息打印出来。
     * <p>
     * 此处使用的是模板注解 {@link OnGroup}, 其代表监听一个群消息。
     * <p>
     * 由于你监听的是一个群消息，因此你可以通过 {@link GroupMsg} 作为参数来接收群消息内容。
     */
    @OnGroup
    public void onGroupMsg(GroupMsg groupMsg) {
        // 打印此次消息中的 纯文本消息内容。
        // 纯文本消息中，不会包含任何特殊消息（例如图片、表情等）。
//        System.out.println(groupMsg.getText());

        // 打印此次消息中的 消息内容。
        // 消息内容会包含所有的消息内容，也包括特殊消息。特殊消息使用CAT码进行表示。
        // 需要注意的是，绝大多数情况下，getMsg() 的效率低于甚至远低于 getText()
//        System.out.println(groupMsg.getMsg());

        // 获取此次消息中的 消息主体。
        // messageContent代表消息主体，其中通过可以获得 msg, 以及特殊消息列表。
        // 特殊消息列表为 List<Neko>, 其中，Neko是CAT码的封装类型。
//        MessageContent msgContent = groupMsg.getMsgContent();

        // 打印消息主体
        // System.out.println(msgContent);
        // 打印消息主体中的所有图片的链接（如果有的话）
//        List<Neko> imageCats = msgContent.getCats("image");
//        System.out.println("img counts: " + imageCats.size());
//        for (Neko image : imageCats) {
//            System.out.println("Img url: " + image.get("url"));
//        }

        // 获取发消息的人。
        GroupAccountInfo accountInfo = groupMsg.getAccountInfo();
        // 发消息者的账号与昵称。
        String accountCode = accountInfo.getAccountCode();
        String accountNickname = accountInfo.getAccountNickname();
        // 获取群信息
        GroupInfo groupInfo = groupMsg.getGroupInfo();
        // 群号与名称
        String groupCode = groupInfo.getGroupCode();
        String groupName = groupInfo.getGroupName();

        log.info("{}({}) - {}({}): {}", groupName, groupCode, accountNickname, accountCode, groupMsg.getMsg());
    }
}
