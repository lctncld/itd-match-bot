package com.epam.match.action;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;

class HelpAction implements Action {

  private final Message message;

  HelpAction(Message message) {
    this.message = message;
  }

  @Override
  public BaseRequest toCommand() {
   return new SendMessage(message.chat().id(), "noop")
       .replyMarkup(new ReplyKeyboardRemove());
  }
}
