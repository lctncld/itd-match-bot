package com.epam.match.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class StubService {

  private final TelegramBot bot;

  public StubService(TelegramBot bot) {
    this.bot = bot;
  }

  public Mono<Void> unknownCommand(Update update) {
    Message message = update.message();
    if (message == null) {
      message = update.callbackQuery().message();
    }
    SendMessage cmd = new SendMessage(message.chat().id(),
        String.format("Unrecognized command \"%s\". Try asking for /help", message.text()))
        .replyMarkup(new ReplyKeyboardRemove());
    return Mono.just(cmd)
        .map(bot::execute)
        .then();
  }

  public Mono<Void> help(Update update) {
    InlineKeyboardButton overviewButton = new InlineKeyboardButton("What is This?")
        .callbackData("/overview");
    InlineKeyboardButton registerButton = new InlineKeyboardButton("Get In")
        .callbackData("/register");

    SendMessage cmd = new SendMessage(update.message().chat().id(), "Hi! What do you wanna do?")
        .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[] { overviewButton, registerButton }));

    return Mono.just(cmd)
        .map(bot::execute)
        .then();
  }

  public Mono<Void> overview(Update update) {
    AnswerCallbackQuery cmd = new AnswerCallbackQuery(update.callbackQuery().id())
        .text("This is just a Demo! Try another button");
    return Mono.just(cmd)
        .map(bot::execute)
        .then();
  }
}
