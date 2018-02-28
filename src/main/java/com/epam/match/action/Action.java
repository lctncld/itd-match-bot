package com.epam.match.action;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;

public interface Action {

  static Action fromUpdate(Update update) {
    Message message = update.message();
    if (message == null) {
      CallbackQuery callback = update.callbackQuery();
      if (callback == null) {
        throw new RuntimeException("Both message and callback are null");
      }
      Long chatId = callback.message().chat().id();
      return new HelpAction(chatId);
    }

    String text = message.text();
    Long chatId = message.chat().id();
    if (text == null) {
      return new HelpAction(chatId);
    }

    switch (text) {
      case "/register":
        return new RegisterAction(message);
      default:
        return new HelpAction(chatId);
    }
  }

  BaseRequest toCommand();
}
