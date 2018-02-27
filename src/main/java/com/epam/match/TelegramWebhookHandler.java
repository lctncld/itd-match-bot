package com.epam.match;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class TelegramWebhookHandler {

  private final TelegramBot bot;

  public TelegramWebhookHandler(TelegramBot bot) {
    this.bot = bot;
  }

  public Mono<ServerResponse> handle(ServerRequest request) {
    return request.bodyToMono(String.class)
        .map(BotUtils::parseUpdate)
        .flatMap(update -> {
          Message message = update.message();

          KeyboardButton shareLocation = new KeyboardButton("Share My Location")
              .requestLocation(true);

          SendMessage req = new SendMessage(message.chat().id(), "Share your location to proceed")
              .replyMarkup(new ReplyKeyboardMarkup(new KeyboardButton[] { shareLocation }));

          SendResponse response = bot.execute(req);
          log.info("sendMessage {}", response);
          return Mono.empty();
        });
  }
}
