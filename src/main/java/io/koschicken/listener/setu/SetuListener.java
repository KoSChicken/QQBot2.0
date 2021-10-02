package io.koschicken.listener.setu;

import catcode.Neko;
import io.koschicken.intercept.limit.Limit;
import io.koschicken.utils.FingerPrint;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilderFactory;
import love.forte.simbot.filter.MatchType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static io.koschicken.constants.Constants.COMMON_CONFIG;

@Slf4j
@Service
public class SetuListener {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private static final long CD = 15;
    private static final String TEMP_DIR = "./temp/";

    @Autowired
    private MiraiMessageContentBuilderFactory factory;

    @Limit(CD)
    @OnGroup
    @Filter(value = "^叫[车車](.*)(.*)?(|r18)$", matchType = MatchType.REGEX_MATCHES)
    public void driver1(GroupMsg msg, MsgSender sender) {
        String qq = msg.getAccountInfo().getAccountCode();
        int i = RandomUtils.nextInt(1, 100);
        if (i <= 10 && !Objects.equals(qq, COMMON_CONFIG.getMasterQQ())) {
            sender.SENDER.sendGroupMsg(msg, "累了，歇会");
        } else {
            executorService.submit(new SetuRunner(msg, factory, sender));
        }
    }

    @Limit(CD * 2)
    @OnGroup
    @Filter(value = "^[来來](.*?)[点點丶份张張](.*?)的?(|r18)[色瑟涩][图圖]$", matchType = MatchType.REGEX_MATCHES)
    public void driver2(GroupMsg msg, MsgSender sender) {
        String qq = msg.getAccountInfo().getAccountCode();
        int i = RandomUtils.nextInt(1, 100);
        if (i <= 10 && !Objects.equals(qq, COMMON_CONFIG.getMasterQQ())) {
            sender.SENDER.sendGroupMsg(msg, "累了，歇会");
        } else {
            executorService.submit(new SetuRunner(msg, factory, sender));
        }
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
                        executorService.submit(new SetuRunner(groupMsg, factory, sender));
                    }
                    FileUtils.delete(file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
