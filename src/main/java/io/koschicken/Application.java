package io.koschicken;

import io.koschicken.config.ExternalProperties;
import love.forte.simbot.spring.autoconfigure.EnableSimbot;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import top.mrxiaom.qsign.QSignService;

import java.io.File;
import java.io.IOException;

/**
 * 启动类。
 * 其中，{@link SpringBootApplication} 为springboot的启动注解，
 * {@link EnableSimbot} 为simbot在springboot-starter下的启动注解。
 *
 * @author ForteScarlet
 */
@EnableSimbot
@SpringBootApplication
@EnableScheduling
public class Application {
    public static void main(String[] args) throws IOException {
        ExternalProperties externalProperties = new ExternalProperties();
        externalProperties.init();
        initFolders();
        setup();
        SpringApplication.run(Application.class, args);
    }

    private static void initFolders() throws IOException {
        File userFolder = new File("./temp/bilibili/user/");
        if (!userFolder.exists()) {
            FileUtils.forceMkdir(userFolder);
        }
        File liveFolder = new File("./temp/bilibili/live/");
        if (!liveFolder.exists()) {
            FileUtils.forceMkdir(liveFolder);
        }
        File videoFolder = new File("./temp/bilibili/video/");
        if (!videoFolder.exists()) {
            FileUtils.forceMkdir(videoFolder);
        }
        File meowFolder = new File("./temp/MEOW/");
        if (!meowFolder.exists()) {
            FileUtils.forceMkdir(meowFolder);
        }
    }

    public static void setup() {
        QSignService.Factory.init(new File("./qsign/txlib/8.9.88"));
        QSignService.Factory.loadProtocols(null);
        QSignService.Factory.register();
    }
}
