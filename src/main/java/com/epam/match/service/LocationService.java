package com.epam.match.service;

import com.epam.match.RedisKeys;
import com.pengrad.telegrambot.model.Location;
import io.lettuce.core.GeoArgs;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class LocationService {

  private final RedisReactiveCommands<String, String> commands;

  public LocationService(RedisReactiveCommands<String, String> commands) {
    this.commands = commands;
  }

  public Mono<Void> set(String userId, Location location) {
    Float latitude = location.latitude();
    Float longitude = location.longitude();
    return commands.geoadd(
        RedisKeys.locations(),
        latitude,
        longitude,
        userId
    ).thenMany(commands.georadiusbymember(
        RedisKeys.locations(),
        userId,
        10,
        GeoArgs.Unit.km
    )).filter(user -> !user.equals(userId))
        .doOnNext(user -> {
          log.info("Found user {} near {}", user, userId);
          // TODO: Notify them
        })
        .then();
  }
}
