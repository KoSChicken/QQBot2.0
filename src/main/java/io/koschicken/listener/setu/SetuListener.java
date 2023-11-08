package io.koschicken.listener.setu;

import catcode.Neko;
import io.koschicken.config.BotConfig;
import io.koschicken.intercept.limit.Limit;
import io.koschicken.utils.FingerPrint;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.annotation.OnPrivate;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.MsgGet;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilderFactory;
import love.forte.simbot.filter.MatchType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
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

@Slf4j
@Service
public class SetuListener {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private static final long CD = 60;
    private static final String TEMP_DIR = "./temp/";

    private final MiraiMessageContentBuilderFactory factory;

    public SetuListener(MiraiMessageContentBuilderFactory factory) {
        this.factory = factory;
    }

    @Limit(CD)
    @OnGroup
    @OnPrivate
    @Filter(value = "^叫[车車](.*)(.*)?(|r18)$", matchType = MatchType.REGEX_MATCHES)
    public void driver1(MsgGet msg, MsgSender sender) {
        String qq = msg.getAccountInfo().getAccountCode();
        int i = RandomUtils.nextInt(1, 100);
        if (i <= 10 && !Objects.equals(qq, BotConfig.getInstance().getMasterQQ())) {
            if (msg instanceof GroupMsg groupMsg) {
                sender.SENDER.sendGroupMsg(groupMsg, "歇会");
            } else {
                sender.SENDER.sendPrivateMsg(msg, "歇会");
            }
        } else {
            executorService.submit(new SetuRunner(msg, factory, sender));
        }
    }

    @Limit(CD * 2)
    @OnGroup
    @OnPrivate
    @Filter(value = "^[来來](.*?)[点點丶份张張](.*?)的?(|r18)[色瑟涩][图圖]$", matchType = MatchType.REGEX_MATCHES)
    public void driver2(MsgGet msg, MsgSender sender) {
        String qq = msg.getAccountInfo().getAccountCode();
        int i = RandomUtils.nextInt(1, 100);
        if (i <= 50 && !Objects.equals(qq, BotConfig.getInstance().getMasterQQ())) {
            if (msg instanceof GroupMsg groupMsg) {
                sender.SENDER.sendGroupMsg(groupMsg, "来nm");
            } else {
                sender.SENDER.sendPrivateMsg(msg, "\uD83D\uDD95");
            }
        } else {
            executorService.submit(new SetuRunner(msg, factory, sender));
        }
    }

    @OnGroup
    @OnPrivate
    public void callPic(MsgGet msg, MsgSender sender) {
        File call = new File("./resource/image/call.jpg");
        List<Neko> cats;
        if (msg instanceof GroupMsg groupMsg) {
            cats = groupMsg.getMsgContent().getCats();
        } else {
            cats = ((PrivateMsg) msg).getMsgContent().getCats();
        }

        List<Neko> imageNekoList = cats.stream().filter(cat -> "image".equals(cat.getType())).toList();
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
                        executorService.submit(new SetuRunner(msg, factory, sender));
                    }
                    FileUtils.delete(file);
                }
            } catch (IOException e) {
                log.error("图片获取失败：", e);
            }
        });
    }
}
