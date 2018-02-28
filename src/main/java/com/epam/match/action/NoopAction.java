package com.epam.match.action;

import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import reactor.core.publisher.Flux;

class NoopAction implements Action {

  private final Long chatId;

  NoopAction(Long chatId) {
    this.chatId = chatId;
  }

  @Override
  public Flux<BaseRequest> execute() {
    return Flux.just(
        new SendMessage(chatId, "Unrecognized command. Try asking for /help")
            .replyMarkup(new ReplyKeyboardRemove())
    );
  }
}
