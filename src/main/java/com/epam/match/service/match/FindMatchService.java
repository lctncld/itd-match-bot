package com.epam.match.service.match;

import com.epam.match.repository.Repository;
import com.epam.match.domain.Match;
import com.epam.match.service.geo.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.epam.match.Functions.negate;

@Slf4j
@Service
public class FindMatchService {

  private final LocationService locationService;

  private final Repository repository;

  public FindMatchService(LocationService locationService, Repository repository) {
    this.locationService = locationService;
    this.repository = repository;
  }

  public Mono<Match> next(Integer userId) {
    return locationService.nearbyUsers(userId.toString(), 10.0)
      .filterWhen(matchId -> repository.seen(userId.toString(), matchId)
        .map(negate())
      )
      .doOnNext(matchId -> log.info("Match for {} is {}", userId, matchId))
      .next()
      .flatMap(repository::getMatchById);
  }
}
