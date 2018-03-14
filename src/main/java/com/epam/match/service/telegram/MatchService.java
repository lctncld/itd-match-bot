package com.epam.match.service.telegram;

import com.epam.match.domain.Contact;
import com.epam.match.repository.Repository;
import com.epam.match.service.match.FindMatchService;
import com.epam.match.spring.annotation.MessageMapping;
import com.epam.match.spring.annotation.TelegramBotController;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import com.pengrad.telegrambot.request.SendContact;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@TelegramBotController
public class MatchService {

  private final FindMatchService findMatchService;

  private final Repository repository;

  public MatchService(FindMatchService findMatchService, Repository repository) {
    this.findMatchService = findMatchService;
    this.repository = repository;
  }

  @MessageMapping("/roll")
  public Mono<? extends BaseRequest> suggest(Update update) {
    Message message = update.message();
    return suggest(message.chat().id(), message.from().id());
  }

  private Mono<? extends BaseRequest> suggestFromCallback(Update update) {
    CallbackQuery cb = update.callbackQuery();
    return suggest(cb.message().chat().id(), cb.from().id());
  }

  @SuppressWarnings("unchecked") // FIXME: Vasya, you can fix this
  private Mono<? extends BaseRequest> suggest(Long chatId, Integer userId) {
    return findMatchService.next(userId)
      .map(match -> new SendPhoto(chatId, match.getImage())
        .caption(match.getName())
        .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[] {
          new InlineKeyboardButton("+").callbackData("/like/" + match.getId()),
          new InlineKeyboardButton("-").callbackData("/dislike/" + match.getId())
        }))
      )
      .cast(BaseRequest.class)
      .defaultIfEmpty(new SendMessage(chatId, "Sorry, no one new is around"));
  }

  @MessageMapping("/like/")
  public Flux<? extends BaseRequest> like(Update update) {
    CallbackQuery cb = update.callbackQuery();
    String matchId = getMatchIdFromCommand(cb.data());
    Long chatId = cb.message().chat().id();
    Integer myId = cb.from().id();
    return repository.like(myId.toString(), matchId)
      .thenMany(
        Flux.just(
          new AnswerCallbackQuery(cb.id()),
          new EditMessageReplyMarkup(chatId, cb.message().messageId())
            .replyMarkup(new InlineKeyboardMarkup())
        )
          .cast(BaseRequest.class)
          .concatWith(shareContacts(myId.toString(), matchId))
          .concatWith(suggestFromCallback(update))
      );
  }

  private Flux<? extends BaseRequest> shareContacts(String myId, String matchId) {
    return repository.isLikedBy(myId, matchId)
      .filter(Boolean::valueOf)
      .flatMapMany(done -> Flux.zip(
        repository.getContact(matchId),
        repository.getContact(myId)
      ))
      .flatMap(tuple -> {
        Contact match = tuple.getT1();
        Contact me = tuple.getT2();
        return Flux.just(
          new SendContact(me.getChatId(), match.getPhone(), match.getFirstName()).lastName(match.getLastName()),
          new SendMessage(match.getChatId(), String.format("Hey, %s is interested in you!", me.getFirstName())),
          new SendContact(match.getChatId(), me.getPhone(), me.getFirstName()).lastName(me.getLastName())
        );
      });
  }

  private String getMatchIdFromCommand(String command) {
    String[] parts = command.split("/");
    return parts[parts.length - 1];
  }

  @MessageMapping("/dislike/")
  public Flux<? extends BaseRequest> dislike(Update update) {
    CallbackQuery cb = update.callbackQuery();
    String matchId = getMatchIdFromCommand(cb.data());
    return repository.dislike(cb.from().id().toString(), matchId)
      .thenMany(Flux.just(
        new AnswerCallbackQuery(cb.id()),
        new EditMessageReplyMarkup(cb.message().chat().id(), cb.message().messageId())
          .replyMarkup(new InlineKeyboardMarkup())
      ))
      .cast(BaseRequest.class)
      .concatWith(suggestFromCallback(update));
  }
}
