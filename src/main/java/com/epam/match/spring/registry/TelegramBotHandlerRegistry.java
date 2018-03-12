package com.epam.match.spring.registry;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.HashMap;
import java.util.Map;

@Component
public class TelegramBotHandlerRegistry {

  private final Map<String, HandlerMethod> messageHandlers = new HashMap<>();

  private final Map<String, HandlerMethod> callbackHandlers = new HashMap<>();

  public void addMessageHandler(String command, HandlerMethod method) {
    messageHandlers.put(command, method);
  }

  public void addCallbackHandler(String command, HandlerMethod method) {
    callbackHandlers.put(command, method);
  }

  public Map<String, HandlerMethod> getMessageHandlers() {
    return messageHandlers;
  }

  public Map<String, HandlerMethod> getCallbackHandlers() {
    return callbackHandlers;
  }
}
