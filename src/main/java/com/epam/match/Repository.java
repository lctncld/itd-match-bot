package com.epam.match;

import com.epam.match.domain.Contact;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class Repository {

  private final RedisReactiveCommands<String, String> commands;

  public Repository(RedisReactiveCommands<String, String> commands) {
    this.commands = commands;
  }

  public Mono<Long> like(String who, String whom) {
    return commands.sadd(RedisKeys.likes(who), whom);
  }

  public Mono<Long> dislike(String who, String whom) {
    return commands.sadd(RedisKeys.dislikes(who), whom);
  }

  public Mono<Boolean> isMutualLike(String who, String whom) {
    return commands.sismember(RedisKeys.likes(whom), who);
  }

  public Mono<Contact> getContact(String id) {
    return commands.hgetall(RedisKeys.contact(id))
      .map(keys -> Contact.builder()
        .chatId(keys.get("chat_id"))
        .firstName("first_name")
        .lastName("last_name")
        .phone("phone")
        .build());
  }
}
