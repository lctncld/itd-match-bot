package com.epam.match.service;

import com.epam.match.session.Step;
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

import java.util.stream.Collectors;

import static java.util.Collections.singletonMap;

@Service
public class ProfileService {

  private static final String KEY = "users";

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
    InlineKeyboardMarkup actions = new InlineKeyboardMarkup(
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
    );
    Long chatId = update.callbackQuery().message().chat().id();
    return commands.hgetall(key(update.callbackQuery().from().id()))
        .map(profile -> {
          String message = profile.isEmpty()
              ? "Your profile appears to be blank, tap these buttons to fill it!"
              : "So, your settings are:\n" + profile.entrySet().stream()
                  .map(entry -> entry.getKey() + ": " + entry.getValue())
                  .collect(Collectors.joining(", "));
          return bot.execute(new SendMessage(chatId, message)
              .replyMarkup(actions));
        }).then();
  }

  private String key(Integer userId) {
    return KEY + ":" + userId.toString();
  }

  public Mono<Void> askAge(Update update) {
    CallbackQuery callbackQuery = update.callbackQuery();
    return sessionService.set(callbackQuery.from().id().toString(), Step.SET_MY_AGE)
        .thenReturn(new SendMessage(callbackQuery.message().chat().id(), "So, what's your age?"))
        .map(bot::execute)
        .then();
  }

  public Mono<Void> setAge(Update update) {
    Message message = update.message();
    String age = message.text();
    return commands.hmset(key(message.from().id()), singletonMap("age", age))
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
