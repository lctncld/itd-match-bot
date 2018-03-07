package com.epam.match.service;

import com.epam.match.Repository;
import com.epam.match.domain.Match;
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
    return locationService.get(userId.toString())
      .filterWhen(matchId -> repository.isLikedBy(userId.toString(), matchId)
        .map(negate())
      )
      .filterWhen(matchId -> repository.isDislikedBy(userId.toString(), matchId)
        .map(negate())
      )
      .doOnNext(matchId -> log.info("Match for {} is {}", userId, matchId))
      .next()
      .flatMap(repository::getMatchById);
  }
}
