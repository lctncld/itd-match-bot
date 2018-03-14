package com.epam.match.service.session;

import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RedisSessionService implements SessionService {

  private final RedisReactiveCommands<String, String> commands;

  public RedisSessionService(RedisReactiveCommands<String, String> commands) {
    this.commands = commands;
  }

  @Override
  public Mono<ProfileSetupStep> get(Integer userId) {
    return commands.get(key(userId))
      .switchIfEmpty(Mono.just(ProfileSetupStep.UNKNOWN.toString()))
      .map(string -> {
        try {
          return ProfileSetupStep.valueOf(string);
        } catch (IllegalArgumentException ex) {
          return ProfileSetupStep.UNKNOWN; // FIXME: handle with reactive operator
        }
      });
  }

  @Override
  public Mono<Void> set(Integer userId, ProfileSetupStep step) {
    return commands.set(key(userId), step.toString())
      .then();
  }

  @Override
  public Mono<Void> clear(Integer userId) {
    return commands.del(key(userId))
      .then();
  }

  private static String key(Object id) {
    return id + ":session";
  }

}
