package com.epam.match.service;

import com.epam.match.Steps;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ProfileService {

  private final TelegramBot bot;

  private final RedisReactiveCommands<String, String> commands;

  private final SessionService sessionService;

  private final LocationService locationService;

  public ProfileService(TelegramBot bot, RedisReactiveCommands<String, String> commands, SessionService sessionService,
      LocationService locationService) {
    this.bot = bot;
    this.commands = commands;
    this.sessionService = sessionService;
    this.locationService = locationService;
  }

  public Mono<Void> setupProfile(Update update) {
    SendMessage cmd = new SendMessage(update.callbackQuery().message().chat().id(), "TODO: get current profile")
        .replyMarkup(new InlineKeyboardMarkup(
            new InlineKeyboardButton[] {
                new InlineKeyboardButton("Your gender")
                    .callbackData("/profile/me/gender"),
                new InlineKeyboardButton("Your age")
                    .callbackData("/profile/me/age"),
            },
            new InlineKeyboardButton[] {
                new InlineKeyboardButton("Gender")
                    .callbackData("/profile/match/gender"),
                new InlineKeyboardButton("Min age")
                    .callbackData("/profile/match/age/from"),
                new InlineKeyboardButton("Max age")
                    .callbackData("/profile/match/age/to")
            },
            new InlineKeyboardButton[] {
                new InlineKeyboardButton("No more changes needed")
                    .callbackData("/profile/done")
            }
        ));
    return Mono.just(cmd)
        .map(bot::execute)
        .then();
  }

  public Mono<Void> askAge(Update update) {
    CallbackQuery callbackQuery = update.callbackQuery();
    return sessionService.set(callbackQuery.from().id().toString(), Steps.SET_MY_AGE)
        .thenReturn(new SendMessage(callbackQuery.message().chat().id(), "So, what's your age?"))
        .map(bot::execute)
        .then();
  }

  public Mono<Void> setAge(Update update) {
    Message message = update.message();
    String age = message.text();
    return commands.hset("users", message.from().id().toString(), age)
        .thenReturn(new SendMessage(message.chat().id(), String.format("Okay, your age is %s", age)))
        .map(bot::execute)
        .then();
  }

  public Mono<Void> setLocation(Update update) {
    return locationService.set(update.message().from().id().toString(), update.message().location())
        .thenReturn(new SendMessage(update.message().chat().id(), "Your location is updated"))
        .map(bot::execute)
        .then();
  }
}
