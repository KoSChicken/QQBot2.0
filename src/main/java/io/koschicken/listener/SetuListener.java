package io.koschicken.listener;

import catcode.CatCodeUtil;
import catcode.Neko;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.koschicken.bean.setu.LoliconResponse;
import io.koschicken.bean.setu.Pixiv;
import io.koschicken.db.bean.Account;
import io.koschicken.db.service.AccountService;
import io.koschicken.intercept.limit.Limit;
import io.koschicken.utils.FingerPrint;
import io.koschicken.utils.SetuUtils;
import lombok.extern.slf4j.Slf4j;
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
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.koschicken.constants.Constants.COMMON_CONFIG;

@Slf4j
@Service
public class SetuListener {
    private static final String TEMP_DIR = "./temp/";
    private static final String SETU_DIR = "./temp/SETU/";
    private static final String SETU_COMP_DIR = "./temp/SETU/comp/";
    private static final String MEOW_DIR = "./temp/MEOW/";
    private static final String ARTWORK_PREFIX = "https://www.pixiv.net/artworks/";
    private static final String ARTIST_PREFIX = "https://www.pixiv.net/users/";
    private static final String AVATAR_API = "http://thirdqq.qlogo.cn/g?b=qq&nk=";
    private static final String MEOW = "http://aws.random.cat/meow";
    private static final String UA = "User-Agent";
    private static final String UA_STRING = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3";
    private static final HashMap<String, Integer> NUMBER;
    private final static long CD = 15;

    static {
        NUMBER = new HashMap<>();
        NUMBER.put("???", 1);
        NUMBER.put("???", 2);
        NUMBER.put("???", 2);
        NUMBER.put("???", 2);
        NUMBER.put("???", 3);
        NUMBER.put("???", 4);
        NUMBER.put("???", 5);
        NUMBER.put("???", 6);
        NUMBER.put("???", 7);
        NUMBER.put("???", 8);
        NUMBER.put("???", 9);
        NUMBER.put("???", 10);
        NUMBER.put("???", RandomUtils.nextInt(1, 4));
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
        File setuCompFolder = new File(SETU_COMP_DIR);
        if (!setuCompFolder.exists()) {
            try {
                FileUtils.forceMkdir(setuCompFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File meowFolder = new File(MEOW_DIR);
        if (!meowFolder.exists()) {
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

    @Limit(CD)
    @OnGroup
    @Filter(value = "^???[??????](.*)(.*)?(|r18)$", matchType = MatchType.REGEX_MATCHES)
    public void driver1(GroupMsg msg, MsgSender sender) {
        String qq = msg.getAccountInfo().getAccountCode();
        Account account = accountService.getById(qq);
        if (account == null) {
            account = createScore(qq);
        }
        int i = RandomUtils.nextInt(1, 100);
        if (i <= 10 && !Objects.equals(qq, COMMON_CONFIG.getMasterQQ())) {
            sender.SENDER.sendGroupMsg(msg, "???????????????");
        } else {
            sendPic(msg, sender, account);
        }
    }

    @Limit(CD)
    @OnGroup
    @Filter(value = "^[??????](.*?)[??????????????????](.*?)????(|r18)[?????????][??????]$", matchType = MatchType.REGEX_MATCHES)
    public void driver2(GroupMsg msg, MsgSender sender) {
        String qq = msg.getAccountInfo().getAccountCode();
        Account account = accountService.getById(qq);
        if (account == null) {
            account = createScore(qq);
        }
        int i = RandomUtils.nextInt(1, 100);
        if (i <= 10) {
            sender.SENDER.sendGroupMsg(msg, "???????????????");
        } else {
            sendPic(msg, sender, account);
        }
    }

    private void sendPic(GroupMsg msg, MsgSender sender, Account account) {
        String groupCode = msg.getGroupInfo().getGroupCode();
        String accountCode = msg.getAccountInfo().getAccountCode();
        String message = msg.getMsg();
        String regex = message.startsWith("???") ? "^???[??????](.*)(.*)?(|r18)$" :
                "^[??????](.*?)[??????????????????](.*?)????(|r18)[????????????][??????]$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(message);
        int num = 1;
        String tag = "";
        boolean r18 = false;
        String number;
        while (m.find()) {
            // ???????????????????????????
            if (message.startsWith("???")) {
                number = m.group(2).trim();
                tag = m.group(1).trim();
            } else {
                number = m.group(1).trim();
                tag = m.group(2).trim();
            }
            try {
                num = NUMBER.get(number) == null ? Integer.parseInt(number) : NUMBER.get(number);
            } catch (NumberFormatException ignore) {
                log.info("number set to 1");
            }
            r18 = !StringUtils.isEmpty(m.group(3).trim());
        }
        if (account.getCoin() >= price * num) {
            GroupMemberList groupMemberList = sender.GETTER.getGroupMemberList(msg);
            for (GroupMemberInfo member : groupMemberList) {
                String remarkOrNickname = member.getAccountRemarkOrNickname();
                if (Objects.nonNull(remarkOrNickname) && Objects.equals(tag, remarkOrNickname.trim())) {
                    log.info("????????????{}\ttag: {}", remarkOrNickname, tag);
                    groupMember(msg, sender, member.getAccountCode());
                    return;
                }
            }
            SendSetu sendSetu = new SendSetu(groupCode, accountCode, sender, tag, num, r18, account, accountService);
            sendSetu.start();
        } else {
            CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
            String at = catCodeUtil.getStringTemplate().at(accountCode);
            sender.SENDER.sendGroupMsg(msg, at + "?????????????????????????????????????????????PY");
        }
    }

    @OnGroup
    @Filter(value = "???(.*?)", matchType = MatchType.REGEX_MATCHES)
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

    @OnGroup
    public void callPic(GroupMsg groupMsg, MsgSender sender) {
        File call = new File("./resource/image/call.jpg");
        List<Neko> cats = groupMsg.getMsgContent().getCats();
        List<Neko> imageNekoList = cats.stream().filter(cat -> "image".equals(cat.getType())).collect(Collectors.toList());
        imageNekoList.forEach(neko -> {
            String url = neko.get("url");
            try {
                if (Objects.nonNull(url)) {
                    File file = new File(TEMP_DIR + UUID.randomUUID());
                    FileUtils.copyURLToFile(new URL(url), file);
                    FingerPrint fp1 = new FingerPrint(ImageIO.read(file));
                    FingerPrint fp2 = new FingerPrint(ImageIO.read(call));
                    float compare = fp1.compare(fp2);
                    log.info("call pic compare, similar rate: {}", compare);
                    if (compare >= 0.95) {
                        String groupCode = groupMsg.getGroupInfo().getGroupCode();
                        String accountCode = groupMsg.getAccountInfo().getAccountCode();
                        SendSetu sendSetu = new SendSetu(groupCode, accountCode, sender, "",
                                1, false, null, accountService);
                        sendSetu.start();
                    }
                    FileUtils.delete(file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
            if (!tagCheck(tag)) {
                LoliconResponse loliconResponse;
                try {
                    loliconResponse = SetuUtils.getSetu(tag, num, r18);
                } catch (IOException e) {
                    e.printStackTrace();
                    sender.SENDER.sendGroupMsg(groupCode, "??????");
                    return;
                }
                if (StringUtils.isEmpty(loliconResponse.getError())) {
                    int sendCount = 0; // ?????????????????????????????????
                    List<Pixiv> data = loliconResponse.getData();
                    if (!CollectionUtils.isEmpty(data)) {
                        for (Pixiv p : data) {
                            String pid = p.getPid().toString();
                            try {
                                File originalFile = new File(SETU_DIR + pid + "." + p.getExt());
                                if (!originalFile.exists()) {
                                    FileUtils.copyURLToFile(new URL(p.getUrls().get("original")), originalFile);
                                }
                                log.info("?????????????????????pid={}, file={}", pid, originalFile.getName());
                                File compressedJPG = new File(SETU_COMP_DIR + pid + ".jpg");
                                if (!compressedJPG.exists() || System.currentTimeMillis() - compressedJPG.lastModified() > 60 * 60 * 1000) {
                                    // ??????1???????????????????????????
                                    sendPic(p, originalFile, compressedJPG);
                                    sendCount++;
                                } else {
                                    if (!p.isR18()) {
                                        log.info("------- ???????????????{}", pid + "." + p.getExt());
                                        sender.SENDER.sendGroupMsg(groupCode, "?????? " + tag + " ?????????????????????");
                                    }
                                    return;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                sender.SENDER.sendGroupMsg(groupCode, "??????");
                            }
                        }
                        if (Objects.nonNull(account)) {
                            account.setCoin((long) (account.getCoin() - price * sendCount));
                            thisAccountService.updateById(account); // ???????????????????????????????????????????????????
                        }
                    } else {
                        notFoundResponse("");
                    }
                } else {
                    sender.SENDER.sendGroupMsg(groupCode, loliconResponse.getError());
                }
            } else {
                String first = tag.trim().substring(0, 1);
                notFoundResponse(first + "nmlgb");
            }
        }

        private void notFoundResponse(String responseMsg) {
            String msg;
            if (StringUtils.isEmpty(responseMsg)) {
                CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
                File image = new File("./resource/image/mao.jpg");
                if (image.exists()) {
                    msg = catCodeUtil.getStringTemplate().image(image.getAbsolutePath());
                } else {
                    msg = "???";
                }
            } else {
                msg = responseMsg;
            }
            if (StringUtils.isEmpty(groupCode)) {
                sender.SENDER.sendPrivateMsg(privateQQ, msg);
            } else {
                sender.SENDER.sendGroupMsg(groupCode, msg);
            }
        }

        private boolean tagCheck(String tag) {
            String tags = COMMON_CONFIG.getSetuBlackTags();
            if (tags == null) {
                return false;
            }
            List<String> tagList = Arrays.asList(tags.split(","));
            int i = RandomUtils.nextInt(1, 100);
            log.info(i + " - " + tags);
            return i <= 50 && tagList.contains(tag);
        }

        private void sendPic(Pixiv p, File originalFile, File compressedJPG) {
            try {
                Thumbnails.of(originalFile).scale(1).outputQuality(1).toFile(compressedJPG);
            } catch (IOException e) {
                e.printStackTrace();
                if (!p.isR18()) { // ???R18????????????????????????
                    sender.SENDER.sendGroupMsg(groupCode, "??????");
                } else {  // R18???????????????
                    sender.SENDER.sendPrivateMsg(privateQQ, "??????");
                }
                return;
            }
            // ????????????
            CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
            String image = catCodeUtil.getStringTemplate().image(compressedJPG.getAbsolutePath());
            String message = image + "\n" +
                    p.getTitle() + "\n" +
                    ARTWORK_PREFIX + p.getPid() + "\n" +
                    p.getAuthor() + "\n" +
                    ARTIST_PREFIX + p.getUid() + "\n";
            // + "tags:" + Arrays.toString(p.getTags());
            if (StringUtils.isEmpty(groupCode)) { // ?????????????????????????????????
                sender.SENDER.sendPrivateMsg(privateQQ, message);
            } else {
                if (!p.isR18()) { // ???R18????????????????????????
                    sender.SENDER.sendGroupMsg(groupCode, message);
                } else {  // R18???????????????
                    sender.SENDER.sendPrivateMsg(privateQQ, message);
                }
            }
        }
    }
}
