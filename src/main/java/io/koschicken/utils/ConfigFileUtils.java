package io.koschicken.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.koschicken.bot.Groups;
import io.koschicken.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.koschicken.constants.Constants.CONFIG_DIR;

@Slf4j
public class ConfigFileUtils {

    private ConfigFileUtils() {
    }

    public static void createFileIfNotExists(File file) {
        if (!file.exists() || !file.isFile()) {
            try {
                FileUtils.touch(file);
            } catch (IOException e) {
                log.error("配置文件创建失败: ", e);
            }
        }
    }

    public static String readFile(File file) throws IOException {
        return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    }

    public static JSONObject readFileToJSON(File file) throws IOException {
        String json = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        return JSON.parseObject(json);
    }

    public static synchronized void updateBotConfig(BotConfig botConfig) {
        String jsonObject = JSON.toJSONString(botConfig);
        try {
            File file = new File(CONFIG_DIR + "/bot.json");
            FileUtils.write(file, jsonObject, "utf-8");
        } catch (IOException e) {
            log.error("创建Bot配置失败");
        }
    }

    public static synchronized void updateGroupConfig(Groups groups) {
        String jsonObject = JSON.toJSONString(groups.getGroupConfigMap());
        try {
            File file = new File(CONFIG_DIR + "/groups.json");
            if (!file.exists() || !file.isFile()) {
                FileUtils.touch(file);
            }
            FileUtils.write(file, jsonObject, "utf-8");
        } catch (IOException e) {
            log.error("创建群组配置失败");
        }
    }
}
