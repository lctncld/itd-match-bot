package com.epam.match.service;

import com.epam.match.Steps;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SessionService {

  private final RedisReactiveCommands<String, String> commands;

  public SessionService(RedisReactiveCommands<String, String> commands) {
    this.commands = commands;
  }

  public Mono<Steps> get(String userId) {
    return commands.hget("steps", userId)
        .switchIfEmpty(Mono.just(Steps.UNKNOWN.toString()))
        .map(string -> {
          try {
            return Steps.valueOf(string);
          } catch (IllegalArgumentException ex) {
            return Steps.UNKNOWN; // FIXME: handle with reactive operator
          }
        });
  }

  public Mono<Void> set(String userId, Steps step) {
    return commands.hset("steps", userId, step.toString())
        .then();
  }

  public Mono<Void> clear(String userId) {
    return commands.hdel("steps", userId)
        .then();
  }
}
