package com.epam.match.action;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;

class HelpAction implements Action {

  private final Message message;

  HelpAction(Message message) {
    this.message = message;
  }

  @Override
  public BaseRequest toCommand() {
    KeyboardButton shareLocation = new KeyboardButton("Share My Location")
        .requestLocation(true);
    return new SendMessage(message.chat().id(), "Share your location to proceed")
        .replyMarkup(new ReplyKeyboardMarkup(new KeyboardButton[] { shareLocation }));
//        .replyToMessageId(message.replyToMessage().messageId());
  }
}
