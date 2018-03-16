package com.epam.match.service.store;

import com.epam.match.domain.Contact;
import com.epam.match.domain.Gender;
import com.epam.match.domain.Match;
import reactor.core.publisher.Mono;

public interface PersistentStore {

  Mono<Void> like(String who, String whom);

  Mono<Void> dislike(String who, String whom);

  Mono<Boolean> isLikedBy(String who, String whom);

  Mono<Boolean> isDislikedBy(String who, String whom);

  Mono<Boolean> seen(String who, String whom);

  Mono<Contact> getContact(String id);

  Mono<Match> getMatchById(String id);

  Mono<String> getSearchProfileAsString(String id);

  Mono<String> getPhone(String id);

  Mono<Void> setAge(String id, Integer age);

  Mono<Void> setGender(String id, Gender gender);

  Mono<Void> setMatchGender(String id, Gender gender);

  Mono<Void> setMatchMinAge(String id, Integer age);

  Mono<Void> setMatchMaxAge(String id, Integer age);

  Mono<Void> setContact(String id, Contact contact);

  Mono<Void> setImage(String id, String imageId);
}
