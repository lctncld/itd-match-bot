package com.epam.match.service.telegram;

import com.epam.match.spring.MessageSourceAdapter;
import com.epam.match.domain.Contact;
import com.epam.match.domain.Match;
import com.epam.match.service.match.FindMatchService;
import com.epam.match.service.store.PersistentStore;
import com.epam.match.spring.annotation.MessageMapping;
import com.epam.match.spring.annotation.TelegramBotController;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@TelegramBotController
public class MatchService {

  private final FindMatchService findMatchService;

  private final PersistentStore store;

  private final MessageSourceAdapter messageSource;

  public MatchService(FindMatchService findMatchService, PersistentStore store,
    MessageSourceAdapter messageSource) {
    this.findMatchService = findMatchService;
    this.store = store;
    this.messageSource = messageSource;
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
      .map(matchToPhotoCard(chatId))
      .cast(BaseRequest.class)
      .defaultIfEmpty(new SendMessage(chatId, messageSource.get("match.no.one.is.around")))
      .onErrorReturn(new SendMessage(chatId, messageSource.get("match.your.location.not.shared")));
  }

  private Function<Match, SendPhoto> matchToPhotoCard(Long chatId) {
    return match -> new SendPhoto(chatId, match.getImage())
      .caption(match.getName())
      .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[] {
        new InlineKeyboardButton(messageSource.get("match.button.like")).callbackData("/like/" + match.getId()),
        new InlineKeyboardButton(messageSource.get("match.button.dislike")).callbackData("/dislike/" + match.getId())
      }));
  }

  @MessageMapping("/like/")
  public Flux<? extends BaseRequest> like(Update update) {
    CallbackQuery cb = update.callbackQuery();
    String matchId = getMatchIdFromCommand(cb.data());
    Long chatId = cb.message().chat().id();
    Integer myId = cb.from().id();
    return store.like(myId.toString(), matchId)
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
    return store.isLikedBy(myId, matchId)
      .filter(Boolean::valueOf)
      .flatMapMany(done -> Flux.zip(
        store.getContact(matchId),
        store.getContact(myId)
      ))
      .flatMap(tuple -> {
        Contact match = tuple.getT1();
        Contact me = tuple.getT2();
        return Flux.just(
          new SendContact(me.getChatId(), match.getPhone(), match.getFirstName()).lastName(match.getLastName()),
          new SendMessage(match.getChatId(), messageSource.get("match.on.mutual.like", me.getFirstName())),
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
    return store.dislike(cb.from().id().toString(), matchId)
      .thenMany(Flux.just(
        new AnswerCallbackQuery(cb.id()),
        new EditMessageReplyMarkup(cb.message().chat().id(), cb.message().messageId())
          .replyMarkup(new InlineKeyboardMarkup())
      ))
      .cast(BaseRequest.class)
      .concatWith(suggestFromCallback(update));
  }

  @MessageMapping("/undo")
  public Mono<? extends BaseRequest> undo(Update update) {
    Message message = update.message();
    Long chatId = message.chat().id();
    return store.undo(message.from().id().toString())
      .flatMap(store::getMatchById)
      .map(matchToPhotoCard(chatId))
      .cast(BaseRequest.class)
      .defaultIfEmpty(new SendMessage(chatId, messageSource.get("match.nothing.to.undo")));
  }
}
