package com.epam.match;

import com.pengrad.telegrambot.BotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class TelegramWebhookHandler {

  private final Logger log = LoggerFactory.getLogger(TelegramWebhookHandler.class);

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
