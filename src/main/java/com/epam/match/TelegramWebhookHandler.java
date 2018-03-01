package com.epam.match;

import com.pengrad.telegrambot.BotUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class TelegramWebhookHandler {

  private final TelegramUpdateRouter router;

  public TelegramWebhookHandler(TelegramUpdateRouter router) {
    this.router = router;
  }

  public Mono<ServerResponse> route(ServerRequest request) {
    request.bodyToMono(String.class)
        .map(BotUtils::parseUpdate)
        .map(router::route)
        .map(Mono::subscribe)
        .log()
        .subscribe();
    return Mono.empty();
  }
}
