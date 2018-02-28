package com.epam.match.action;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;

public class RegisterAction implements Action {

  private final Message message;

  public RegisterAction(Message message) {
    this.message = message;
  }

  @Override
  public BaseRequest toCommand() {
    KeyboardButton shareLocation = new KeyboardButton("Share My Location")
        .requestLocation(true);
    InlineKeyboardButton button = new InlineKeyboardButton("Share")
        .callbackData("wtf");

    return new SendMessage(message.chat().id(), "Share your location to proceed")
        .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[] { button }));
//        .replyToMessageId(message.replyToMessage().messageId());
  }
}
