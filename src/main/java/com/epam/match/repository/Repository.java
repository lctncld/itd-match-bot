package com.epam.match.repository;

import com.epam.match.domain.Contact;
import com.epam.match.domain.Gender;
import com.epam.match.domain.Match;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class Repository {

  private final RedisReactiveCommands<String, String> commands;

  public Repository(RedisReactiveCommands<String, String> commands) {
    this.commands = commands;
  }

  public Mono<Long> like(String who, String whom) {
    return commands.sadd(Keys.likes(who), whom);
  }

  public Mono<Long> dislike(String who, String whom) {
    return commands.sadd(Keys.dislikes(who), whom);
  }

  public Mono<Boolean> isLikedBy(String who, String whom) {
    return commands.sismember(Keys.likes(who), whom);
  }

  public Mono<Boolean> isDislikedBy(String who, String whom) {
    return commands.sismember(Keys.dislikes(who), whom);
  }

  public Mono<Contact> getContact(String id) {
    return commands.hgetall(Keys.contact(id))
      .map(keys -> Contact.builder()
        .chatId(keys.get("chat_id"))
        .firstName("first_name")
        .lastName("last_name")
        .phone("phone")
        .build());
  }

  public Mono<Match> getMatchById(String id) {
    return Flux.zip(
      commands.get(Keys.image(id)),
      commands.hget(Keys.contact(id), "first_name")
    )
      .next()
      .map(tuple -> Match.builder()
        .id(id)
        .image(tuple.getT1())
        .name(tuple.getT2())
        .build()
      );
  }

  public Mono<String> getSearchProfileAsString(String id) {
    return commands.hgetall(Keys.user(id))
      .filter(profile -> !profile.isEmpty())
      .map(profile -> profile.entrySet().stream()
        .map(entry -> entry.getKey() + ":" + entry.getValue())
        .collect(Collectors.joining(", "))
      );
  }

  public Mono<String> getPhone(String id) {
    return commands.hget(Keys.contact(id), "phone");
  }

  public Mono<Void> setAge(String id, Integer age) {
    return commands.hmset(Keys.user(id), Map.of("age", age.toString()))
      .then();
  }

  public Mono<Void> setGender(String id, Gender gender) {
    return commands.hmset(Keys.user(id), Map.of("gender", gender.toString()))
      .then();
  }

  public Mono<Void> setMatchGender(String id, Gender gender) {
    return commands.hmset(Keys.user(id), Map.of("matchGender", gender.toString()))
      .then();
  }

  public Mono<Void> setMatchMinAge(String id, Integer age) {
    return commands.hmset(Keys.user(id), Map.of("matchMinAge", age.toString()))
      .then();
  }

  public Mono<Void> setMatchMaxAge(String id, Integer age) {
    return commands.hmset(Keys.user(id), Map.of("matchMaxAge", age.toString()))
      .then();
  }

  public Mono<Void> setContact(String id, Contact contact) {
    return commands.hmset(Keys.contact(id), new HashMap<>() {{
      put("phone", contact.getPhone());
      put("first_name", contact.getFirstName());
      put("last_name", contact.getLastName());
      put("chat_id", contact.getChatId());
    }})
      .then();
  }

  public Mono<Void> setImage(String id, String imageId) {
    return commands.set(Keys.image(id), imageId)
      .then();
  }

  private static class Keys {

    public static String user(Object id) {
      return id + ":profile";
    }

    public static String image(Object id) {
      return id + ":image";
    }

    public static String likes(Object id) {
      return id + ":likes";
    }

    public static String dislikes(Object id) {
      return id + ":dislikes";
    }

    public static String contact(Object id) {
      return id + ":contact";
    }
  }

}
