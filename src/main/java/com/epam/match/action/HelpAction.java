package com.epam.match.action;

import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;

class HelpAction implements Action {

  private final Long chatId;

  HelpAction(Long chatId) {
    this.chatId = chatId;
  }

  @Override
  public BaseRequest toCommand() {
   return new SendMessage(chatId, "noop")
       .replyMarkup(new ReplyKeyboardRemove());
  }
}
