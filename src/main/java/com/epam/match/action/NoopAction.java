package com.epam.match.action;

import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;

public class NoopAction implements Action {

  private final Long chatId;

  public NoopAction(Long chatId) {
    this.chatId = chatId;
  }

  @Override
  public BaseRequest toCommand() {
    return new SendMessage(chatId, "Unrecognized command. Try asking for /help")
        .replyMarkup(new ReplyKeyboardRemove());
  }
}
