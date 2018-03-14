package com.epam.match.service.telegram;

import com.epam.match.spring.annotation.MessageMapping;
import com.epam.match.spring.annotation.TelegramBotController;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import reactor.core.publisher.Mono;

@TelegramBotController
public class MessageService {

  @MessageMapping("/unknown_command")
  public Mono<BaseRequest> unknownCommand(Update update) {
    Message message = update.message();
    if (message == null) {
      message = update.callbackQuery().message();
    }
    return Mono.just(
      new SendMessage(message.chat().id(), "Unrecognized command. Try asking for /help")
        .replyMarkup(new ReplyKeyboardRemove())
    );
  }

  @MessageMapping("/help")
  public Mono<BaseRequest> help(Update update) {
    Long chatId = update.message().chat().id();
    return Mono.just(
      new SendMessage(chatId, "Hi! Type /profile to set up your profile, or try a button below!").replyMarkup(
        new InlineKeyboardMarkup(
          new InlineKeyboardButton[] {
            new InlineKeyboardButton("What is This?")
              .callbackData("/overview"),
          })
      )
    );
  }

  @MessageMapping(value = "/overview")
  public Mono<BaseRequest> overview(Update update) {
    return Mono.just(
      new AnswerCallbackQuery(update.callbackQuery().id())
        .text("This is just a Demo! Try another button")
    );
  }
}
