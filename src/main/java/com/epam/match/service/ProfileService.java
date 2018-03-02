package com.epam.match.service;

import com.epam.match.RedisKeys;
import com.epam.match.domain.Gender;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static java.util.Collections.singletonMap;

@Service
public class ProfileService {

  private final TelegramBot bot;

  private final RedisReactiveCommands<String, String> commands;

  private final LocationService locationService;

  public ProfileService(TelegramBot bot, RedisReactiveCommands<String, String> commands,
      LocationService locationService) {
    this.bot = bot;
    this.commands = commands;
    this.locationService = locationService;
  }

  private SendMessage profileMenu(Long chatId, String message) {
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
    return new SendMessage(chatId, message)
        .replyMarkup(actions);
  }

  public Mono<Void> setupProfile(Update update) {
    CallbackQuery cb = update.callbackQuery();
    Long chatId = cb.message().chat().id();
    return commands.hgetall(RedisKeys.user(cb.from().id())) //FIXME unchecked assignment
        .map(profile -> {
          String message = profile.isEmpty()
              ? "Your profile appears to be blank, tap these buttons to fill it!"
              : "So, your settings are:\n" + profile.entrySet().stream()
                  .map(entry -> entry.getKey() + ": " + entry.getValue())
                  .collect(Collectors.joining(", "));
          return profileMenu(chatId, message);
        })
        .cast(BaseRequest.class)
        .concatWith(Mono.just(new AnswerCallbackQuery(cb.id())))
        .map(bot::execute)
        .then();
  }

  public Mono<Void> setAge(Update update) {
    Message message = update.message();
    String age = message.text();
    Long chatId = message.chat().id();
    return commands.hmset(RedisKeys.user(message.from().id()), singletonMap("age", age))
        .thenMany(Flux.just(
            new SendMessage(chatId, String.format("Okay, your age is %s", age)),
            profileMenu(chatId, "Anything else?")
        )).map(bot::execute)
        .then();
  }

  public Mono<Void> setLocation(Update update) {
    Message message = update.message();
    Long chatId = message.chat().id();
    return locationService.set(message.from().id().toString(), message.location())
        .thenMany(Flux.just(
            new SendMessage(chatId, "Your location is updated"),
            profileMenu(chatId, "Anything else?")
        )).map(bot::execute)
        .then();
  }

  public Mono<Void> setMatchGender(Update update, Gender gender) {
    CallbackQuery cb = update.callbackQuery();
    Long chatId = cb.message().chat().id();
    return commands.hmset(RedisKeys.user(cb.from().id()), singletonMap("matchGender", gender.toString()))
        .thenMany(Flux.just(
            new AnswerCallbackQuery(cb.id()),
            new SendMessage(chatId, "Now looking for " + gender.toString()),
            profileMenu(chatId, "Anything else?")
        )).map(bot::execute)
        .then();

  }

  public Mono<Void> setGender(Update update, Gender gender) {
    CallbackQuery cb = update.callbackQuery();
    Long chatId = cb.message().chat().id();
    return commands.hmset(RedisKeys.user(cb.from().id()), singletonMap("gender", gender.toString()))
        .thenMany(Flux.just(
            new AnswerCallbackQuery(cb.id()),
            new SendMessage(chatId, String.format("You are %s, understood", gender.toString())),
            profileMenu(chatId, "Anything else?")
        )).map(bot::execute)
        .then();
  }

  public Mono<Void> setMatchMinAge(Update update) {
    Message message = update.message();
    String age = message.text();
    Long chatId = message.chat().id();
    return commands.hmset(RedisKeys.user(message.from().id()), singletonMap("matchMinAge", age))
        .thenMany(Flux.just(
            new SendMessage(chatId, String.format("Okay, match min age is %s", age)),
            profileMenu(chatId, "Anything else?")
        )).map(bot::execute)
        .then();
  }

  public Mono<Void> setMatchMaxAge(Update update) {
    Message message = update.message();
    String age = message.text();
    Long chatId = message.chat().id();
    return commands.hmset(RedisKeys.user(message.from().id()), singletonMap("matchMaxAge", age))
        .thenMany(Flux.just(
            new SendMessage(chatId, String.format("Set match max age to %s", age)),
            profileMenu(chatId, "Anything else?")
        )).map(bot::execute)
        .then();
  }
}
