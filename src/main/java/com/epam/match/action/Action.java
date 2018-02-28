package com.epam.match.action;

import com.pengrad.telegrambot.request.BaseRequest;
import reactor.core.publisher.Flux;

public interface Action {

  Flux<BaseRequest> execute();
}
