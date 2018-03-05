package com.epam.match.service;

import com.epam.match.RedisKeys;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendPhoto;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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

  public Mono<Void> next(Update update) {
    Message message = update.message();
    Integer userId = message.from().id();

    return locationService.get(userId.toString())
        .filterWhen(matchId -> commands.sismember(RedisKeys.likes(userId), matchId)
            .map(negate()))
        .filterWhen(matchId -> commands.sismember(RedisKeys.dislikes(userId), matchId)
            .map(negate()))
        .doOnNext(matchId -> log.info("Match: {}", matchId))
        .single()
        .flatMap(matchId -> commands.get(RedisKeys.image(matchId)))
        .map(imageId -> new SendPhoto(message.chat().id(), imageId))
        .map(bot::execute)
        .then();
  }

  private Function<Boolean, Boolean> negate() {
    return exists -> !exists;
  }
}
