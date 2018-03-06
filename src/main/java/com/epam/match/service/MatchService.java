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
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.Map;
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
        String photoId = tuple.getT2();
        String name = tuple.getT3();
        return new SendPhoto(chatId, photoId)
          .caption(name)
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

  private Flux<Tuple3<String, String, String>> nextMatch(Integer userId) {
    return locationService.get(userId.toString())
      .filterWhen(matchId -> commands.sismember(RedisKeys.likes(userId), matchId)
        .map(negate()))
      .filterWhen(matchId -> commands.sismember(RedisKeys.dislikes(userId), matchId)
        .map(negate()))
      .doOnNext(matchId -> log.info("Match for {} is {}", userId, matchId))
      .take(1)
      .flatMap(matchId -> Flux.zip(
        Mono.just(matchId),
        commands.get(RedisKeys.image(matchId)),
        commands.hget(RedisKeys.contact(matchId), "first_name")
      ));
  }

  private Function<Boolean, Boolean> negate() {
    return exists -> !exists;
  }

  public Mono<Void> like(Update update) {
    CallbackQuery cb = update.callbackQuery();
    String matchId = getMatchIdFromCommand(cb.data());
    Long chatId = cb.message().chat().id();
    Integer myId = cb.from().id();
    return commands.sadd(RedisKeys.likes(myId), matchId)
      .thenMany(Flux.just(
        new AnswerCallbackQuery(cb.id()),
        new EditMessageReplyMarkup(chatId, cb.message().messageId())
          .replyMarkup(new InlineKeyboardMarkup())
      ))
      .map(bot::execute)
      .then(commands.sismember(RedisKeys.likes(matchId), myId.toString()))
      .filter(Boolean::valueOf)
      .flatMapMany(done -> Flux.zip(
        commands.hgetall(RedisKeys.contact(matchId)),
        commands.hgetall(RedisKeys.contact(myId))
      ))
      .flatMap(tuple -> {
        Map<String, String> match = tuple.getT1();
        Map<String, String> me = tuple.getT2();
        return Flux.just(
          new SendContact(chatId, match.get("phone"), match.get("first_name")).lastName(match.get("last_name")),
          new SendMessage(match.get("chat_id"), String.format("Hey, %s is interested in you!", me.get("first_name"))),
          new SendContact(match.get("chat_id"), me.get("phone"), me.get("first_name")).lastName(me.get("last_name"))
        );
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
