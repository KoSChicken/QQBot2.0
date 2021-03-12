package io.koschicken;

import io.koschicken.config.ExternalProperties;
import io.koschicken.db.InitDatabase;
import love.forte.simbot.spring.autoconfigure.EnableSimbot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类。
 * 其中，{@link SpringBootApplication} 为springboot的启动注解，
 * {@link EnableSimbot} 为simbot在springboot-starter下的启动注解。
 *
 * @author ForteScarlet
 */
@EnableSimbot
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        ExternalProperties externalProperties = new ExternalProperties();
        externalProperties.init();
        InitDatabase initDatabase = new InitDatabase();
        initDatabase.initDB();
        SpringApplication.run(Application.class, args);
    }
}
