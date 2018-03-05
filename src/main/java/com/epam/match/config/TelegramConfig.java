package com.epam.match.config;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SetWebhook;
import com.pengrad.telegrambot.response.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramConfig {

  private final Logger log = LoggerFactory.getLogger(TelegramConfig.class);

  @Value("${TELEGRAM_TOKEN}")
  private String token;

  @Value("${TELEGRAM_WEBHOOK_URL}")
  private String webHookUrl;

  @Bean
  public TelegramBot telegramBot() {
    TelegramBot bot = new TelegramBot.Builder(token).build();
    BaseResponse res = bot.execute(new SetWebhook().url(webHookUrl));
    log.info("setWebhook {}", res);
    if (!res.isOk()) {
      throw new RuntimeException(
        String.format("Failed to create TelegramBot: %s %s", res.errorCode(), res.description()));
    }
    return bot;
  }
}
