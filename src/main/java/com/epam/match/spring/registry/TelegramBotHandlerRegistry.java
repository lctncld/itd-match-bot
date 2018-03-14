package com.epam.match.spring.registry;

import com.epam.match.service.session.ProfileSetupStep;
import com.epam.match.service.session.SessionService;
import com.pengrad.telegrambot.model.Update;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class TelegramBotHandlerRegistry {

  private final Map<String, HandlerMethod> handlers = new HashMap<>();

  private final SessionService sessionService;

  public TelegramBotHandlerRegistry(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  public void addMessageHandler(String command, HandlerMethod method) {
    handlers.put(command, method);
  }

  public Mono<HandlerMethod> getHandler(String command, Update update) {
    log.info("getting handler for {}", command);
    return Mono.justOrEmpty(command)
      .flatMap(cmd -> Mono.justOrEmpty(handlers.get(cmd)))
      .switchIfEmpty(
        Mono.defer(() -> Mono.just(update.message().from().id()))
          .flatMap(sessionService::get)
          .filter(step -> step != ProfileSetupStep.UNKNOWN)
          .map(Enum::toString)
          .map(handlers::get)
      )
      .defaultIfEmpty(handlers.get("/unknown_command"));
  }
}
