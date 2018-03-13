package com.epam.match.spring.web;

import com.epam.match.spring.registry.TelegramBotHandlerRegistry;
import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Contact;
import com.pengrad.telegrambot.model.Location;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Update;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

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

    String command = getCommand(update);
    return registry.getHandler(command, update)
      .doOnNext(handler -> log.info("Command: {}, Handler: {}, Update: {}", command, handler, update))
      .map(handler -> InvokableUpdate.builder()
        .method(handler)
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

  private String getCommand(Update update) {
    CallbackQuery callback = update.callbackQuery();
    if (callback != null) {
      String callbackData = callback.data();
      return extractCommand(callbackData);
    }

    Message message = update.message();
    if (message != null) {

      String messageText = message.text();
      if (messageText != null) {
        return extractCommand(messageText);
      }

      Location location = message.location();
      if (location != null) {
        return "/location";
      }
      Contact contact = message.contact();
      if (contact != null) {
        return "/contact";
      }
      PhotoSize[] photo = message.photo();
      if (photo != null && photo.length > 0) {
        return "/photo";
      }
    }
    return null;
  }

  // TODO
  private String extractCommand(String text) {
    boolean isCommand = text != null && text.startsWith("/");
    if (!isCommand) {
      log.info("String {} is not a command", text);
    }
    return isCommand
      ? text
      : null;
  }
}
