package com.epam.match.service;

import com.pengrad.telegrambot.model.Location;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class LocationService {

  private final RedisReactiveCommands<String, String> commands;

  public LocationService(RedisReactiveCommands<String, String> commands) {
    this.commands = commands;
  }

  public Mono<Void> set(String userId, Location location) {
    return commands.geoadd(
        "locations",
        location.latitude(),
        location.longitude(),
        userId
    ).then();
  }
}
