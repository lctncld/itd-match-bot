package com.epam.match.service.match;

import com.epam.match.domain.Match;
import com.epam.match.service.store.PersistentStore;
import com.epam.match.service.geo.GeoLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
    return locationService.nearbyUsers(userId.toString(), 10.0)
      .filterWhen(matchId -> store.seen(userId.toString(), matchId)
        .map(negate())
      )
      .doOnNext(matchId -> log.info("Match for {} is {}", userId, matchId))
      .next()
      .flatMap(store::getMatchById);
  }
}
