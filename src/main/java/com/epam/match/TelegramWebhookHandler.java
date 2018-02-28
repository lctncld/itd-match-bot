package com.epam.match;

import com.epam.match.action.ActionFactory;
import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.response.BaseResponse;
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

  public Mono<ServerResponse> route(ServerRequest request) {
    return request.bodyToMono(String.class)
        .map(BotUtils::parseUpdate)
        .map(ActionFactory::fromUpdate)
        .filter(action -> action != null)
        .map(action -> {
          BaseResponse response = bot.execute(action.toCommand());
          log.info("sendMessage {}", response);
          return Mono.empty();
        })
        .flatMap(nop -> Mono.empty());
  }
}
