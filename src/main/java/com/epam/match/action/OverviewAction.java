package com.epam.match.action;

import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;

public class OverviewAction implements Action {

  private final Long chatId;

  public OverviewAction(Long chatId) {
    this.chatId = chatId;
  }

  @Override
  public BaseRequest toCommand() {
    return new SendMessage(chatId, "This is just a Demo! Try another button")
        .replyMarkup(new ReplyKeyboardRemove());
  }
}
