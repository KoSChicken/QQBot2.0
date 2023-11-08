package io.koschicken.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.koschicken.bot.Groups;
import io.koschicken.utils.ConfigFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import static io.koschicken.constants.Constants.CONFIG_DIR;

/**
 * 外部配置初始化
 * 包括bot的账号密码和基本的功能开关
 */
@Slf4j
public class ExternalProperties {

    public void init() {
        Properties properties = new Properties();
        try {
            writeAccount(properties);
            configBot(properties.getProperty("masterqq"));
            configGroups();
        } catch (IOException e) {
            log.error("IOException: ", e);
        }
    }

    public void refresh() {
        configBot("");
        configGroups();
    }

    private void writeAccount(Properties properties) throws IOException {
        String externalPropFile = "application.properties";
        File file = new File(externalPropFile);
        if (!file.exists()) {
            FileUtils.touch(file);
        }
        try (InputStreamReader in = new InputStreamReader(new FileInputStream(externalPropFile), StandardCharsets.UTF_8)) {
            properties.load(in);
            String bot = properties.getProperty("simbot.core.bots");
            if (bot == null) {
                FileUtils.cleanDirectory(new File(CONFIG_DIR));
                try (OutputStreamWriter op = new OutputStreamWriter(new FileOutputStream(externalPropFile), StandardCharsets.UTF_8)) {
                    scanAccount(properties, op);
                }
            }
        } catch (IOException e) {
            log.error("IOException: ", e);
        }
    }

    private void scanAccount(Properties properties, OutputStreamWriter op) throws IOException {
        Scanner scanner = new Scanner(System.in);
        log.info("请输入所要登陆的账号");
        log.info("账号:  ");
        String qq = scanner.next();
        log.info("密码:  ");
        String pw = scanner.next();
        log.info("masterQQ（用于控制Bot开关，不填写则bot无法开启）:  ");
        String masterQQ = scanner.next();
        properties.setProperty("simbot.core.bots", qq + ":" + pw);
        properties.setProperty("masterqq", masterQQ);
        properties.store(op, "### bot config");
    }

    private void configBot(String masterqq) {
        File file = new File(CONFIG_DIR + "/bot.json");
        ConfigFileUtils.createFileIfNotExists(file);
        JSONObject jsonObject = null;
        try {
            jsonObject = ConfigFileUtils.readFileToJSON(file);
        } catch (IOException e) {
            log.error("配置文件读取失败", e);
        }
        BotConfig botConfig = BotConfig.getInstance();
        if (jsonObject != null) {
            botConfig.setGlobalSwitch(jsonObject.getBoolean("globalSwitch"));
            botConfig.setDiceSwitch(jsonObject.getBoolean("diceSwitch"));
            botConfig.setSetuSwitch(jsonObject.getBoolean("setuSwitch"));
            botConfig.setMasterQQ(jsonObject.getString("masterQQ"));
            botConfig.setR18Private(jsonObject.getBoolean("r18Private"));
            botConfig.setSauceNaoApiKey(jsonObject.getString("sauceNaoApiKey"));
            botConfig.setSetuBlackTags(jsonObject.getString("setuBlackTags"));
        } else {
            botConfig.setGlobalSwitch(true);
            botConfig.setDiceSwitch(true);
            botConfig.setSetuSwitch(true);
            botConfig.setMasterQQ(masterqq);
            botConfig.setR18Private(true);
            botConfig.setSauceNaoApiKey("");
            botConfig.setSetuBlackTags("");
        }
        ConfigFileUtils.updateBotConfig(botConfig);
    }

    private void configGroups() {
        File file = new File(CONFIG_DIR + "/groups.json");
        ConfigFileUtils.createFileIfNotExists(file);
        try {
            String jsonString = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            if (StringUtils.hasText(jsonString)) {
                JSONObject jsonObject = JSON.parseObject(jsonString);
                Set<String> keySet = jsonObject.keySet();
                for (String s : keySet) {
                    String string = jsonObject.getJSONObject(s).toJSONString();
                    GroupConfig keyValues = JSON.parseObject(string, GroupConfig.class);
                    Groups.getInstance().put(s, keyValues);
                }
            }
        } catch (IOException | NullPointerException e) {
            log.error("error: ", e);
        }

    }
}
