package io.koschicken.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.koschicken.annotation.Limit;
import io.koschicken.bean.Pixiv;
import io.koschicken.db.bean.Account;
import io.koschicken.db.service.AccountService;
import io.koschicken.utils.SetuUtils;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.results.GroupMemberInfo;
import love.forte.simbot.api.message.results.GroupMemberList;
import love.forte.simbot.api.sender.MsgSender;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SetuListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetuListener.class);
    private static final String TEMP = "./temp/SETU/";
    private static final String ARTWORK_PREFIX = "https://www.pixiv.net/artworks/";
    private static final String ARTIST_PREFIX = "https://www.pixiv.net/users/";
    private static final String AVATAR_API = "http://thirdqq.qlogo.cn/g?b=qq&nk=";
    private static final String AWSL = "https://setu.awsl.ee/api/setu!";
    private static final String MJX = "https://api.66mz8.com/api/rand.tbimg.php?format=pic";
    private static final String MEOW = "http://aws.random.cat/meow";
    private static final String UA = "User-Agent";
    private static final String UA_STRING = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3";
    private static final int CD = 20;
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
    }

    static {
        File setuFolder = new File(TEMP);
        if (!setuFolder.exists()) {
            try {
                FileUtils.forceMkdir(setuFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Autowired
    private AccountService accountService;

    @Value("${setu.price}")
    private double price;

    // @Limit(CD)
    @OnGroup
    @Filter("叫[车車](.*)(.*)?(|r18)")
    public void driver1(GroupMsg msg, MsgSender sender) {
        String qq = msg.getAccountInfo().getAccountCode();
        Account account = accountService.getById(qq);
        if (account == null) {
            createScore(qq);
            //sender.SENDER.sendGroupMsg(msg, CQ_AT + qq + "]" + "你没钱了，请尝试签到或找开发者PY");
        } else {
            int i = RandomUtils.nextInt(1, 100);
            if (i <= 10) {
                sender.SENDER.sendGroupMsg(msg, "累了，不想发车。");
            } else {
                sendPic(msg, sender, account);
            }
        }
    }

    @OnGroup
    @Filter("来(.*?)[点丶份张張](.*?)的?(|r18)[色瑟涩][图圖]")
    public void driver2(GroupMsg msg, MsgSender sender) {
        String qq = msg.getAccountInfo().getAccountCode();
        Account account = accountService.getById(qq);
        if (account == null) {
            createScore(qq);
            //sender.SENDER.sendGroupMsg(msg, CQ_AT + qq + "]" + "你没钱了，请尝试签到或找开发者PY");
        } else {
            int i = RandomUtils.nextInt(1, 100);
            if (i <= 10) {
                sender.SENDER.sendGroupMsg(msg, "累了，不想发车。");
            } else {
                sendPic(msg, sender, account);
            }
        }
    }

    private void sendPic(GroupMsg msg, MsgSender sender, Account account) {
        String groupCode = msg.getGroupInfo().getGroupCode();
        String accountCode = msg.getAccountInfo().getAccountCode();
        String message = msg.getMsg();
        String regex = message.startsWith("叫") ? "叫[车車](.*)(.*)?(|r18)" : "来(.*?)[点點丶份张張](.*?)的?(|r18)[色瑟涩澀][图圖]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(message);
        int num = 1;
        String tag = "";
        boolean r18 = false;
        String number;
        while (m.find()) {
            // 兼容原有的叫车功能
            if (message.startsWith("叫车")) {
                number = m.group(2).trim();
                tag = m.group(1).trim();
            } else {
                number = m.group(1).trim();
                tag = m.group(2).trim();
            }
            try {
                num = NUMBER.get(number) == null ? Integer.parseInt(number) : NUMBER.get(number);
            } catch (NumberFormatException ignore) {
                LOGGER.info("number set to 1");
            }
            r18 = !StringUtils.isEmpty(m.group(3).trim());
        }
        if (account.getCoin() >= price * num) {
            // 发图
            GroupMemberList groupMemberList = sender.GETTER.getGroupMemberList(msg);
            for (GroupMemberInfo member : groupMemberList) {
                String remarkOrNickname = member.getAccountRemarkOrNickname();
                if (Objects.equals(tag, remarkOrNickname)) {
                    LOGGER.info("群名片：{}\ttag: {}", remarkOrNickname, tag);
                    groupMember(msg, sender, member.getAccountCode());
                    return;
                }
            }
            SendSetu sendSetu = new SendSetu(groupCode, accountCode, sender, tag, num, r18, account, accountService);
            sendSetu.start();
        } else {
            // sender.SENDER.sendGroupMsg(msg, CQ_AT + msg + "]" + "你没钱了，请尝试签到或找开发者PY");
        }
    }

    @OnGroup
    @Filter(value = "喵(.*?)")
    public void meow(GroupMsg msg, MsgSender sender) throws IOException {
        ResponseHandler<String> myHandler = response -> EntityUtils.toString(response.getEntity(), Consts.UTF_8);
        String response = Request.Get(MEOW).addHeader(UA, UA_STRING).execute().handleResponse(myHandler);
        JSONObject jsonObject = JSON.parseObject(response);
        String fileUrl = jsonObject.getString("file");
        String fileSuffix = fileUrl.substring(fileUrl.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        File file = new File(TEMP + uuid + fileSuffix);
        if (!Objects.equals(fileSuffix, ".gif")) {
            Thumbnails.of(new URL(fileUrl.replace("https", "http"))).scale(1).outputQuality(0.25).toFile(file);
        } else {
            FileUtils.copyURLToFile(new URL(fileUrl.replace("https", "http")), file);
        }
//        String image = kqCodeUtils.toCq(Constants.cqType.IMAGE, Constants.cqPrefix.FILE + file.getAbsolutePath());
//        sender.SENDER.sendGroupMsg(msg, image);
    }

    @OnGroup
    @Filter(value = "#抽奖")
    public void luck(GroupMsg msg, MsgSender sender) throws IOException {
        HttpResponse httpResponse = Request.Get(AWSL).addHeader(UA, UA_STRING).execute().returnResponse();
        InputStream content = httpResponse.getEntity().getContent();
        String uuid = UUID.randomUUID().toString();
        Path path = Paths.get(TEMP + uuid + ".jpg");
        Files.copy(content, path);
//        String image = kqCodeUtils.toCq(Constants.cqType.IMAGE, Constants.cqPrefix.FILE + path.toAbsolutePath());
//        sender.SENDER.sendGroupMsg(msg, image);
    }

    @Limit(CD * 6)
    @OnGroup
    @Filter(value = "#mjx")
    public void mjx(GroupMsg msg, MsgSender sender) throws IOException {
        InputStream content = Request.Get(MJX)
                .setHeader(UA, UA_STRING)
                .execute().returnResponse().getEntity().getContent();
        String uuid = UUID.randomUUID().toString();
        Path path = Paths.get(TEMP + uuid + ".jpg");
        Files.copy(content, path);
//        String image = kqCodeUtils.toCq(Constants.cqType.IMAGE, Constants.cqPrefix.FILE + path.toAbsolutePath());
//        sender.SENDER.sendGroupMsg(msg, image);
    }

    private void createScore(String accountCode) {
        Account account = new Account();
        account.setSignFlag(false);
        account.setAccount(accountCode);
        account.setCoin(0L);
        accountService.save(account);
    }

    private void groupMember(GroupMsg msg, MsgSender sender, String qq) {
        String api = AVATAR_API + qq + "&s=640";
        try {
            InputStream imageStream = Request.Get(api).execute().returnResponse().getEntity().getContent();
            File pic = new File(TEMP + qq + System.currentTimeMillis() + ".jpg");
            FileUtils.copyInputStreamToFile(imageStream, pic);
            // String image = kqCodeUtils.toCq(Constants.cqType.IMAGE, Constants.cqPrefix.FILE + pic.getAbsolutePath());
            LOGGER.info(pic.getAbsolutePath());
            // sender.SENDER.sendGroupMsg(msg, image);
            FileUtils.deleteQuietly(pic);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class SendSetu extends Thread {
        private final String groupCode;
        private final String privateQQ;
        private final MsgSender sender;
        private final String tag;
        private final Integer num;
        private final Boolean r18;
        private final Account account;
        private final AccountService thisAccountService;

        public SendSetu(String groupCode, String privateQQ, MsgSender sender, String tag, Integer num,
                        Boolean r18, Account account, AccountService thisAccountService) {
            this.groupCode = groupCode;
            this.privateQQ = privateQQ;
            this.sender = sender;
            this.tag = tag;
            this.num = num;
            this.r18 = r18;
            this.account = account;
            this.thisAccountService = thisAccountService;
        }

        @Override
        public void run() {
            int sendCount = 0; // 记录实际发送的图片张数
            try {
                List<Pixiv> setu = SetuUtils.getSetu(tag, num, r18);
                Pixiv pixiv = setu.get(0);
                String code = pixiv.getCode();
                boolean fromLolicon = "0".equals(code);
                if ("200".equals(code) || fromLolicon || Objects.isNull(code)) {
                    for (Pixiv p : setu) {
                        String filename = p.getFileName();
                        String imageUrl = p.getOriginal();
                        File compressedJPG = new File(TEMP + filename.replace("png", "jpg"));
                        if (!compressedJPG.exists() || System.currentTimeMillis() - compressedJPG.lastModified() > 60 * 60 * 1000) {
                            // 图片1小时内没发过才会发
                            sendPic(fromLolicon, p, imageUrl, compressedJPG);
                            sendCount++;
                        } else {
                            if (!p.isR18()) {
                                LOGGER.info("------- 图片名称：{}", filename);
                                sender.SENDER.sendGroupMsg(groupCode, "含有 " + tag + " 的车已经发完了");
                            }
                            return;
                        }
                    }
                    account.setCoin((long) (account.getCoin() - price * sendCount));
                    thisAccountService.updateById(account); // 按照实际发送的张数来扣除叫车者的币
                } else {
                    if (StringUtils.isEmpty(groupCode)) {
                        sender.SENDER.sendPrivateMsg(privateQQ, "冇");
                    } else {
                        sender.SENDER.sendGroupMsg(groupCode, "冇");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                sender.SENDER.sendGroupMsg(groupCode, "炸了");
            }
        }

        private void sendPic(boolean fromLolicon, Pixiv p, String imageUrl, File compressedJPG) throws IOException {
            Thumbnails.of(new URL(imageUrl)).scale(1).outputQuality(0.25).toFile(compressedJPG);
            // 发送图片
            String image = "";// kqCodeUtils.toCq(Constants.cqType.IMAGE, Constants.cqPrefix.FILE + compressedJPG.getAbsolutePath());
            String message = image + "\n" +
                    p.getTitle() + "\n" +
                    ARTWORK_PREFIX + p.getArtwork() + "\n" +
                    p.getAuthor() + "\n" +
                    ARTIST_PREFIX + p.getArtist() + "\n";
            // + "tags:" + Arrays.toString(p.getTags());
            if (fromLolicon) {
                message += "\n" + "今日剩余额度：" + p.getQuota();
            }
            if (StringUtils.isEmpty(groupCode)) { // 不是群消息，则直接私聊
                sender.SENDER.sendPrivateMsg(privateQQ, message);
            } else {
                if (!p.isR18()) { // 非R18且叫车的是群消息
                    sender.SENDER.sendGroupMsg(groupCode, message);
                } else {  // R18则发送私聊
                    sender.SENDER.sendPrivateMsg(privateQQ, message);
                }
            }
        }
    }
}
