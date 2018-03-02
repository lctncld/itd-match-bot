package com.epam.match.config;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SetWebhook;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TelegramProperties.class)
@Slf4j
public class TelegramConfig {

  private final TelegramProperties telegram;

  public TelegramConfig(TelegramProperties telegram) {
    this.telegram = telegram;
  }

  @Bean
  public TelegramBot telegramBot() {
    log.info("Telegram props: {}", telegram);
    TelegramBot bot = new TelegramBot.Builder(telegram.getToken()).build();
    BaseResponse res = bot.execute(new SetWebhook().url(telegram.getWebHookUrl()));
    log.info("setWebhook {}", res);
    if (!res.isOk()) {
      throw new RuntimeException(
          String.format("Failed to create TelegramBot: %s %s", res.errorCode(), res.description()));
    }
    return bot;
  }
}
