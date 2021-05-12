package io.koschicken.task;

import catcode.CatCodeUtil;
import io.koschicken.bean.GroupPower;
import love.forte.simbot.api.sender.BotSender;
import love.forte.simbot.bot.BotManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import static io.koschicken.intercept.BotIntercept.GROUP_CONFIG_MAP;

@Component
public class FunnyTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(PCRTask.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    private BotManager botManager;

    /**
     * 做那么多，钱到哪里？
     * 差不多七点了，饮茶
     */
    @Scheduled(cron = "0 50 18 * * ?")
    public void drinkTask() {
        LOGGER.info("drink time {}", dateFormat.format(new Date()));
        sendPic("./resource/image/drink.jpg");
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
            LOGGER.error("图片{}不存在", picPath);
        }
    }
}
