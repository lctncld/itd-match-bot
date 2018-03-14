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
        .chatId(keys.get(Keys.Contact.chatId()))
        .firstName(keys.get(Keys.Contact.firstName()))
        .lastName(keys.get(Keys.Contact.lastName()))
        .phone(keys.get(Keys.Contact.phone()))
        .build());
  }

  public Mono<Match> getMatchById(String id) {
    return Flux.zip(
      commands.get(Keys.image(id)),
      commands.hget(Keys.contact(id), Keys.Contact.firstName())
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
    return commands.hgetall(Keys.profile(id))
      .filter(profile -> !profile.isEmpty())
      .map(profile -> profile.entrySet().stream()
        .map(entry -> entry.getKey() + ":" + entry.getValue())
        .collect(Collectors.joining(", "))
      );
  }

  public Mono<String> getPhone(String id) {
    return commands.hget(Keys.contact(id), Keys.Contact.phone());
  }

  public Mono<Void> setAge(String id, Integer age) {
    return commands.hmset(Keys.profile(id), Map.of(Keys.Profile.age(), age.toString()))
      .then();
  }

  public Mono<Void> setGender(String id, Gender gender) {
    return commands.hmset(Keys.profile(id), Map.of(Keys.Profile.gender(), gender.toString()))
      .then();
  }

  public Mono<Void> setMatchGender(String id, Gender gender) {
    return commands.hmset(Keys.profile(id), Map.of(Keys.Profile.matchGender(), gender.toString()))
      .then();
  }

  public Mono<Void> setMatchMinAge(String id, Integer age) {
    return commands.hmset(Keys.profile(id), Map.of(Keys.Profile.matchMinAge(), age.toString()))
      .then();
  }

  public Mono<Void> setMatchMaxAge(String id, Integer age) {
    return commands.hmset(Keys.profile(id), Map.of(Keys.Profile.matchMaxAge(), age.toString()))
      .then();
  }

  public Mono<Void> setContact(String id, Contact contact) {
    return commands.hmset(Keys.contact(id), new HashMap<>() {{
      put(Keys.Contact.phone(), contact.getPhone());
      put(Keys.Contact.firstName(), contact.getFirstName());
      put(Keys.Contact.lastName(), contact.getLastName());
      put(Keys.Contact.chatId(), contact.getChatId());
    }})
      .then();
  }

  public Mono<Void> setImage(String id, String imageId) {
    return commands.set(Keys.image(id), imageId)
      .then();
  }

  private static class Keys {

    public static String profile(Object id) {
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

    private static class Contact {

      public static String firstName() {
        return "first_name";
      }

      public static String lastName() {
        return "last_name";
      }

      public static String chatId() {
        return "chat_id";
      }

      public static String phone() {
        return "phone";
      }
    }

    private static class Profile {

      public static String age() {
        return "age";
      }

      public static String gender() {
        return "gender";
      }

      public static String matchGender() {
        return "matchGender";
      }

      public static String matchMinAge() {
        return "matchMinAge";
      }

      public static String matchMaxAge() {
        return "matchMaxAge";
      }
    }
  }

}
