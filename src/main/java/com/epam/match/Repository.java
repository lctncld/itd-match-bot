package com.epam.match;

import com.epam.match.domain.Contact;
import com.epam.match.domain.Match;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

  public Mono<Boolean> isLikedBy(String who, String whom) {
    return commands.sismember(RedisKeys.likes(whom), who);
  }

  public Mono<Boolean> isDislikedBy(String who, String whom) {
    return commands.sismember(RedisKeys.dislikes(whom), who);
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

  public Mono<Match> getMatchById(String id) {
    return Flux.zip(
      commands.get(RedisKeys.image(id)),
      commands.hget(RedisKeys.contact(id), "first_name")
    )
      .next()
      .map(tuple -> Match.builder()
        .id(tuple.getT1())
        .image(tuple.getT2())
        .name(id)
        .build()
      );
  }

}
