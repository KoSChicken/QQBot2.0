package io.koschicken.bean;

import lombok.Data;

@Data
public class CommonConfig {
    // 提醒买药小助手文件名
    private String maiyaoPic;
    // 抽卡上限
    private int gachaLimit;
    //抽卡冷却时间，以秒为单位
    private int gachaCooldown;
    //总开关
    private boolean globalSwitch;
    //扭蛋开关
    private boolean gachaSwitch;
    //买药小助手提示
    private boolean maiyaoSwitch;
    //赛马开关
    private boolean horseSwitch;
    //骰子开关
    private boolean diceSwitch;
    //setu开关
    private boolean setuSwitch;
    //彩票开关
    private boolean lotterySwitch;
    //主人qq
    private String masterQQ;
    //签到一次给的钱
    private int signCoin;
    //发一次图给的钱
    private int setuCoin;
    //r18图片的私聊开关
    private boolean r18Private;
    //LoliconApiKey
    private String loliconApiKey;
    //SauceNaoApiKey
    private String sauceNaoApiKey;
    //B站cookie
    private String bilibiliCookie;
    //setu黑名单
    private String setuBlackTags;
}
