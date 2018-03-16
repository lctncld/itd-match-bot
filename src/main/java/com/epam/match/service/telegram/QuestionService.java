package com.epam.match.service.telegram;

import com.epam.match.EmojiResolver;
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

  public QuestionService(SessionService session) {
    this.session = session;
  }

  @MessageMapping("/profile/me/gender")
  public Flux<? extends BaseRequest> askGender(Update update) {
    CallbackQuery cb = update.callbackQuery();
    Long chatId = cb.message().chat().id();
    return Flux.just(
      new AnswerCallbackQuery(cb.id()),
      new SendMessage(chatId, "Who are you?")
        .replyMarkup(new InlineKeyboardMarkup(
          new InlineKeyboardButton[] {
            new InlineKeyboardButton(EmojiResolver.man())
              .callbackData("/profile/me/gender/male"),
            new InlineKeyboardButton(EmojiResolver.woman())
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
        new SendMessage(chatId, "So, what's your age?"),
        new DeleteMessage(chatId, cb.message().messageId())
      ));
  }

  @MessageMapping("/profile/match/gender")
  public Flux<? extends BaseRequest> askMatchGender(Update update) {
    CallbackQuery cb = update.callbackQuery();
    Long chatId = cb.message().chat().id();
    return Flux.just(
      new AnswerCallbackQuery(cb.id()),
      new SendMessage(chatId, "Who do you wanna find?")
        .replyMarkup(new InlineKeyboardMarkup(
          new InlineKeyboardButton[] {
            new InlineKeyboardButton(EmojiResolver.man())
              .callbackData("/profile/match/gender/male"),
            new InlineKeyboardButton(EmojiResolver.woman())
              .callbackData("/profile/match/gender/female"),
            new InlineKeyboardButton(EmojiResolver.both())
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
        new SendMessage(chatId, "Send me min age"),
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
        new SendMessage(chatId, "And the max age is?"),
        new DeleteMessage(chatId, cb.message().messageId())
      ));
  }
}
