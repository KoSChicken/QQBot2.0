package io.koschicken.config;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;

/**
 * 外部配置初始化
 * 包括bot的账号密码和基本的功能开关
 */
public class ExternalProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalProperties.class);

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
                try (OutputStreamWriter op = new OutputStreamWriter(new FileOutputStream(externalPropFile), StandardCharsets.UTF_8)) {
                    scanAccount(properties, op);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initDefaultProperties(Properties properties, File file) {
        try (InputStreamReader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            properties.load(in);
            storeProp(properties, file, "setu.price", "0", "### 涩图价格");
            storeProp(properties, file, "setu.tags", "", "### 会触发bot嘴臭的tag");
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
        LOGGER.info("请输入所要登陆的账号");
        LOGGER.info("账号:  ");
        String qq = scanner.next();
        LOGGER.info("密码:  ");
        String pw = scanner.next();
        properties.setProperty("simbot.core.bots", qq + ":" + pw);
        properties.store(op, "### bot config");
    }
}
