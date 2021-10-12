package io.koschicken;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.koschicken.bean.CommonConfig;
import io.koschicken.bean.GroupPower;
import io.koschicken.bean.HorseEvent;
import io.koschicken.constants.GameConstants;
import io.koschicken.utils.SafeProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static io.koschicken.constants.Constants.COMMON_CONFIG;
import static io.koschicken.constants.Constants.CONFIG_DIR;
import static io.koschicken.constants.PCRConstants.*;
import static io.koschicken.intercept.BotIntercept.GROUP_CONFIG_MAP;

@Slf4j
public class InitConfig {

    private InitConfig() {
    }

    //读取配置文件
    public static void initConfigs() {
        getFile();//群组配置文件
        getConfig();//通用设置
        getEvent();//马事件
        getGachaConfig();//扭蛋
    }

    //加载配置文件
    public static void getFile() {
        File file = new File(CONFIG_DIR + "/config.json");
        //群组设定
        if (!file.exists() || !file.isFile()) {
            //没有读取到配置文件
            try {
                FileUtils.touch(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                String jsonString = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                if (StringUtils.isNotEmpty(jsonString)) {
                    JSONObject jsonObject = JSON.parseObject(jsonString);
                    Set<String> keySet = jsonObject.keySet();
                    for (String s : keySet) {
                        String string = jsonObject.getJSONObject(s).toJSONString();
                        GroupPower keyValues = JSON.parseObject(string, GroupPower.class);
                        GROUP_CONFIG_MAP.put(s, keyValues);
                    }
                }
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    //加载通用配置
    public static void getConfig() {
        //通用设定
        File file = new File(CONFIG_DIR + "/通用配置.txt");
        if (!file.exists() || !file.isFile()) {
            try {
                freshConfig(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            COMMON_CONFIG = new CommonConfig();
            COMMON_CONFIG.setMaiyaoPic("shop.png");
            COMMON_CONFIG.setGachaLimit(300);
            COMMON_CONFIG.setGachaCooldown(10);
            COMMON_CONFIG.setGlobalSwitch(false);
            COMMON_CONFIG.setGachaSwitch(false);
            COMMON_CONFIG.setMaiyaoSwitch(false);
            COMMON_CONFIG.setHorseSwitch(false);
            COMMON_CONFIG.setDiceSwitch(false);
            COMMON_CONFIG.setSetuSwitch(false);
            COMMON_CONFIG.setLotterySwitch(false);
            COMMON_CONFIG.setSignCoin(5000);
            COMMON_CONFIG.setSetuCoin(500);
            COMMON_CONFIG.setR18Private(true);
            COMMON_CONFIG.setSetuBlackTags("");
        }
        try {
            COMMON_CONFIG = loadConfig(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void freshConfig(File file) throws IOException {
        OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        SafeProperties pro = new SafeProperties();
        pro.addComment("图片路径是resources/image");
        pro.setProperty("提醒买药小助手图片名", "shop.png");
        pro.setProperty("抽卡上限", "1000");
        pro.addComment("每次抽卡中间所需的冷却时间，单位为秒");
        pro.setProperty("抽卡冷却", "5");
        pro.addComment("以下为机器人开关的默认设置 true为开，false为关");
        pro.setProperty("总开关默认关闭", String.valueOf(Boolean.FALSE));
        pro.setProperty("抽卡默认关闭", String.valueOf(Boolean.FALSE));
        pro.setProperty("买药提醒默认关闭", String.valueOf(Boolean.FALSE));
        pro.setProperty("赛马默认关闭", String.valueOf(Boolean.FALSE));
        pro.setProperty("骰子默认关闭", String.valueOf(Boolean.FALSE));
        pro.setProperty("彩票默认关闭", String.valueOf(Boolean.FALSE));
        pro.setProperty("色图默认关闭", String.valueOf(Boolean.FALSE));
        pro.addComment("主人qq相当于在所有群里对这个机器人有管理员权限");
        pro.setProperty("主人qq", getMasterQQ());
        pro.addComment("签到增加币数目，设置为负数则有可能会越签越少");
        pro.setProperty("签到一次金币", "5000");
        pro.addComment("发一次色图所要花费的币数量，设置为负数可能会越花越多");
        pro.setProperty("发一次色图花费", "1000");
        pro.addComment("r18私聊开关");
        pro.setProperty("r18私聊", "true");
        pro.addComment("LoliconAPIKey");
        pro.setProperty("LOLICON_API_KEY", "");
        pro.addComment("B站Cookie");
        pro.setProperty("bilibiliCookie", "");
        pro.addComment("setu黑名单");
        pro.setProperty("setuBlackTags", "");
        pro.store(outputStream, "通用配置");
        outputStream.close();
    }

    private static String getMasterQQ() {
        SafeProperties pro = new SafeProperties();
        try (InputStreamReader in = new InputStreamReader(new FileInputStream("application.properties"), StandardCharsets.UTF_8)) {
            pro.load(in);
            return pro.get("masterqq").toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static CommonConfig loadConfig(File file) throws IOException {
        SafeProperties pro = new SafeProperties();
        InputStreamReader in;
        in = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        pro.load(in);
        CommonConfig commonConfig = new CommonConfig();
        commonConfig.setMaiyaoPic(pro.getProperty("提醒买药小助手图片名"));
        commonConfig.setGachaLimit(Integer.parseInt(pro.getProperty("抽卡上限")));
        commonConfig.setGachaCooldown(Integer.parseInt(pro.getProperty("抽卡冷却")));
        commonConfig.setGlobalSwitch(Boolean.parseBoolean(pro.getProperty("总开关默认关闭")));
        commonConfig.setGachaSwitch(Boolean.parseBoolean(pro.getProperty("抽卡默认关闭")));
        commonConfig.setMaiyaoSwitch(Boolean.parseBoolean(pro.getProperty("买药提醒默认关闭")));
        commonConfig.setHorseSwitch(Boolean.parseBoolean(pro.getProperty("赛马默认关闭")));
        commonConfig.setDiceSwitch(Boolean.parseBoolean(pro.getProperty("骰子默认关闭")));
        commonConfig.setLotterySwitch(Boolean.parseBoolean(pro.getProperty("彩票默认关闭")));
        commonConfig.setSetuSwitch(Boolean.parseBoolean(pro.getProperty("色图默认关闭")));
        commonConfig.setMasterQQ(pro.getProperty("主人qq"));
        commonConfig.setSignCoin(Integer.parseInt(pro.getProperty("签到一次金币")));
        commonConfig.setSetuCoin(Integer.parseInt(pro.getProperty("发一次色图花费")));
        commonConfig.setR18Private(Boolean.parseBoolean(pro.getProperty("r18私聊")));
        commonConfig.setLoliconApiKey(pro.getProperty("LOLICON_API_KEY"));
        commonConfig.setBilibiliCookie(pro.getProperty("bilibiliCookie"));
        commonConfig.setSetuBlackTags(pro.getProperty("setuBlackTags"));
        in.close();
        return commonConfig;
    }

    //读好坏事
    @SuppressWarnings("unchecked")
    public static synchronized void getEvent() {
        GameConstants.HORSE_EVENT = new HorseEvent();
        String jsonObject = JSON.toJSONString(GameConstants.HORSE_EVENT);
        try {
            File file = new File(CONFIG_DIR + "/事件.json");
            if (!file.exists() || !file.isFile()) {
                if (file.createNewFile()) {
                    FileUtils.write(file, jsonObject, "utf-8");
                }
            } else {
                JSONObject jsonObject1 = JSON.parseObject(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
                List<String> bedHorseEvent = JSON.parseObject(jsonObject1.get("badHorseEvent").toString(), List.class);
                List<String> goodHorseEvent = JSON.parseObject(jsonObject1.get("goodHorseEvent").toString(), List.class);
                GameConstants.HORSE_EVENT.getGoodHorseEvent().addAll(goodHorseEvent);
                GameConstants.HORSE_EVENT.getBadHorseEvent().addAll(bedHorseEvent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //读扭蛋信息
    public static synchronized void getGachaConfig() {
        try {
            File file = new File(CONFIG_DIR + "/扭蛋.json");
            if (!file.exists() || !file.isFile()) {
                if (file.createNewFile()) {
                    //准备内置的转蛋信息写入内存
                    //up池
                    JSONObject upGacha = new JSONObject();
                    upGacha.put("三星总概率", SSR_CHANCE);
                    upGacha.put("二星总概率", SR_CHANCE);
                    upGacha.put("一星总概率", R_CHANCE);
                    upGacha.put("三星人物池（除去up角）", NO_UP_SSR);
                    upGacha.put("二星人物池（除去up角）", NO_UP_SR);
                    upGacha.put("一星人物池（除去up角）", NO_UP_R);
                    upGacha.put("三星人物池（up角）", SSR_UP);
                    upGacha.put("二星人物池（up角）", SR_UP);
                    upGacha.put("一星人物池（up角）", R_UP);
                    upGacha.put("三星up总概率", UP_SSR_CHANCE);
                    upGacha.put("二星up总概率", UP_SR_CHANCE);
                    upGacha.put("一星up总概率", UP_R_CHANCE);
                    //白金池
                    JSONObject gacha = new JSONObject();
                    gacha.put("三星总概率", PLATINUM_SSR_CHANCE);
                    gacha.put("二星总概率", PLATINUM_SR_CHANCE);
                    gacha.put("一星总概率", PLATINUM_R_CHANCE);
                    gacha.put("三星人物池", SSR);
                    gacha.put("二星人物池", SR);
                    gacha.put("一星人物池", R);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("up池信息", upGacha);
                    jsonObject.put("白金池信息", gacha);
                    FileUtils.write(file, jsonObject.toJSONString(), "utf-8");
                }
            } else {
                JSONObject jsonObject = JSON.parseObject(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
                JSONObject gacha = jsonObject.getJSONObject("白金池信息");
                JSONObject upGacha = jsonObject.getJSONObject("up池信息");
                PLATINUM_R_CHANCE = (int) gacha.get("一星总概率");
                PLATINUM_SR_CHANCE = (int) gacha.get("二星总概率");
                PLATINUM_SSR_CHANCE = (int) gacha.get("三星总概率");
                R = Arrays.stream(gacha.getJSONArray("一星人物池").toArray()).map(Object::toString).toArray(String[]::new);
                SR = Arrays.stream(gacha.getJSONArray("二星人物池").toArray()).map(Object::toString).toArray(String[]::new);
                SSR = Arrays.stream(gacha.getJSONArray("三星人物池").toArray()).map(Object::toString).toArray(String[]::new);

                R_CHANCE = upGacha.getInteger("一星总概率");
                SR_CHANCE = upGacha.getInteger("二星总概率");
                SSR_CHANCE = upGacha.getInteger("三星总概率");
                NO_UP_R = Arrays.stream(upGacha.getJSONArray("一星人物池（除去up角）").toArray()).map(Object::toString).toArray(String[]::new);
                NO_UP_SR = Arrays.stream(upGacha.getJSONArray("二星人物池（除去up角）").toArray()).map(Object::toString).toArray(String[]::new);
                NO_UP_SSR = Arrays.stream(upGacha.getJSONArray("三星人物池（除去up角）").toArray()).map(Object::toString).toArray(String[]::new);
                R_UP = Arrays.stream(upGacha.getJSONArray("一星人物池（up角）").toArray()).map(Object::toString).toArray(String[]::new);
                SR_UP = Arrays.stream(upGacha.getJSONArray("二星人物池（up角）").toArray()).map(Object::toString).toArray(String[]::new);
                SSR_UP = Arrays.stream(upGacha.getJSONArray("三星人物池（up角）").toArray()).map(Object::toString).toArray(String[]::new);

                UP_R_CHANCE = upGacha.getInteger("一星up总概率");
                UP_SR_CHANCE = upGacha.getInteger("二星up总概率");
                UP_SSR_CHANCE = upGacha.getInteger("三星up总概率");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            log.error("扭蛋配置文件错误，是否删除了一项？");
        }
    }
}
