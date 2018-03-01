package com.epam.match;

import com.epam.match.command.TelegramUpdateRouter;
import com.pengrad.telegrambot.BotUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class TelegramWebhookHandler {

  private final TelegramUpdateRouter factory;

  public TelegramWebhookHandler(TelegramUpdateRouter factory) {
    this.factory = factory;
  }

  public Mono<ServerResponse> route(ServerRequest request) {
    request.bodyToMono(String.class)
        .map(BotUtils::parseUpdate)
        .map(factory::route)
        .map(Mono::subscribe)
        .subscribe();
    return Mono.empty();
  }
}
