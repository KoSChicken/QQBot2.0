package io.koschicken.config;

import io.koschicken.utils.ConfigFileUtils;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

import static io.koschicken.constants.Constants.CONFIG_DIR;

@Getter
public class BilibiliConfig {
    private static BilibiliConfig instance;
    private String cookie;

    private BilibiliConfig() {
    }

    public static synchronized BilibiliConfig getInstance() throws IOException {
        if (instance == null) {
            instance = new BilibiliConfig();
        }
        instance.cookie = ConfigFileUtils.readFile(new File(CONFIG_DIR + "/bilibili-cookie.txt"));
        return instance;
    }
}
