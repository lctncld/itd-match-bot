package com.epam.match.action;

import com.pengrad.telegrambot.model.Location;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Builder
@Slf4j
class SetLocationCommand implements Action {

  private final Long chatId;

  private final Location location;

  @Override
  public Flux<BaseRequest> execute() {
    log.info("Location: {}", location);
    return Flux.just(
        new SendMessage(chatId, "Your location is updated to " + location)
    );
  }
}
