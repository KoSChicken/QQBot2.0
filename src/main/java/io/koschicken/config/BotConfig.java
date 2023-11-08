package io.koschicken.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BotConfig {

    private static BotConfig instance;
    // 总开关
    private boolean globalSwitch;
    // 骰子开关
    private boolean diceSwitch;
    // setu开关
    private boolean setuSwitch;
    // 主人qq
    private String masterQQ;
    // r18图片的私聊开关
    private boolean r18Private;
    // SauceNaoApiKey
    private String sauceNaoApiKey;
    // setu黑名单
    private String setuBlackTags;

    private BotConfig() {
    }

    public static synchronized BotConfig getInstance() {
        if (instance == null) {
            instance = new BotConfig();
        }
        return instance;
    }
}
