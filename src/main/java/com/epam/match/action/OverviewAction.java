package com.epam.match.action;

import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.BaseRequest;
import reactor.core.publisher.Flux;

class OverviewAction implements Action {

  private final String callbackQueryId;

  OverviewAction(String callbackQueryId) {
    this.callbackQueryId = callbackQueryId;
  }

  @Override
  public Flux<BaseRequest> execute() {
    return Flux.just(
        new AnswerCallbackQuery(callbackQueryId)
            .text("This is just a Demo! Try another button")
    );
  }
}
