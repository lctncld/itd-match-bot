package com.epam.match.action;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;

class HelpAction implements Action {

  private final Long chatId;

  HelpAction(Long chatId) {
    this.chatId = chatId;
  }

  @Override
  public BaseRequest toCommand() {
    InlineKeyboardButton overviewButton = new InlineKeyboardButton("What is This?")
        .callbackData("/overview");
    InlineKeyboardButton registerButton = new InlineKeyboardButton("Get In")
        .callbackData("/register");
    return new SendMessage(chatId, "Hi! What do you wanna do?")
        .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[] { overviewButton, registerButton }));
  }
}
