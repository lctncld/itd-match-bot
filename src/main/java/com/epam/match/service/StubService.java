package com.epam.match.service;

import com.epam.match.command.CallbackQueryCommand;
import com.epam.match.command.NoArgCommand;
import com.epam.match.command.UnrecognizedCommand;
import com.pengrad.telegrambot.TelegramBot;
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

  public Mono<Void> unknownCommand(UnrecognizedCommand command) {
    SendMessage cmd = new SendMessage(command.getChatId(), "Unrecognized command. Try asking for /help")
        .replyMarkup(new ReplyKeyboardRemove());
    return Mono.just(cmd)
        .map(bot::execute)
        .then();
  }

  public Mono<Void> help(NoArgCommand command) {
    InlineKeyboardButton overviewButton = new InlineKeyboardButton("What is This?")
        .callbackData("/overview");
    InlineKeyboardButton registerButton = new InlineKeyboardButton("Get In")
        .callbackData("/register");

    SendMessage cmd = new SendMessage(command.getChatId(), "Hi! What do you wanna do?")
        .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[] { overviewButton, registerButton }));

    return Mono.just(cmd)
        .map(bot::execute)
        .then();
  }

  public Mono<Void> overview(CallbackQueryCommand command) {
    AnswerCallbackQuery cmd = new AnswerCallbackQuery(command.getQueryId())
        .text("This is just a Demo! Try another button");
    return Mono.just(cmd)
        .map(bot::execute)
        .then();
  }
}
