package com.epam.match;

import com.epam.match.spring.web.TelegramBotHandlerMapping;
import com.epam.match.spring.registry.TelegramBotHandlerRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.HandlerMapping;

@SpringBootApplication
public class MatchApplication {
  @Bean
  public HandlerMapping telegramBotHandlerMapping(TelegramBotHandlerRegistry registry) {
    TelegramBotHandlerMapping handlerMapping = new TelegramBotHandlerMapping();
    return handlerMapping;
  }

  public static void main(String[] args) {
    SpringApplication.run(MatchApplication.class, args);
  }
}
