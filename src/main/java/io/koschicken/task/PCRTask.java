package io.koschicken.task;

import catcode.CatCodeUtil;
import io.koschicken.bean.GroupPower;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.BotSender;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.bot.BotManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

import static io.koschicken.constants.Constants.COMMON_CONFIG;
import static io.koschicken.intercept.BotIntercept.GROUP_CONFIG_MAP;

@Component
public class PCRTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(PCRTask.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    private BotManager botManager;

    /**
     * PCR竞技场背刺提醒
     * 每天下午2点40
     */
    // @Scheduled(cron = "0 40 14 * * ?")
    public void JJCTask() {
        LOGGER.info("JJC time {}", dateFormat.format(new Date()));
        sendPic("./resource/image/stab.jpg");
    }

    /**
     * PCR商店刷新提醒
     * 每天的0, 6, 12, 18点提醒
     */
    // @Scheduled(cron = "0 0 0/6 * * ? ")
    public void shop() {
        LOGGER.info("shop refresh time {}", dateFormat.format(new Date()));
        sendPic("./resource/image/" + COMMON_CONFIG.getMaiyaoPic());
    }

    @OnGroup
    @Filter(".tt")
    public void greetings(GroupMsg groupMsg, Sender sender) {
        if (Objects.equals(COMMON_CONFIG.getMasterQQ(), groupMsg.getAccountInfo().getAccountCode())) {
            sendPic("./resource/image/" + COMMON_CONFIG.getMaiyaoPic());
            sendPic("./resource/image/stab.jpg");
        }
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
