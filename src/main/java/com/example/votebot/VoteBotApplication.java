package com.example.votebot;

import com.example.votebot.bot.BotConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({BotConfig.class})
public class VoteBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(VoteBotApplication.class, args);
    }

}
