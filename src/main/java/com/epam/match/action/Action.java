package com.epam.match.action;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;

public interface Action {

  static Action fromUpdate(Update update) {
    Message message = update.message();
    String text = message.text();
    switch (text) {
      default:
        return new HelpAction(message);
    }
  }

  BaseRequest toCommand();
}
