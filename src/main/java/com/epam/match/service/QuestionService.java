package com.epam.match.service;

import com.epam.match.session.Step;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class QuestionService {

  private final TelegramBot bot;

  private final SessionService session;

  public QuestionService(TelegramBot bot, SessionService session) {
    this.bot = bot;
    this.session = session;
  }

  public Mono<Void> askGender(Update update) {
    CallbackQuery cb = update.callbackQuery();
    return Mono.just(
        new SendMessage(cb.message().chat().id(), "Who are you?")
            .replyMarkup(new InlineKeyboardMarkup(
                new InlineKeyboardButton[] {
                    new InlineKeyboardButton("M")
                        .callbackData("/profile/me/gender/male"),
                    new InlineKeyboardButton("F")
                        .callbackData("/profile/me/gender/female")
                }
            ))
    ).map(bot::execute)
        .then();
  }

  public Mono<Void> askAge(Update update) {
    CallbackQuery callbackQuery = update.callbackQuery();
    return session.set(callbackQuery.from().id().toString(), Step.SET_MY_AGE)
        .thenReturn(new SendMessage(callbackQuery.message().chat().id(), "So, what's your age?"))
        .map(bot::execute)
        .then();
  }

  public Mono<Void> askMatchGender(Update update) {
    CallbackQuery cb = update.callbackQuery();
    return Mono.just(
        new SendMessage(cb.message().chat().id(), "Who do you wanna find?")
            .replyMarkup(new InlineKeyboardMarkup(
                new InlineKeyboardButton[] {
                    new InlineKeyboardButton("M")
                        .callbackData("/profile/match/gender/male"),
                    new InlineKeyboardButton("F")
                        .callbackData("/profile/match/gender/female"),
                    new InlineKeyboardButton("Both")
                        .callbackData("/profile/match/gender/both")
                })
            )
    ).map(bot::execute)
        .then();
  }

  public Mono<Void> askMatchMinAge(Update update) {
    CallbackQuery cb = update.callbackQuery();
    String user = cb.from().id().toString();
    return session.set(user, Step.SET_MATCH_MIN_AGE)
        .thenReturn(new SendMessage(cb.message().chat().id(), "Send me min age"))
        .map(bot::execute)
        .then();
  }

  public Mono<Void> askMatchMaxAge(Update update) {
    CallbackQuery cb = update.callbackQuery();
    String user = cb.from().id().toString();
    return session.set(user, Step.SET_MATCH_MAX_AGE)
        .thenReturn(new SendMessage(cb.message().chat().id(), "And the max age is?"))
        .map(bot::execute)
        .then();
  }
}
