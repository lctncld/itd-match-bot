package com.epam.match;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class MatchApplication {

  @Autowired
  private TelegramWebhookHandler telegramHandler;

  @Bean
  public RouterFunction<ServerResponse> router() {
    return route(POST("/").and(accept(APPLICATION_JSON)), telegramHandler::route);
  }

  public static void main(String[] args) {
    SpringApplication.run(MatchApplication.class, args);
  }
}
