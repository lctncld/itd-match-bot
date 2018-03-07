package com.epam.match.service.geo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LocationService {

  Mono<Void> update(String userId, Float latitude, Float longitude);

  Flux<String> nearbyUsers(String toWhom, Double kilometers);
}
