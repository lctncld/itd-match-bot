package com.epam.match.service.telegram;

import com.epam.match.domain.Gender;
import com.epam.match.repository.Repository;
import com.epam.match.service.geo.LocationService;
import com.epam.match.service.session.SessionService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Contact;
import com.pengrad.telegrambot.model.Location;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.UserProfilePhotos;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.GetUserProfilePhotos;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetUserProfilePhotosResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProfileService {

  private final TelegramBot bot;

  private final LocationService locationService;

  private final SessionService sessionService;

  private final Repository repository;

  public ProfileService(TelegramBot bot, LocationService locationService, SessionService sessionService,
    Repository repository) {
    this.bot = bot;
    this.locationService = locationService;
    this.sessionService = sessionService;
    this.repository = repository;
  }

  public Mono<Void> setupProfile(Update update) {
    Message message = update.message();
    Integer userId = message.from().id();
    return repository.getPhone(userId.toString())
      .single()
      .then(getProfileAsString(userId))
      .map(profile -> profileMenu(message.chat().id(), profile))
      .onErrorReturn(new SendMessage(message.chat().id(), "Share your phone number first!")
        .replyMarkup(new ReplyKeyboardMarkup(new KeyboardButton[] {
          new KeyboardButton("Share phone")
            .requestContact(true) })
          .oneTimeKeyboard(true)
          .resizeKeyboard(true))
      )
      .map(bot::execute)
      .then();
  }

  private Mono<String> getProfileAsString(Integer userId) {
    return repository.getSearchProfileAsString(userId.toString())
      .switchIfEmpty(Mono.just("Your profile appears to be blank, tap these buttons to fill it!"));
  }

  private SendMessage profileMenu(Long chatId, String message) {
    InlineKeyboardMarkup actions = new InlineKeyboardMarkup(
      new InlineKeyboardButton[] {
        new InlineKeyboardButton("Your gender")
          .callbackData("/profile/me/gender"),
        new InlineKeyboardButton("Your age")
          .callbackData("/profile/me/age"),
      },
      new InlineKeyboardButton[] {
        new InlineKeyboardButton("Gender")
          .callbackData("/profile/match/gender"),
        new InlineKeyboardButton("Min age")
          .callbackData("/profile/match/age/min"),
        new InlineKeyboardButton("Max age")
          .callbackData("/profile/match/age/max")
      },
      new InlineKeyboardButton[] {
        new InlineKeyboardButton("No more changes needed")
          .callbackData("/profile/done")
      }
    );
    return new SendMessage(chatId, message)
      .replyMarkup(actions);
  }

  public Mono<Void> setAge(Update update) {
    Message message = update.message();
    Long chatId = message.chat().id();
    Integer userId = message.from().id();
    return Mono.just(message.text())
      .map(Integer::valueOf)
      .flatMap(age -> repository.setAge(userId.toString(), age))
      .then(sessionService.clear(userId))
      .thenReturn(profileMenu(chatId, "Anything else?"))
      .onErrorReturn(ageValidationFailMessage(chatId))
      .map(bot::execute)
      .then();
  }

  public Mono<Void> setLocation(Update update) {
    Message message = update.message();
    Long chatId = message.chat().id();
    Location location = message.location();
    return locationService.update(message.from().id().toString(), location.latitude(), location.longitude())
      .thenMany(Flux.just(
        new SendMessage(chatId, "Your location is updated, I'll let others know")
      ))
      .onErrorReturn(new SendMessage(chatId, "Something went wrong"))
      .map(bot::execute)
      .then();
  }

  public Mono<Void> setMatchGender(Update update, Gender gender) {
    CallbackQuery cb = update.callbackQuery();
    Long chatId = cb.message().chat().id();
    return repository.setMatchGender(cb.from().id().toString(), gender)
      .thenMany(Flux.just(
        new AnswerCallbackQuery(cb.id()),
        new SendMessage(chatId, "Now looking for " + gender.toString()),
        profileMenu(chatId, "Anything else?")
      ))
      .map(bot::execute)
      .then();
  }

  public Mono<Void> setGender(Update update, Gender gender) {
    CallbackQuery cb = update.callbackQuery();
    Long chatId = cb.message().chat().id();
    return repository.setGender(cb.from().id().toString(), gender)
      .thenMany(Flux.just(
        new AnswerCallbackQuery(cb.id()),
        new SendMessage(chatId, String.format("You are %s, understood", gender.toString())),
        profileMenu(chatId, "Anything else?")
      ))
      .map(bot::execute)
      .then();
  }

  public Mono<Void> setMatchMinAge(Update update) {
    Message message = update.message();
    Long chatId = message.chat().id();
    Integer userId = message.from().id();
    return Mono.just(message.text())
      .map(Integer::valueOf)
      .flatMap(age -> repository.setMatchMinAge(userId.toString(), age))
      .then(sessionService.clear(userId))
      .thenReturn(profileMenu(chatId, "Anything else?"))
      .onErrorReturn(ageValidationFailMessage(chatId))
      .map(bot::execute)
      .then();
  }

  public Mono<Void> setMatchMaxAge(Update update) {
    Message message = update.message();
    Long chatId = message.chat().id();
    Integer userId = message.from().id();
    return Mono.just(message.text())
      .map(Integer::valueOf)
      .flatMap(age -> repository.setMatchMaxAge(userId.toString(), age))
      .then(sessionService.clear(userId))
      .thenReturn(profileMenu(chatId, "Anything else?"))
      .onErrorReturn(ageValidationFailMessage(chatId))
      .map(bot::execute)
      .then();
  }

  private SendMessage ageValidationFailMessage(Long chatId) {
    return new SendMessage(chatId, "Respond with a number please");
  }

  public Mono<Void> leaveProfileConfiguration(Update update) {
    CallbackQuery cb = update.callbackQuery();
    Long chatId = cb.message().chat().id();
    return getProfileAsString(cb.from().id())
      .flatMapMany(profile -> Flux.just(
        new SendMessage(chatId, profile),
        new AnswerCallbackQuery(cb.id()),
        new DeleteMessage(chatId, cb.message().messageId())
      ))
      .map(bot::execute)
      .then();
  }

  public Mono<Void> setContact(Update update) {
    Message message = update.message();
    Contact contact = message.contact();
    if (contact.userId() == null || !contact.userId().equals(message.from().id())) {
      return Mono.just(new SendMessage(message.chat().id(), "I don't believe you"))
        .map(bot::execute)
        .then();
    }
    Integer id = update.message().from().id();
    return repository.setContact(id.toString(), com.epam.match.domain.Contact.builder()
      .phone(contact.phoneNumber())
      .firstName(contact.firstName())
      .lastName(contact.lastName())
      .chatId(message.chat().id().toString())
      .build()
    )
      .then(setDefaultImage(id))
      .thenReturn(profileMenu(message.chat().id(), "Now, who are we looking for?"))
      .map(bot::execute)
      .then();
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
      .flatMap(image -> repository.setImage(userId.toString(), image))
      .then();
  }

  public Mono<Void> setImage(Update update) {
    Message message = update.message();
    PhotoSize photo = message.photo()[0];
    String photoId = photo.fileId();
    return repository.setImage(message.from().id().toString(), photoId)
      .thenReturn(new SendMessage(message.chat().id(), "Updated your photo!"))
      .map(bot::execute)
      .then();
  }
}
