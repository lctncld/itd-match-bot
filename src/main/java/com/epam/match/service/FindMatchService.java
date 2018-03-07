package com.epam.match.service;

import com.epam.match.RedisKeys;
import com.epam.match.domain.Match;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@Service
public class FindMatchService {

  private final LocationService locationService;

  private final RedisReactiveCommands<String, String> commands;

  public FindMatchService(LocationService locationService, RedisReactiveCommands<String, String> commands) {
    this.locationService = locationService;
    this.commands = commands;
  }

  public Mono<Match> next(Integer userId) {
    return locationService.get(userId.toString())
      .filterWhen(matchId -> commands.sismember(RedisKeys.likes(userId), matchId)
        .map(negate())
      )
      .filterWhen(matchId -> commands.sismember(RedisKeys.dislikes(userId), matchId)
        .map(negate())
      )
      .doOnNext(matchId -> log.info("Match for {} is {}", userId, matchId))
      .take(1)
      .flatMap(matchId -> Flux.zip(
        Mono.just(matchId),
        commands.get(RedisKeys.image(matchId)),
        commands.hget(RedisKeys.contact(matchId), "first_name")
      ))
      .map(tuple -> Match.builder()
        .id(tuple.getT1())
        .image(tuple.getT2())
        .name(tuple.getT3())
        .build()
      )
      .next();
  }

  private Function<Boolean, Boolean> negate() {
    return exists -> !exists;
  }
}
