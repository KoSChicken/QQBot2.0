package io.koschicken;

import io.koschicken.config.ExternalProperties;
import love.forte.simbot.spring.autoconfigure.EnableSimbot;
import net.mamoe.mirai.utils.BotConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import xyz.cssxsh.mirai.tool.FixProtocolVersion;

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
    public static void main(String[] args) {
        ExternalProperties externalProperties = new ExternalProperties();
        externalProperties.init();
        InitConfig.initConfigs();
        FixProtocolVersion.fetch(BotConfiguration.MiraiProtocol.ANDROID_PAD, "latest");
        FixProtocolVersion.load(BotConfiguration.MiraiProtocol.ANDROID_PAD);
        SpringApplication.run(Application.class, args);
    }
}
