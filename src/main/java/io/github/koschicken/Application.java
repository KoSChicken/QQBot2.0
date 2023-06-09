package io.github.koschicken;

import love.forte.simboot.spring.autoconfigure.EnableSimbot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableSimbot
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
