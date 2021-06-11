package io.koschicken.listener;

import catcode.CatCodeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.koschicken.bean.Pixiv;
import io.koschicken.db.bean.Account;
import io.koschicken.db.service.AccountService;
import io.koschicken.intercept.limit.Limit;
import io.koschicken.utils.SetuUtils;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.results.GroupMemberInfo;
import love.forte.simbot.api.message.results.GroupMemberList;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.filter.MatchType;
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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.koschicken.constants.Constants.COMMON_CONFIG;

@Service
public class SetuListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetuListener.class);
    private static final String SETU_DIR = "./temp/SETU/";
    private static final String MJX_DIR = "./temp/MJX/";
    private static final String MEOW_DIR = "./temp/MEOW/";
    private static final String ARTWORK_PREFIX = "https://www.pixiv.net/artworks/";
    private static final String ARTIST_PREFIX = "https://www.pixiv.net/users/";
    private static final String AVATAR_API = "http://thirdqq.qlogo.cn/g?b=qq&nk=";
    private static final String AWSL = "https://setu.awsl.ee/api/setu!";
    private static final String MJX = "https://api.sumt.cn/api/rand.tbimg.php?format=jpg";
    private static final String MEOW = "http://aws.random.cat/meow";
    private static final String UA = "User-Agent";
    private static final String UA_STRING = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3";
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
        File setuFolder = new File(SETU_DIR);
        if (!setuFolder.exists()) {
            try {
                FileUtils.forceMkdir(setuFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File mjxFolder = new File(MJX_DIR);
        if (!setuFolder.exists()) {
            try {
                FileUtils.forceMkdir(mjxFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File meowFolder = new File(MEOW_DIR);
        if (!setuFolder.exists()) {
            try {
                FileUtils.forceMkdir(meowFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Autowired
    private AccountService accountService;

    @Value("${setu.price}")
    private double price;

    private final static long CD = 30;

    @Limit(CD)
    @OnGroup
    @Filter(value = "^叫[车車](.*)(.*)?(|r18)$", matchType = MatchType.REGEX_MATCHES)
    public void driver1(GroupMsg msg, MsgSender sender) {
        String qq = msg.getAccountInfo().getAccountCode();
        Account account = accountService.getById(qq);
        if (account == null) {
            account = createScore(qq);
        }
        int i = RandomUtils.nextInt(1, 100);
        if (i <= 10) {
            sender.SENDER.sendGroupMsg(msg, "累了，不想发车。");
        } else {
            sendPic(msg, sender, account);
        }
    }

    @Limit(CD)
    @OnGroup
    @Filter(value = "^[来來](.*?)[点點丶份张張](.*?)的?(|r18)[色瑟涩][图圖]$", matchType = MatchType.REGEX_MATCHES)
    public void driver2(GroupMsg msg, MsgSender sender) {
        String qq = msg.getAccountInfo().getAccountCode();
        Account account = accountService.getById(qq);
        if (account == null) {
            account = createScore(qq);
        }
        int i = RandomUtils.nextInt(1, 100);
        if (i <= 10) {
            sender.SENDER.sendGroupMsg(msg, "累了，不想发车。");
        } else {
            sendPic(msg, sender, account);
        }
    }

    private void sendPic(GroupMsg msg, MsgSender sender, Account account) {
        String groupCode = msg.getGroupInfo().getGroupCode();
        String accountCode = msg.getAccountInfo().getAccountCode();
        String message = msg.getMsg();
        String regex = message.startsWith("叫") ? "^叫[车車](.*)(.*)?(|r18)$" :
                "^[来來](.*?)[点點丶份张張](.*?)的?(|r18)[色瑟涩澀][图圖]$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(message);
        int num = 1;
        String tag = "";
        boolean r18 = false;
        String number;
        while (m.find()) {
            // 兼容原有的叫车功能
            if (message.startsWith("叫")) {
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
            CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
            String at = catCodeUtil.getStringTemplate().at(accountCode);
            sender.SENDER.sendGroupMsg(msg, at + "你没钱了，请尝试签到或找开发者PY");
        }
    }

    @OnGroup
    @Filter(value = "喵(.*?)", matchType = MatchType.REGEX_MATCHES)
    public void meow(GroupMsg msg, MsgSender sender) throws IOException {
        ResponseHandler<String> myHandler = response -> EntityUtils.toString(response.getEntity(), Consts.UTF_8);
        String response = Request.Get(MEOW).addHeader(UA, UA_STRING).execute().handleResponse(myHandler);
        JSONObject jsonObject = JSON.parseObject(response);
        String fileUrl = jsonObject.getString("file");
        String fileSuffix = fileUrl.substring(fileUrl.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        File file = new File(MEOW_DIR + uuid + fileSuffix);
        if (!Objects.equals(fileSuffix, ".gif")) {
            Thumbnails.of(new URL(fileUrl.replace("https", "http"))).scale(1)
                    .outputQuality(0.25).toFile(file);
        } else {
            FileUtils.copyURLToFile(new URL(fileUrl.replace("https", "http")), file);
        }
        CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
        String image = catCodeUtil.getStringTemplate().image(file.getAbsolutePath());
        sender.SENDER.sendGroupMsg(msg, image);
    }

    @Limit(CD)
    @OnGroup
    @Filter("#抽奖")
    public void luck(GroupMsg msg, MsgSender sender) throws IOException {
        HttpResponse httpResponse = Request.Get(AWSL).addHeader(UA, UA_STRING).execute().returnResponse();
        InputStream content = httpResponse.getEntity().getContent();
        String uuid = UUID.randomUUID().toString();
        Path path = Paths.get(SETU_DIR + uuid + ".jpg");
        Files.copy(content, path);
        CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
        String image = catCodeUtil.getStringTemplate().image(path.toFile().getAbsolutePath());
        sender.SENDER.sendGroupMsg(msg, image);
    }

    @Limit(CD * 4)
    @OnGroup
    @Filter("#mjx")
    public void mjx(GroupMsg msg, MsgSender sender) throws IOException {
        InputStream content = Request.Get(MJX)
                .setHeader(UA, UA_STRING)
                .execute().returnResponse().getEntity().getContent();
        String uuid = UUID.randomUUID().toString();
        Path path = Paths.get(MJX_DIR + uuid + ".jpg");
        Files.copy(content, path);
        CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
        String image = catCodeUtil.getStringTemplate().image(path.toFile().getAbsolutePath());
        sender.SENDER.sendGroupMsg(msg, image);
    }

    private Account createScore(String accountCode) {
        Account account = new Account();
        account.setSignFlag(false);
        account.setAccount(accountCode);
        account.setCoin(10000L);
        accountService.save(account);
        return account;
    }

    private void groupMember(GroupMsg msg, MsgSender sender, String qq) {
        String api = AVATAR_API + qq + "&s=640";
        try {
            InputStream imageStream = Request.Get(api).execute().returnResponse().getEntity().getContent();
            File pic = new File(SETU_DIR + qq + System.currentTimeMillis() + ".jpg");
            FileUtils.copyInputStreamToFile(imageStream, pic);
            CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
            String image = catCodeUtil.getStringTemplate().image(pic.getAbsolutePath());
            sender.SENDER.sendGroupMsg(msg, image);
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
                if (!tagCheck(tag)) {
                    List<Pixiv> setu = SetuUtils.getSetu(tag, num, r18);
                    if (CollectionUtils.isEmpty(setu)) {
                        notFoundResponse("冇");
                        return;
                    }
                    Pixiv pixiv = setu.get(0);
                    String code = pixiv.getCode();
                    boolean fromLolicon = "0".equals(code);
                    if ("200".equals(code) || fromLolicon || Objects.isNull(code)) {
                        for (Pixiv p : setu) {
                            String filename = p.getFileName();
                            String imageUrl = p.getOriginal();
                            File compressedJPG = new File(SETU_DIR + filename.replace("png", "jpg"));
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
                        notFoundResponse("冇");
                    }
                } else {
                    String first = tag.trim().substring(0, 1);
                    notFoundResponse(first + "nmlgb");
                }
            } catch (IOException e) {
                e.printStackTrace();
                sender.SENDER.sendGroupMsg(groupCode, "炸了");
            }
        }

        private void notFoundResponse(String responseMsg) {
            if (StringUtils.isEmpty(groupCode)) {
                sender.SENDER.sendPrivateMsg(privateQQ, responseMsg);
            } else {
                sender.SENDER.sendGroupMsg(groupCode, responseMsg);
            }
        }

        private boolean tagCheck(String tag) {
            String tags = COMMON_CONFIG.getSetuBlackTags();
            List<String> tagList = Arrays.asList(tags.split(","));
            int i = RandomUtils.nextInt(1, 100);
            LOGGER.info(i + " - " + tags);
            return i <= 50 && tagList.contains(tag);
        }

        private void sendPic(boolean fromLolicon, Pixiv p, String imageUrl, File compressedJPG) throws IOException {
            Thumbnails.of(new URL(imageUrl)).scale(1).outputQuality(1).toFile(compressedJPG);
            // 发送图片
            CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
            String image = catCodeUtil.getStringTemplate().image(compressedJPG.getAbsolutePath());
            String message = image + "\n" +
                    p.getTitle() + "\n" +
                    ARTWORK_PREFIX + p.getArtwork() + "\n" +
                    p.getAuthor() + "\n" +
                    ARTIST_PREFIX + p.getArtist() + "\n";
            // + "tags:" + Arrays.toString(p.getTags());
//            if (fromLolicon) {
//                message += "\n" + "今日剩余额度：" + p.getQuota();
//            }
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
