package com.epam.match.service;

import com.epam.match.Repository;
import com.epam.match.domain.Contact;
import com.pengrad.telegrambot.TelegramBot;
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
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MatchService {

  private final FindMatchService findMatchService;

  private final Repository repository;

  private final TelegramBot bot;

  public MatchService(FindMatchService findMatchService, Repository repository, TelegramBot bot) {
    this.findMatchService = findMatchService;
    this.repository = repository;
    this.bot = bot;
  }

  public Mono<Void> suggest(Update update) {
    Message message = update.message();
    return suggest(message.chat().id(), message.from().id());
  }

  public Mono<Void> suggestFromCallback(Update update) {
    CallbackQuery cb = update.callbackQuery();
    return suggest(cb.message().chat().id(), cb.from().id());
  }

  @SuppressWarnings("unchecked") // FIXME: Vasya, you can fix this
  private Mono<Void> suggest(Long chatId, Integer userId) {
    return findMatchService.next(userId)
      .map(match -> new SendPhoto(chatId, match.getImage())
        .caption(match.getName())
        .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[] {
          new InlineKeyboardButton("+").callbackData("/like/" + match.getId()),
          new InlineKeyboardButton("-").callbackData("/dislike/" + match.getId())
        }))
      )
      .cast(BaseRequest.class)
      .defaultIfEmpty(new SendMessage(chatId, "Sorry, no one new is around"))
      .map(bot::execute)
      .then();
  }

  public Mono<Void> like(Update update) {
    CallbackQuery cb = update.callbackQuery();
    String matchId = getMatchIdFromCommand(cb.data());
    Long chatId = cb.message().chat().id();
    Integer myId = cb.from().id();
    return repository.like(myId.toString(), matchId)
      .thenMany(Flux.just(
        new AnswerCallbackQuery(cb.id()),
        new EditMessageReplyMarkup(chatId, cb.message().messageId())
          .replyMarkup(new InlineKeyboardMarkup())
      ))
      .map(bot::execute)
      .then(shareContacts(myId.toString(), matchId))
      .then(suggestFromCallback(update));
  }

  private Mono<Void> shareContacts(String myId, String matchId) {
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
      })
      .map(bot::execute)
      .then();
  }

  private String getMatchIdFromCommand(String command) {
    String[] parts = command.split("/");
    return parts[parts.length - 1];
  }


  public Mono<Void> dislike(Update update) {
    CallbackQuery cb = update.callbackQuery();
    String matchId = getMatchIdFromCommand(cb.data());
    return repository.dislike(cb.from().id().toString(), matchId)
      .thenMany(Flux.just(
        new AnswerCallbackQuery(cb.id()),
        new EditMessageReplyMarkup(cb.message().chat().id(), cb.message().messageId())
          .replyMarkup(new InlineKeyboardMarkup())
      ))
      .map(bot::execute)
      .then(suggestFromCallback(update));
  }
}
