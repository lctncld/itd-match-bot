package com.epam.match.service;

import com.epam.match.RedisKeys;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.*;
import io.lettuce.core.KeyValue;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.function.Function;

@Service
public class MatchService {

  private final Logger log = LoggerFactory.getLogger(MatchService.class);

  private final RedisReactiveCommands<String, String> commands;

  private final LocationService locationService;

  private final TelegramBot bot;

  public MatchService(RedisReactiveCommands<String, String> commands, LocationService locationService,
    TelegramBot bot) {
    this.commands = commands;
    this.locationService = locationService;
    this.bot = bot;
  }

  public Mono<Void> suggest(Update update) {
    Message message = update.message();
    return suggest(message.chat().id(), message.from().id());
  }

  @SuppressWarnings("unchecked") // FIXME: Vasya, you can fix this
  private Mono<Void> suggest(Long chatId, Integer userId) {
    return nextMatch(userId)
      .map(tuple -> {
        String matchId = tuple.getT1();
        return new SendPhoto(chatId, tuple.getT2())
          .caption("Description goes here?") //TODO
          .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[] {
            new InlineKeyboardButton("+").callbackData("/like/" + matchId),
            new InlineKeyboardButton("-").callbackData("/dislike/" + matchId)
          }));
      })
      .cast(BaseRequest.class)
      .defaultIfEmpty(new SendMessage(chatId, "Sorry, no one new is around"))
      .map(bot::execute)
      .then();
  }

  private Flux<Tuple2<String, String>> nextMatch(Integer userId) {
    return locationService.get(userId.toString())
      .filterWhen(matchId -> commands.sismember(RedisKeys.likes(userId), matchId)
        .map(negate()))
      .filterWhen(matchId -> commands.sismember(RedisKeys.dislikes(userId), matchId)
        .map(negate()))
      .doOnNext(matchId -> log.info("Match for {} is {}", userId, matchId))
      .take(1)
      .flatMap(matchId -> commands.get(RedisKeys.image(matchId))
        .map(imageId -> Tuples.of(matchId, imageId)));
  }

  private Function<Boolean, Boolean> negate() {
    return exists -> !exists;
  }

  public Mono<Void> like(Update update) {
    CallbackQuery cb = update.callbackQuery();
    String matchId = getMatchIdFromCommand(cb.data());
    Long chatId = cb.message().chat().id();
    return commands.sadd(RedisKeys.likes(cb.from().id()), matchId)
      .thenMany(Flux.just(
        new AnswerCallbackQuery(cb.id()),
        new EditMessageReplyMarkup(chatId, cb.message().messageId())
          .replyMarkup(new InlineKeyboardMarkup())
      ))
      .map(bot::execute)
      .then(commands.sismember(RedisKeys.likes(matchId), cb.from().id().toString()))
      .filter(Boolean::valueOf)
      .flatMap(done -> commands.mget(RedisKeys.contact(matchId))
        .collectMap(KeyValue::getKey, KeyValue::getValue)
      )
      .map(details -> { // TODO: flatMapMany contact to other party too
        String phone = RedisKeys.Contact.phone(matchId);
        String firstName = RedisKeys.Contact.firstName(matchId);
        String lastName = RedisKeys.Contact.lastName(matchId);
        return new SendContact(chatId, details.get(phone), details.get(firstName)).lastName(details.get(lastName));
      })
      .map(bot::execute)
      .then(suggestFromCallback(update))
      .then();
  }

  private String getMatchIdFromCommand(String command) {
    String[] parts = command.split("/");
    return parts[parts.length - 1];
  }

  private Mono<Void> suggestFromCallback(Update update) {
    CallbackQuery cb = update.callbackQuery();
    return suggest(cb.message().chat().id(), cb.from().id());
  }

  public Mono<Void> dislike(Update update) {
    CallbackQuery cb = update.callbackQuery();
    String matchId = getMatchIdFromCommand(cb.data());
    return commands.sadd(RedisKeys.dislikes(cb.from().id()), matchId)
      .thenMany(Flux.just(
        new AnswerCallbackQuery(cb.id()),
        new EditMessageReplyMarkup(cb.message().chat().id(), cb.message().messageId())
          .replyMarkup(new InlineKeyboardMarkup())
      ))
      .map(bot::execute)
      .then(suggestFromCallback(update));
  }
}
