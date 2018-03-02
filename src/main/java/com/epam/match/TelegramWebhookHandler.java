package com.epam.match;

import com.pengrad.telegrambot.BotUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class TelegramWebhookHandler {

  private final TelegramUpdateRouter router;

  public TelegramWebhookHandler(TelegramUpdateRouter router) {
    this.router = router;
  }

  public Mono<ServerResponse> route(ServerRequest request) {
    return request.bodyToMono(String.class)
        .doOnNext(log::info)
        .map(BotUtils::parseUpdate)
        .map(router::route)
        .map(Mono::subscribe)
        .log()
        .then(ServerResponse.ok().build());
  }
}
