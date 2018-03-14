package com.epam.match.service.session;

import reactor.core.publisher.Mono;

public interface SessionService {

  Mono<ProfileSetupStep> get(Integer userId);

  Mono<Void> set(Integer userId, ProfileSetupStep step);

  Mono<Void> clear(Integer userId);
}
