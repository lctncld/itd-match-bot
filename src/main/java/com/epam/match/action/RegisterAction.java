package com.epam.match.action;

import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;

public class RegisterAction implements Action {

  private final Long chatId;

  public RegisterAction(Long chatId) {
    this.chatId = chatId;
  }

  @Override
  public BaseRequest toCommand() {
    KeyboardButton shareLocation = new KeyboardButton("Share My Location")
        .requestLocation(true);
    return new SendMessage(chatId, "Tap on button to share your location")
        .replyMarkup(new ReplyKeyboardMarkup(new KeyboardButton[] { shareLocation }));
//        .replyToMessageId(message.replyToMessage().messageId());
  }
}
