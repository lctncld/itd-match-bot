package com.epam.match.service.telegram;

import com.epam.match.spring.MessageSourceAdapter;
import com.epam.match.service.session.ProfileSetupStep;
import com.epam.match.service.session.SessionService;
import com.epam.match.spring.annotation.MessageMapping;
import com.epam.match.spring.annotation.TelegramBotController;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import reactor.core.publisher.Flux;

@TelegramBotController
public class QuestionService {

  private final SessionService session;

  private final MessageSourceAdapter messageSource;

  public QuestionService(SessionService session, MessageSourceAdapter messageSource) {
    this.session = session;
    this.messageSource = messageSource;
  }

  @MessageMapping("/profile/me/gender")
  public Flux<? extends BaseRequest> askGender(Update update) {
    CallbackQuery cb = update.callbackQuery();
    Long chatId = cb.message().chat().id();
    return Flux.just(
      new AnswerCallbackQuery(cb.id()),
      new SendMessage(chatId, messageSource.get("question.set.my.gender"))
        .replyMarkup(new InlineKeyboardMarkup(
          new InlineKeyboardButton[] {
            new InlineKeyboardButton(messageSource.get("gender.male"))
              .callbackData("/profile/me/gender/male"),
            new InlineKeyboardButton(messageSource.get("gender.female"))
              .callbackData("/profile/me/gender/female")
          }
        )),
      new DeleteMessage(chatId, cb.message().messageId())
    );
  }

  @MessageMapping("/profile/me/age")
  public Flux<? extends BaseRequest> askAge(Update update) {
    CallbackQuery cb = update.callbackQuery();
    Long chatId = cb.message().chat().id();
    return session.set(cb.from().id(), ProfileSetupStep.SET_MY_AGE)
      .thenMany(Flux.just(
        new AnswerCallbackQuery(cb.id()),
        new SendMessage(chatId, messageSource.get("question.set.my.age")),
        new DeleteMessage(chatId, cb.message().messageId())
      ));
  }

  @MessageMapping("/profile/match/gender")
  public Flux<? extends BaseRequest> askMatchGender(Update update) {
    CallbackQuery cb = update.callbackQuery();
    Long chatId = cb.message().chat().id();
    return Flux.just(
      new AnswerCallbackQuery(cb.id()),
      new SendMessage(chatId, messageSource.get("question.set.match.gender"))
        .replyMarkup(new InlineKeyboardMarkup(
          new InlineKeyboardButton[] {
            new InlineKeyboardButton(messageSource.get("gender.male"))
              .callbackData("/profile/match/gender/male"),
            new InlineKeyboardButton(messageSource.get("gender.female"))
              .callbackData("/profile/match/gender/female"),
            new InlineKeyboardButton(messageSource.get("gender.both"))
              .callbackData("/profile/match/gender/both")
          })
        ),
      new DeleteMessage(chatId, cb.message().messageId())
    );
  }

  @MessageMapping("/profile/match/age/min")
  public Flux<? extends BaseRequest> askMatchMinAge(Update update) {
    CallbackQuery cb = update.callbackQuery();
    Long chatId = cb.message().chat().id();
    return session.set(cb.from().id(), ProfileSetupStep.SET_MATCH_MIN_AGE)
      .thenMany(Flux.just(
        new AnswerCallbackQuery(cb.id()),
        new SendMessage(chatId, messageSource.get("question.set.match.age.from")),
        new DeleteMessage(chatId, cb.message().messageId())
      ));
  }

  @MessageMapping("/profile/match/age/max")
  public Flux<? extends BaseRequest> askMatchMaxAge(Update update) {
    CallbackQuery cb = update.callbackQuery();
    Long chatId = cb.message().chat().id();
    return session.set(cb.from().id(), ProfileSetupStep.SET_MATCH_MAX_AGE)
      .thenMany(Flux.just(
        new AnswerCallbackQuery(cb.id()),
        new SendMessage(chatId, messageSource.get("question.set.match.age.to")),
        new DeleteMessage(chatId, cb.message().messageId())
      ));
  }
}
