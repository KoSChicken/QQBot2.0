package io.koschicken.task;

import catcode.CatCodeUtil;
import io.koschicken.bean.GroupPower;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.api.sender.BotSender;
import love.forte.simbot.bot.BotManager;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import static io.koschicken.constants.Constants.COMMON_CONFIG;
import static io.koschicken.intercept.BotIntercept.GROUP_CONFIG_MAP;

@Slf4j
@Component
public class HBTask {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    private BotManager botManager;

    /**
     * 每小时发送一条消息
     */
    @Scheduled(cron = "0 30 0,1,2,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23 ? * *")
    public void heartBeat() {
        log.info("bot still running {}", dateFormat.format(new Date()));
        boolean b = RandomUtils.nextBoolean();
        sendPic("./resource/image/hb." + (b ? "gif" : "jpg"));
    }

    /**
     * 每10分钟发送一条消息给Bot管理员
     */
    //@Scheduled(fixedRate = 1000 * 60 * 10)
    public void heartBeat2Master() {
        log.info("bot still running {}", dateFormat.format(new Date()));
        BotSender sender = botManager.getDefaultBot().getSender();
        sender.SENDER.sendPrivateMsg(COMMON_CONFIG.getMasterQQ(), "1");
    }

    private void sendPic(String picPath) {
        File image = new File(picPath);
        if (image.exists()) {
            CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
            String cat = catCodeUtil.getStringTemplate().image(image.getAbsolutePath());
            BotSender sender = botManager.getDefaultBot().getSender();
            Set<String> groups = GROUP_CONFIG_MAP.keySet();
            for (String groupCode : groups) {
                GroupPower groupPower = GROUP_CONFIG_MAP.get(groupCode);
                if (groupPower.isGlobalSwitch() && groupPower.isMaiyaoSwitch()) {
                    sender.SENDER.sendGroupMsg(groupCode, cat);
                }
            }
        } else {
            log.error("图片{}不存在", picPath);
        }
    }
}
