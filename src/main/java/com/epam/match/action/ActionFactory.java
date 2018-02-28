package com.epam.match.action;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Location;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActionFactory {

  public static Action fromUpdate(Update update) {
    String command = command(update);
    Long chatId = chatId(update);
    if (command == null) {
      return new NoopAction(chatId);
    }
    switch (command) {
      case "/help":
        return new HelpAction(chatId);
      case "/overview":
        return new OverviewAction(update.callbackQuery().id());
      case "/register":
        return new ProfileSetupAction(chatId);
      case "/location":
//        Location location = update.message().location();
//        return new
      default:
        return new NoopAction(chatId);
    }
  }

  private static String command(Update update) {
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

    }
    throw new RuntimeException("Both callback and message are null");
  }

  private static Long chatId(Update update) {
    CallbackQuery callback = update.callbackQuery();
    if (callback != null) {
      return callback.message().chat().id();
    }
    Message message = update.message();
    if (message != null) {
      return message.chat().id();
    }
    throw new RuntimeException("Both callback and message are null");
  }

  // TODO
  private static String extractCommand(String text) {
    boolean isCommand = text != null && text.startsWith("/");
    if (!isCommand) {
      log.info("String {} is not a command", text);
    }
    return text;
  }
}
