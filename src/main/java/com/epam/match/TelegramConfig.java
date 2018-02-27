package com.epam.match;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SetWebhook;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class TelegramConfig {

  @Value("${telegram.token}")
  private String token;

  @Value("${telegram.webhook.url}")
  private String url;

  @Bean
  public TelegramBot telegramBot() {
    TelegramBot bot = new TelegramBot.Builder(token).build();
    BaseResponse res = bot.execute(new SetWebhook().url(url));
    log.info("setWebhook {}", res);
    if (!res.isOk()) {
      throw new RuntimeException(
          String.format("Failed to create TelegramBot: %s %s", res.errorCode(), res.description()));
    }
    return bot;
  }
}
