package io.koschicken.listener.setu;

import catcode.CatCodeUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import io.koschicken.bean.setu.LoliconResponse;
import io.koschicken.bean.setu.Pixiv;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.MsgGet;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.message.results.GroupMemberInfo;
import love.forte.simbot.api.message.results.GroupMemberList;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.component.mirai.message.MiraiMessageContent;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilder;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilderFactory;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.koschicken.constants.Constants.COMMON_CONFIG;

@Slf4j
@Data
public class SetuRunner implements Callable<LoliconResponse> {

    private static final String SETU_DIR = "./temp/SETU/";
    private static final String SETU_COMP_DIR = "./temp/SETU/comp/";
    private static final String ARTWORK_PREFIX = "https://www.pixiv.net/artworks/";
    private static final String ARTIST_PREFIX = "https://www.pixiv.net/users/";
    private static final String AVATAR_API = "http://thirdqq.qlogo.cn/g?b=qq&nk=";
    private static final HashMap<String, Integer> NUMBER;

    static {
        NUMBER = new HashMap<>();
        NUMBER.put("一", 1);
        NUMBER.put("二", 2);
        NUMBER.put("俩", 2);
        NUMBER.put("两", 2);
        NUMBER.put("三", 3);
        NUMBER.put("四", 4);
        NUMBER.put("五", 5);
        NUMBER.put("六", 6);
        NUMBER.put("七", 7);
        NUMBER.put("八", 8);
        NUMBER.put("九", 9);
        NUMBER.put("十", 10);
        NUMBER.put("几", RandomUtils.nextInt(1, 4));

        File setuFolder = new File(SETU_DIR);
        if (!setuFolder.exists()) {
            try {
                FileUtils.forceMkdir(setuFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File setuCompFolder = new File(SETU_COMP_DIR);
        if (!setuCompFolder.exists()) {
            try {
                FileUtils.forceMkdir(setuCompFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private MsgGet msg;
    private MiraiMessageContentBuilderFactory factory;
    private MsgSender sender;

    public SetuRunner(MsgGet msg, MiraiMessageContentBuilderFactory factory, MsgSender sender) {
        this.msg = msg;
        this.factory = factory;
        this.sender = sender;
    }

    @Override
    public LoliconResponse call() throws Exception {
        String message;
        boolean isGroup;
        if (msg instanceof GroupMsg) {
            message = ((GroupMsg) msg).getMsg();
            isGroup = true;
        } else {
            message = ((PrivateMsg) msg).getMsg();
            isGroup = false;
        }
        Pixiv pixiv = parseMsg(message);
        String keyword = pixiv.getKeyword();
        int num = pixiv.getNum();
        boolean r18 = pixiv.isR18();
        log.info("tag={}, num={}, r18={}", keyword, num, r18);
        LoliconResponse loliconResponse;
        if (!tagCheck(keyword)) {
            loliconResponse = Pixiv.get(keyword, num, r18);
        } else {
            String first = keyword.trim().substring(0, 1);
            loliconResponse = new LoliconResponse("", new ArrayList<>(), first + "nmlgb");
        }
        boolean sent = isGroup && groupMember((GroupMsg) msg, sender, keyword);
        if (!sent) {
            send(loliconResponse, isGroup);
        }
        return loliconResponse;
    }

    private Pixiv parseMsg(String msg) {
        String regex = msg.startsWith("叫") ? "^叫[车車](.*)?(|r18)$" : "^[来來](.*?)[点點丶份张張](.*?)的?(|r18)[色瑟涩澀][图圖]$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(msg);
        int num = 1;
        String tag = "";
        boolean r18 = false;
        String number;
        while (m.find()) {
            if (msg.startsWith("叫")) {
                tag = m.group(1).trim();
                if ("r18".equals(tag) || "R18".equals(tag)) {
                    tag = "";
                    r18 = true;
                } else {
                    r18 = !StringUtils.isEmpty(m.group(2).trim());
                }
            } else {
                number = m.group(1).trim();
                tag = m.group(2).trim();
                r18 = !StringUtils.isEmpty(m.group(3).trim());
                try {
                    num = NUMBER.get(number) == null ? Math.min(10, Integer.parseInt(number)) : NUMBER.get(number);
                } catch (NumberFormatException ignore) {
                    log.info("number set to 1");
                }
            }
        }
        return new Pixiv(tag, num, r18);
    }

    private boolean tagCheck(String tag) {
        String tags = COMMON_CONFIG.getSetuBlackTags();
        if (StringUtils.isEmpty(tags)) {
            return false;
        }
        List<String> tagList = Arrays.asList(tags.split(","));
        int i = RandomUtils.nextInt(1, 100);
        log.info(i + " - " + tags);
        return i <= 50 && tagList.contains(tag);
    }

    private void send(LoliconResponse loliconResponse, boolean isGroup) {
        List<MessageContent> msgList = listMsg(loliconResponse);
        if (msgList.size() > 1 || checkR18(loliconResponse)) {
            MiraiMessageContentBuilder messageContentBuilder = factory.getMessageContentBuilder();
                messageContentBuilder.forwardMessage(forwardBuilder -> {
                    for (MessageContent messageContent : msgList) {
                        forwardBuilder.add(RandomUtils.nextBoolean() ? msg.getAccountInfo() : randomGroupMember(), messageContent);
                    }
                });
            final MiraiMessageContent messageContent = messageContentBuilder.build();
            if (isGroup) {
                sender.SENDER.sendGroupMsg((GroupMsg) msg, messageContent);
            } else {
                sender.SENDER.sendPrivateMsg(msg, messageContent);
            }
        } else {
            if (isGroup) {
                sender.SENDER.sendGroupMsg((GroupMsg) msg, msgList.get(0));
            } else {
                sender.SENDER.sendPrivateMsg(msg, msgList.get(0));
            }
        }
    }

    private boolean checkR18(LoliconResponse loliconResponse) {
        return Objects.nonNull(loliconResponse)
                && Objects.nonNull(loliconResponse.getData())
                && loliconResponse.getData().stream().anyMatch(Pixiv::isR18);
    }

    private GroupMemberInfo randomGroupMember() {
        List<GroupMemberInfo> groupMemberList = sender.GETTER.getGroupMemberList(((GroupMsg) msg).getGroupInfo().getGroupCode())
                .stream().collect(Collectors.toList());
        Collections.shuffle(groupMemberList);
        return groupMemberList.get(0);
    }

    private List<MessageContent> listMsg(LoliconResponse loliconResponse) {
        String error = loliconResponse.getError();
        String customMsg = loliconResponse.getCustomMsg();
        List<Pixiv> data = loliconResponse.getData();
        List<MessageContent> msgList = new ArrayList<>();
        MiraiMessageContentBuilder messageContentBuilder = factory.getMessageContentBuilder();
        if (StringUtils.isNotEmpty(customMsg)) {
            msgList.add(messageContentBuilder.text(customMsg).build());
            return msgList;
        }
        if (StringUtils.isEmpty(error)) {
            if (CollectionUtils.isNotEmpty(data)) {
                data.parallelStream().forEach(p -> msgList.add(buildMessage(p)));
                List<MessageContent> errors = msgList.stream().filter(m -> m.getMsg().toLowerCase().contains("exception")).collect(Collectors.toList());
                msgList.removeAll(errors);
                if (CollectionUtils.isEmpty(msgList)) {
                    File image = new File("./resource/image/zmsn.jpg");
                    if (image.exists()) {
                        msgList.add(messageContentBuilder.image(image.getAbsolutePath()).build());
                    } else {
                        msgList.add(messageContentBuilder.text("炸了").build());
                    }
                }
                log.info("{} error(s), {} content(s)", errors.size(), msgList.size());
                return msgList;
            } else {
                File image = new File("./resource/image/mao.jpg");
                if (image.exists()) {
                    msgList.add(messageContentBuilder.image(image.getAbsolutePath()).build());
                } else {
                    msgList.add(messageContentBuilder.text("冇").build());
                }
            }
        } else {
            msgList.add(messageContentBuilder.text(error).build());
        }
        return msgList;
    }

    private MessageContent buildMessage(Pixiv p) {
        MiraiMessageContentBuilder messageContentBuilder = factory.getMessageContentBuilder();
        String pid = p.getPid().toString();
        try {
            File originalFile = new File(SETU_DIR + pid + "." + p.getExt());
            if (!originalFile.exists()) {
                FileUtils.copyURLToFile(new URL(p.getUrls().get("original")), originalFile);
            }
            log.info("原图创建完成，pid={}, file={}", pid, originalFile.getName());
            File compressedJPG = new File(SETU_COMP_DIR + pid + ".jpg");
            Thumbnails.of(originalFile).scale(1).outputQuality(1).toFile(compressedJPG);
            String message = "\n" +
                    p.getTitle() + "\n" +
                    ARTWORK_PREFIX + p.getPid() + "\n" +
                    p.getAuthor() + "\n" +
                    ARTIST_PREFIX + p.getUid() + "\n";
            // + "tags:" + Arrays.toString(p.getTags());
            return messageContentBuilder.image(compressedJPG.getAbsolutePath()).text(message).build();
        } catch (IOException e) {
            e.printStackTrace();
            return messageContentBuilder.text(e.getClass().getName()).build();
        }
    }

    private boolean groupMember(GroupMsg msg, MsgSender sender, String tag) {
        GroupMemberList groupMemberList = sender.GETTER.getGroupMemberList(msg);
        for (GroupMemberInfo member : groupMemberList) {
            String remarkOrNickname = member.getAccountRemarkOrNickname();
            if (Objects.nonNull(remarkOrNickname) && Objects.equals(tag, remarkOrNickname.trim())) {
                log.info("群名片：{}\ttag: {}", remarkOrNickname, tag);
                String api = AVATAR_API + member.getAccountCode() + "&s=640";
                try {
                    InputStream imageStream = Request.Get(api).execute().returnResponse().getEntity().getContent();
                    File pic = new File(SETU_DIR + member.getAccountCode() + System.currentTimeMillis() + ".jpg");
                    FileUtils.copyInputStreamToFile(imageStream, pic);
                    CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
                    String image = catCodeUtil.getStringTemplate().image(pic.getAbsolutePath());
                    sender.SENDER.sendGroupMsg(msg, image);
                    FileUtils.deleteQuietly(pic);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
