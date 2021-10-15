package io.koschicken.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeAccount(Properties properties) throws IOException {
        String externalPropFile = "application.properties";
        File file = new File(externalPropFile);
        if (!file.exists()) {
            FileUtils.touch(file);
            initDefaultProperties(properties, file);
        }
        try (InputStreamReader in = new InputStreamReader(new FileInputStream(externalPropFile), StandardCharsets.UTF_8)) {
            properties.load(in);
            String bot = properties.getProperty("simbot.core.bots");
            if (bot == null) {
                deleteConfigs();
                try (OutputStreamWriter op = new OutputStreamWriter(new FileOutputStream(externalPropFile), StandardCharsets.UTF_8)) {
                    scanAccount(properties, op);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteConfigs() {
        FileUtils.deleteQuietly(new File(CONFIG_DIR + "/bilibili.json"));
        FileUtils.deleteQuietly(new File(CONFIG_DIR + "/config.json"));
        FileUtils.deleteQuietly(new File(CONFIG_DIR + "/扭蛋.json"));
        FileUtils.deleteQuietly(new File(CONFIG_DIR + "/事件.json"));
        FileUtils.deleteQuietly(new File(CONFIG_DIR + "/通用配置.txt"));
    }

    private void initDefaultProperties(Properties properties, File file) {
        try (InputStreamReader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            properties.load(in);
            storeProp(properties, file, "setu.price", "0", "### 涩图价格");
            storeProp(properties, file, "cygames.delay", "60", "### 猜语音游戏公布答案的时间");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void storeProp(Properties properties, File file, String propKey, String propValue, String propComment) throws IOException {
        String prop = properties.getProperty(propKey);
        if (prop == null) {
            try (OutputStreamWriter op = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                properties.setProperty(propKey, propValue);
                properties.store(op, propComment);
            }
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
}
