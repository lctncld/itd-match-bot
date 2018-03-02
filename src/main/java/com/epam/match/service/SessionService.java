package com.epam.match.service;

import com.epam.match.session.Step;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SessionService {

  private static final String KEY = "session";

  private final RedisReactiveCommands<String, String> commands;

  public SessionService(RedisReactiveCommands<String, String> commands) {
    this.commands = commands;
  }

  public Mono<Step> get(String userId) {
    return commands.hget(KEY, userId)
        .switchIfEmpty(Mono.just(Step.UNKNOWN.toString()))
        .map(string -> {
          try {
            return Step.valueOf(string);
          } catch (IllegalArgumentException ex) {
            return Step.UNKNOWN; // FIXME: handle with reactive operator
          }
        });
  }

  public Mono<Void> set(String userId, Step step) {
    return commands.hset(KEY, userId, step.toString())
        .then();
  }

  public Mono<Void> clear(String userId) {
    return commands.hdel(KEY, userId)
        .then();
  }
}
