package com.epam.match;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class TelegramWebhookHandler {

  private final TelegramBot bot;

  public TelegramWebhookHandler(TelegramBot bot) {
    this.bot = bot;
  }

  public Mono<ServerResponse> handle(ServerRequest request) {
    return request.bodyToMono(String.class)
        .map(BotUtils::parseUpdate)
        .flatMap(update -> {
          Message message = update.message();
          SendResponse response = bot.execute(new SendMessage(message.chat().id(), message.text()));
          log.info("sendMessage {}", response);
          return Mono.empty();
        });
  }
}
