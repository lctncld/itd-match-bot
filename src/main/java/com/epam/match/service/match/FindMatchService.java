package com.epam.match.service.match;

import com.epam.match.domain.Gender;
import com.epam.match.domain.Match;
import com.epam.match.domain.Profile;
import com.epam.match.service.geo.GeoLocationService;
import com.epam.match.service.store.PersistentStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Optional;

import static com.epam.match.Functions.negate;

@Slf4j
@Service
public class FindMatchService {

  private final GeoLocationService locationService;

  private final PersistentStore store;

  public FindMatchService(GeoLocationService locationService, PersistentStore store) {
    this.locationService = locationService;
    this.store = store;
  }

  public Mono<Match> next(Integer userId) {
    String user_id = userId.toString();
    return locationService.nearbyUsers(user_id, 10.0)
      .filterWhen(matchId -> store.seen(user_id, matchId)
        .map(negate())
      )
      .doOnNext(matchId -> log.info("Nearby users: {}:{}", userId, matchId))
      .flatMap(id -> store.getSearchProfile(id).defaultIfEmpty(Profile.empty(id))
        .zipWith(store.getSearchProfile(user_id).defaultIfEmpty(Profile.empty(user_id)))
      )
      .filter(tuple -> {
        Profile match = tuple.getT1();
        Profile me = tuple.getT2();

        Integer from = me.getMatchMinAge().orElse(0);
        Integer to = me.getMatchMaxAge().orElse(100);
        Gender myGender = me.getMatchGender().orElse(Gender.BOTH);

        Integer age = match.getAge().orElse(0);
        Optional<Gender> gender = match.getGender();

        boolean ageMatches = age >= from && age <= to;
        boolean genderMatches = myGender == Gender.BOTH || (gender.isPresent() && myGender == gender.get());
        log.info("Matching {}:{}. Age matches? {} Gender matches? {}",
          me.getId(), match.getId(), ageMatches, genderMatches);
        return ageMatches && genderMatches;
      })
      .map(Tuple2::getT1)
      .map(Profile::getId)
      .flatMap(store::getMatchById)
      .next();
  }
}
