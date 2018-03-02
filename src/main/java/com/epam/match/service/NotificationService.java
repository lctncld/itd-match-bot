package com.epam.match.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.GetUserProfilePhotos;
import com.pengrad.telegrambot.request.SendPhoto;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class NotificationService {

  private final TelegramBot bot;

  private final RedisReactiveCommands<String, String> commands;

  public NotificationService(TelegramBot bot, RedisReactiveCommands<String, String> commands) {
    this.bot = bot;
    this.commands = commands;
  }

  public Mono<Void> notify(Integer who, Integer whom) {
    GetUserProfilePhotos request = new GetUserProfilePhotos(who);
    return Mono.just(request)
        .map(bot::execute)
        .map(response -> response.photos().photos())
        .doOnNext(photo -> log.info("photos: {}", photo))
        .filter(photos -> photos.length > 0)
        .map(photos -> {
          PhotoSize[] profilePic = photos[0];
          PhotoSize biggest = profilePic[profilePic.length];
          return new SendPhoto("", biggest.fileId())
              .caption("Interested in ... ?");
//              .replyMarkup(new ReplyKeyboardRemove());
        }).map(bot::execute)
        .then();
  }
}
