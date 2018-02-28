package com.epam.match.action;

import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import reactor.core.publisher.Flux;

class RegisterAction implements Action {

  private final Long chatId;

  private String callbackQueryId;

  public RegisterAction(Long chatId) {
    this.chatId = chatId;
  }

  public Action callbackQueryId(String id) {
    this.callbackQueryId = id;
    return this;
  }

  @Override
  public Flux<BaseRequest> execute() {
    KeyboardButton shareLocation = new KeyboardButton("Share My Location")
        .requestLocation(true);
    SendMessage shareLocationRequest = new SendMessage(chatId, "Tap on button to share your location")
        .replyMarkup(new ReplyKeyboardMarkup(new KeyboardButton[] { shareLocation })
            .oneTimeKeyboard(true)
            .resizeKeyboard(true)
        );
    if (callbackQueryId == null) {
      return Flux.just(shareLocationRequest);
    }

    return Flux.just(
        shareLocationRequest,
        new AnswerCallbackQuery(callbackQueryId)
    );
  }
}
