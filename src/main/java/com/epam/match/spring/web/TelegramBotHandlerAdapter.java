package com.epam.match.spring.web;

import com.epam.match.spring.registry.TelegramBotHandlerRegistry;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.HandlerAdapter;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Map;

@Component
public class TelegramBotHandlerAdapter implements HandlerAdapter {

  @Autowired
  private TelegramBotHandlerRegistry registry;

  @Autowired
  private TelegramBot bot;

  @Override
  public boolean supports(Object handler) {
    if (handler instanceof InvokableUpdate) {
      HandlerMethod handlerMethod = ((InvokableUpdate) handler).getMethod();
      return registry.getMessageHandlers().containsValue(handlerMethod)
        || registry.getCallbackHandlers().containsValue(handlerMethod);
    }
    return false;
  }

  @Override
  public Mono<HandlerResult> handle(ServerWebExchange exchange, Object handler) {
    InvokableUpdate updateHandler = (InvokableUpdate) handler;
    HandlerMethod handlerMethod = updateHandler.getMethod();

    Method method = handlerMethod.getMethod();
    Object bean = handlerMethod.getBean();
    Update update = updateHandler.getUpdate();

    Object result = ReflectionUtils.invokeMethod(method, bean, update);
//    HandlerResult handlerResult = new HandlerResult(handler, result, handlerMethod.getReturnType());

    if (result instanceof Flux) {
      return ((Flux<BaseRequest>) result)
        .map(bot::execute)
        .onErrorResume(ex -> Mono.error(ex))
        .then(Mono.empty());
    } else if (result instanceof Mono) {
      return ((Mono<BaseRequest>) result)
        .map(bot::execute)
        .onErrorResume(ex -> Mono.error(ex))
        .then(Mono.empty());
    }
    return Mono.empty();
  }
}
