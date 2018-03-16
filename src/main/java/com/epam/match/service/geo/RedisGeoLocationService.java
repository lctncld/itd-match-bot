package com.epam.match.service.geo;

import io.lettuce.core.GeoArgs;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RedisGeoLocationService implements GeoLocationService {

  private final Logger log = LoggerFactory.getLogger(RedisGeoLocationService.class);

  private final RedisReactiveCommands<String, String> commands;

  public RedisGeoLocationService(RedisReactiveCommands<String, String> commands) {
    this.commands = commands;
  }

  @Override
  public Mono<Void> update(String userId, Float latitude, Float longitude) {
    return commands.geoadd(
      key(),
      latitude,
      longitude,
      userId
    )
      .then();
  }

  @Override
  public Flux<String> nearbyUsers(String toWhom, Double kilometers) {
    return commands.georadiusbymember(
      key(),
      toWhom,
      kilometers,
      GeoArgs.Unit.km
    )
      .filter(user -> !user.equals(toWhom))
      .doOnNext(user -> log.info("Found profile {} near {}", user, toWhom));
  }

  private static String key() {
    return "locations";
  }
}
