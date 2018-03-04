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

  private final NotificationService notificationService;

  public LocationService(RedisReactiveCommands<String, String> commands, NotificationService notificationService) {
    this.commands = commands;
    this.notificationService = notificationService;
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
    ).thenMany(commands.georadiusbymember(
        RedisKeys.locations(),
        userId,
        10,
        GeoArgs.Unit.km
    )).filter(user -> user.equals(userId))
        .doOnNext(user -> log.info("Found user {} near {}", user, userId))
        .map(user -> notificationService.notify(Integer.valueOf(user), Integer.valueOf(userId)).subscribe()) //TODO ?
        .then();
  }


}
