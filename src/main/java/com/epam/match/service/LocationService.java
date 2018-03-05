package com.epam.match.service;

import com.epam.match.RedisKeys;
import com.pengrad.telegrambot.model.Location;
import io.lettuce.core.GeoArgs;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class LocationService {

  private final Logger log = LoggerFactory.getLogger(LocationService.class);

  private final RedisReactiveCommands<String, String> commands;

  public LocationService(RedisReactiveCommands<String, String> commands) {
    this.commands = commands;
  }

  public Mono<Void> set(String userId, Location location) {
    if (location == null) {
      return Mono.error(new RuntimeException("Location is null"));
    }
    return commands.geoadd(
        RedisKeys.locations(),
        location.latitude(),
        location.longitude(),
        userId
    ).then();
  }

  public Flux<String> get(String userId) {
    return commands.georadiusbymember(
        RedisKeys.locations(),
        userId,
        10,
        GeoArgs.Unit.km
    )
        .filter(user -> !user.equals(userId))
        .doOnNext(user -> log.info("Found user {} near {}", user, userId));

  }

}
