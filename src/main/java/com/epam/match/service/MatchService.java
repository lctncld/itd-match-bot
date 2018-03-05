package com.epam.match.service;

import com.epam.match.RedisKeys;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.function.Function;

@Slf4j
@Service
public class MatchService {

  private final RedisReactiveCommands<String, String> commands;

  private final LocationService locationService;

  private final TelegramBot bot;

  public MatchService(RedisReactiveCommands<String, String> commands, LocationService locationService,
      TelegramBot bot) {
    this.commands = commands;
    this.locationService = locationService;
    this.bot = bot;
  }

  @SuppressWarnings("unchecked") // FIXME: Vasya, you can fix this
  public Mono<Void> next(Update update) {
    Message message = update.message();
    Integer userId = message.from().id();

    return locationService.get(userId.toString())
        .filterWhen(matchId -> commands.sismember(RedisKeys.likes(userId), matchId)
            .map(negate()))
        .filterWhen(matchId -> commands.sismember(RedisKeys.dislikes(userId), matchId)
            .map(negate()))
        .doOnNext(matchId -> log.info("Match for {} is {}", userId, matchId))
        .take(1)
        .flatMap(matchId -> commands.get(RedisKeys.image(matchId))
            .map(imageId -> Tuples.of(matchId, imageId))
        )
        .map(tuple -> {
              String matchId = tuple.getT1();
              return new SendPhoto(message.chat().id(), tuple.getT2())
                  .caption("Description goes here?") //TODO
                  .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[] {
                      new InlineKeyboardButton("+").callbackData("/like/" + matchId),
                      new InlineKeyboardButton("-").callbackData("/dislike/" + matchId)
                  }));
            }
        )
        .cast(BaseRequest.class)
        .defaultIfEmpty(new SendMessage(message.chat().id(), "Sorry, no one new is around"))
        .map(bot::execute)
        .then();
  }

  private Function<Boolean, Boolean> negate() {
    return exists -> !exists;
  }

  public Mono<Void> like(Update update) {
    CallbackQuery cb = update.callbackQuery();
    String matchId = getMatchIdFromCommand(cb.data());
    return commands.sadd(RedisKeys.likes(cb.from().id()), matchId)
        .thenMany(Flux.just(
            new AnswerCallbackQuery(cb.id()),
            new EditMessageReplyMarkup(cb.message().chat().id(), cb.message().messageId())
                .replyMarkup(new InlineKeyboardMarkup())
        ))
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
    return commands.sadd(RedisKeys.dislikes(cb.from().id()), matchId)
        .thenMany(Flux.just(
            new AnswerCallbackQuery(cb.id()),
            new EditMessageReplyMarkup(cb.message().chat().id(), cb.message().messageId())
                .replyMarkup(new InlineKeyboardMarkup())
        ))
        .map(bot::execute)
        .then();
  }
}
