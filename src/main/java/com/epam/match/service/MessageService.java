package com.epam.match.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MessageService {

  private final TelegramBot bot;

  public MessageService(TelegramBot bot) {
    this.bot = bot;
  }

  public Mono<Void> unknownCommand(Update update) {
    Message message = update.message();
    if (message == null) {
      message = update.callbackQuery().message();
    }
    return Mono.just(
        new SendMessage(message.chat().id(),
            String.format("Unrecognized command \"%s\". Try asking for /help", message.text()))
            .replyMarkup(new ReplyKeyboardRemove())
    ).map(bot::execute)
        .then();
  }

  public Mono<Void> help(Update update) {
    return Mono.just(
        new SendMessage(update.message().chat().id(), "Hi! What do you wanna do?")
            .replyMarkup(new InlineKeyboardMarkup(
                new InlineKeyboardButton[] {
                    new InlineKeyboardButton("What is This?")
                        .callbackData("/overview"),
                    new InlineKeyboardButton("Get In")
                        .callbackData("/profile")
                })
            )
    ).map(bot::execute)
        .then();
  }

  public Mono<Void> overview(Update update) {
    return Mono.just(
        new AnswerCallbackQuery(update.callbackQuery().id())
            .text("This is just a Demo! Try another button")
    ).map(bot::execute)
        .then();
  }

  public Mono<Void> leaveProfileConfiguration(Update update) {
    CallbackQuery cb = update.callbackQuery();
    return Flux.just(
        new AnswerCallbackQuery(cb.id()),
        new SendMessage(cb.message().chat().id(), "Okay, Done!")
    ).map(bot::execute)
        .then();
  }
}
