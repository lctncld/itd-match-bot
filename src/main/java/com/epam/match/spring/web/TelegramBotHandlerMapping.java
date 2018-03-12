package com.epam.match.spring.web;

import com.epam.match.spring.registry.TelegramBotHandlerRegistry;
import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.model.Update;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class TelegramBotHandlerMapping implements HandlerMapping, InitializingBean, Ordered {

  private List<HttpMessageReader<?>> messageReaders = Collections.emptyList();

  @Autowired
  private TelegramBotHandlerRegistry registry;

  public void setMessageReaders(List<HttpMessageReader<?>> messageReaders) {
    this.messageReaders = messageReaders;
  }

  @Override
  public Mono<Object> getHandler(ServerWebExchange exchange) {
    ServerRequest request = ServerRequest.create(exchange, this.messageReaders);
    Update update = request.bodyToMono(String.class)
      .map(BotUtils::parseUpdate)
      .block();

    Map<String, HandlerMethod> callbackHandlers = registry.getCallbackHandlers();
    Map<String, HandlerMethod> messageHandlers = registry.getMessageHandlers();

    HandlerMethod handlerMethod;
    if (update.callbackQuery() != null) {
      String cmd = update.callbackQuery().data();
      handlerMethod = callbackHandlers.get(cmd);
    } else if (update.message() != null) {
      String text = update.message().text();
      handlerMethod = messageHandlers.get(text);
      if (handlerMethod == null) {
        handlerMethod = messageHandlers.get("/unknown_command");
      }
    } else {
      handlerMethod = messageHandlers.get("/unknown_command");
    }
    log.info("Handler for {} is {}", update, handlerMethod);

    return Mono.just(
      InvokableUpdate.builder()
        .method(handlerMethod)
        .update(update)
        .build()
    );
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
