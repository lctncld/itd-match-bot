package com.epam.match.spring.web;

import com.epam.match.spring.registry.TelegramBotHandlerRegistry;
import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.model.Update;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class TelegramBotHandlerMapping implements HandlerMapping, InitializingBean, Ordered {

  private final TelegramBotHandlerRegistry registry;

  private final CommandExtractor commandExtractor;

  private List<HttpMessageReader<?>> messageReaders = Collections.emptyList();

  public TelegramBotHandlerMapping(TelegramBotHandlerRegistry registry,
    CommandExtractor commandExtractor) {
    this.registry = registry;
    this.commandExtractor = commandExtractor;
  }

  @Override
  public Mono<Object> getHandler(ServerWebExchange exchange) {
    ServerRequest request = ServerRequest.create(exchange, this.messageReaders);
    return request.bodyToMono(String.class)
      .map(BotUtils::parseUpdate)
      .flatMap(update -> {
        String command = commandExtractor.getCommand(update);
        log.info("Command: {}, Update: {}", command, update);
        return Mono.zip(
          registry.getHandler(command, update),
          Mono.justOrEmpty(update)
        );
      })
      .map(tuple -> {
        HandlerMethod handlerMethod = tuple.getT1();
        Update update = tuple.getT2();
        return InvokableUpdate.builder()
          .method(handlerMethod)
          .update(update)
          .build();
      });
  }

  @Override
  public void afterPropertiesSet() {
    if (CollectionUtils.isEmpty(this.messageReaders)) {
      ServerCodecConfigurer codecConfigurer = ServerCodecConfigurer.create();
      this.messageReaders = codecConfigurer.getReaders();
    }
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
