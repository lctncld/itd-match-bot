package com.epam.match.action;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import reactor.core.publisher.Flux;

public class ProfileSetupAction implements Action {

  private final Long chatId;

  public ProfileSetupAction(Long chatId) {
    this.chatId = chatId;
  }

  @Override
  public Flux<BaseRequest> execute() {
    SendMessage sendMenuCommand = new SendMessage(chatId, "TODO: get current profile")
        .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[] {
                new InlineKeyboardButton("Your gender")
                    .callbackData("/profile/me/gender"),
                new InlineKeyboardButton("Your age")
                    .callbackData("/profile/me/age"),
                new InlineKeyboardButton("Gender")
                    .callbackData("/profile/match/gender"),
                new InlineKeyboardButton("Min age")
                    .callbackData("/profile/match/age/from"),
                new InlineKeyboardButton("Max age")
                    .callbackData("/profile/match/age/to"),
                new InlineKeyboardButton("No more changes needed")
                    .callbackData("/profile/done")
            })
        );
    return Flux.just(sendMenuCommand);
  }
}
