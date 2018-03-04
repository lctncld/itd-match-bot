package com.epam.match.service;

import com.epam.match.RedisKeys;
import com.epam.match.session.Step;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SessionService {

  private final RedisReactiveCommands<String, String> commands;

  public SessionService(RedisReactiveCommands<String, String> commands) {
    this.commands = commands;
  }

  public Mono<Step> get(Integer userId) {
    return commands.get(RedisKeys.session(userId))
        .switchIfEmpty(Mono.just(Step.UNKNOWN.toString()))
        .map(string -> {
          try {
            return Step.valueOf(string);
          } catch (IllegalArgumentException ex) {
            return Step.UNKNOWN; // FIXME: handle with reactive operator
          }
        });
  }

  public Mono<Void> set(Integer userId, Step step) {
    return commands.set(RedisKeys.session(userId), step.toString())
        .then();
  }

  public Mono<Void> clear(Integer userId) {
    return commands.del(RedisKeys.session(userId))
        .then();
  }
}
