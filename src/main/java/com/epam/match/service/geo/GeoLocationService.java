package com.epam.match.service.geo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GeoLocationService {

  Mono<Void> update(String userId, Float latitude, Float longitude);

  Mono<Void> delete(String userId);

  Flux<String> nearbyUsers(String toWhom, Double kilometers);
}
