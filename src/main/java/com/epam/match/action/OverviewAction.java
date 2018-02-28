package com.epam.match.action;

import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.BaseRequest;

public class OverviewAction implements Action {

  private final String callbackQueryId;

  public OverviewAction(String callbackQueryId) {
    this.callbackQueryId = callbackQueryId;
  }

  @Override
  public BaseRequest toCommand() {
    return new AnswerCallbackQuery(callbackQueryId)
        .text("This is just a Demo! Try another button");
  }
}
