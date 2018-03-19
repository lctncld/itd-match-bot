package com.epam.match.service.store;

import com.epam.match.domain.Contact;
import com.epam.match.domain.Gender;
import com.epam.match.domain.Match;
import com.epam.match.domain.Profile;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class RedisPersistentStore implements PersistentStore {

  private final RedisReactiveCommands<String, String> commands;

  public RedisPersistentStore(RedisReactiveCommands<String, String> commands) {
    this.commands = commands;
  }

  @Override
  public Mono<Void> like(String who, String whom) {
    return commands.sadd(Keys.likes(who), whom)
      .and(commands.lpush(Keys.seen(who), whom))
      .then();
  }

  @Override
  public Mono<Void> dislike(String who, String whom) {
    return commands.sadd(Keys.dislikes(who), whom)
      .and(commands.lpush(Keys.seen(who), whom))
      .then();
  }

  @Override
  public Mono<Boolean> isLikedBy(String who, String whom) {
    return commands.sismember(Keys.likes(whom), who);
  }

  @Override
  public Mono<Boolean> isDislikedBy(String who, String whom) {
    return commands.sismember(Keys.dislikes(whom), who);
  }

  @Override
  public Mono<Boolean> seen(String who, String whom) {
    return isLikedBy(whom, who)
      .filter(Boolean::valueOf)
      .switchIfEmpty(isDislikedBy(whom, who))
      .filter(Boolean::valueOf)
      .defaultIfEmpty(false);
  }

  @Override
  public Mono<Contact> getContact(String id) {
    return commands.hgetall(Keys.contact(id))
      .map(keys -> Contact.builder()
        .chatId(keys.get(Keys.Contact.chatId()))
        .firstName(keys.get(Keys.Contact.firstName()))
        .lastName(keys.get(Keys.Contact.lastName()))
        .phone(keys.get(Keys.Contact.phone()))
        .build());
  }

  @Override
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

  @Override
  public Mono<Profile> getSearchProfile(String id) {
    return commands.hgetall(Keys.profile(id))
      .filter(keys -> !keys.isEmpty())
      .map(keys -> {
          Optional<String> age = Optional.ofNullable(keys.get(Keys.Profile.age()));
          Optional<String> myGender = Optional.ofNullable(keys.get(Keys.Profile.gender()));
          Optional<String> minAge = Optional.ofNullable(keys.get(Keys.Profile.matchMinAge()));
          Optional<String> maxAge = Optional.ofNullable(keys.get(Keys.Profile.matchMaxAge()));
          Optional<String> matchGender = Optional.ofNullable(keys.get(Keys.Profile.matchGender()));
          return Profile.builder()
            .id(id)
            .age(age.map(Integer::valueOf))
            .gender(myGender.map(Gender::valueOf))
            .matchMinAge(minAge.map(Integer::valueOf))
            .matchMaxAge(maxAge.map(Integer::valueOf))
            .matchGender(matchGender.map(Gender::valueOf))
            .build();
        }
      );
  }

  @Override
  public Mono<String> getPhone(String id) {
    return commands.hget(Keys.contact(id), Keys.Contact.phone());
  }

  @Override
  public Mono<Void> setAge(String id, Integer age) {
    return commands.hmset(Keys.profile(id), Map.of(Keys.Profile.age(), age.toString()))
      .then();
  }

  @Override
  public Mono<Void> setGender(String id, Gender gender) {
    return commands.hmset(Keys.profile(id), Map.of(Keys.Profile.gender(), gender.toString()))
      .then();
  }

  @Override
  public Mono<Void> setMatchGender(String id, Gender gender) {
    return commands.hmset(Keys.profile(id), Map.of(Keys.Profile.matchGender(), gender.toString()))
      .then();
  }

  @Override
  public Mono<Void> setMatchMinAge(String id, Integer age) {
    return commands.hmset(Keys.profile(id), Map.of(Keys.Profile.matchMinAge(), age.toString()))
      .then();
  }

  @Override
  public Mono<Void> setMatchMaxAge(String id, Integer age) {
    return commands.hmset(Keys.profile(id), Map.of(Keys.Profile.matchMaxAge(), age.toString()))
      .then();
  }

  @Override
  public Mono<Void> setContact(String id, Contact contact) {
    return commands.hmset(Keys.contact(id), new HashMap<>() {{
      put(Keys.Contact.phone(), contact.getPhone());
      put(Keys.Contact.firstName(), contact.getFirstName());
      put(Keys.Contact.lastName(), contact.getLastName());
      put(Keys.Contact.chatId(), contact.getChatId());
    }})
      .then();
  }

  @Override
  public Mono<Void> setImage(String id, String imageId) {
    return commands.set(Keys.image(id), imageId)
      .then();
  }

  @Override
  public Mono<Void> resetProfile(String id) {
    return commands.keys(Keys.allUserKeys(id))
      .flatMap(commands::del)
      .then();
  }

  private static class Keys {

    public static String allUserKeys(Object id) {
      return id + ":*";
    }

    public static String profile(Object id) {
      return id + ":profile";
    }

    public static String image(Object id) {
      return id + ":image";
    }

    public static String likes(Object id) {
      return id + ":likes";
    }

    public static String seen(Object id) {
      return id + ":seen";
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
