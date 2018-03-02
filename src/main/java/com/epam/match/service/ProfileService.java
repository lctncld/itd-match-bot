package com.epam.match.service;

import com.epam.match.RedisKeys;
import com.epam.match.domain.Gender;
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
                .callbackData("/profile/match/age/min"),
            new InlineKeyboardButton("Max age")
                .callbackData("/profile/match/age/max")
        },
        new InlineKeyboardButton[] {
            new InlineKeyboardButton("No more changes needed")
                .callbackData("/profile/done")
        }
    );
    Long chatId = update.callbackQuery().message().chat().id();
    return commands.hgetall(RedisKeys.user(update.callbackQuery().from().id()))
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

  public Mono<Void> setAge(Update update) {
    Message message = update.message();
    String age = message.text();
    return commands.hmset(RedisKeys.user(message.from().id()), singletonMap("age", age))
        .thenReturn(new SendMessage(message.chat().id(), String.format("Okay, your age is %s", age)))
        .map(bot::execute)
        .then();
  }

  public Mono<Void> setLocation(Update update) {
    Message message = update.message();
    return locationService.set(message.from().id().toString(), message.location())
        .thenReturn(new SendMessage(message.chat().id(), "Your location is updated"))
        .map(bot::execute)
        .then();
  }

  public Mono<Void> setMatchGender(Update update, Gender gender) {
    CallbackQuery cb = update.callbackQuery();
    return commands.hmset(RedisKeys.user(cb.from().id()), singletonMap("matchGender", gender.toString()))
        .thenReturn(new SendMessage(cb.message().chat().id(), "Now looking for " + gender.toString()))
        .map(bot::execute)
        .then();

  }

  public Mono<Void> setGender(Update update, Gender gender) {
    CallbackQuery cb = update.callbackQuery();
    return commands.hmset(RedisKeys.user(cb.from().id()), singletonMap("gender", gender.toString()))
        .thenReturn(new SendMessage(cb.message().chat().id(), String.format("You are %s, understood", gender.toString())))
        .map(bot::execute)
        .then();
  }

  public Mono<Void> setMatchMinAge(Update update) {
    Message message = update.message();
    String age = message.text();
    return commands.hmset(RedisKeys.user(message.from().id()), singletonMap("matchMinAge", age))
        .thenReturn(new SendMessage(message.chat().id(), String.format("Okay, match mix age is %s", age)))
        .map(bot::execute)
        .then();
  }

  public Mono<Void> setMatchMaxAge(Update update) {
    Message message = update.message();
    String age = message.text();
    return commands.hmset(RedisKeys.user(message.from().id()), singletonMap("matchMaxAge", age))
        .thenReturn(new SendMessage(message.chat().id(), String.format("Set match max age to %s", age)))
        .map(bot::execute)
        .then();
  }
}
