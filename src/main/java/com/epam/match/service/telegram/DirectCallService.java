package com.epam.match.service.telegram;

import com.epam.match.service.store.PersistentStore;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.UserProfilePhotos;
import com.pengrad.telegrambot.request.GetUserProfilePhotos;
import com.pengrad.telegrambot.response.GetUserProfilePhotosResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DirectCallService {

  private final TelegramBot bot;

  private final PersistentStore store;

  public DirectCallService(TelegramBot bot, PersistentStore store) {
    this.bot = bot;
    this.store = store;
  }

  public Mono<Void> setDefaultImage(Integer userId) {
    return Mono.just(userId)
      .map(GetUserProfilePhotos::new)
      .map(bot::execute)
      .map(GetUserProfilePhotosResponse::photos)
      .filter(p -> p.totalCount() > 0)
      .map(UserProfilePhotos::photos)
      .map(p -> p[0]) // Cool
      .map(p -> p[0]) // API
      .map(PhotoSize::fileId)
      .flatMap(image -> store.setImage(userId.toString(), image))
      .then();
  }
}
