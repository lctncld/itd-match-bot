package com.epam.match.service.telegram;

import com.epam.match.domain.Gender;
import com.epam.match.service.store.PersistentStore;
import com.epam.match.service.geo.GeoLocationService;
import com.epam.match.service.session.ProfileSetupStep;
import com.epam.match.service.session.SessionService;
import com.epam.match.spring.annotation.MessageMapping;
import com.epam.match.spring.annotation.TelegramBotController;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Contact;
import com.pengrad.telegrambot.model.Location;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@TelegramBotController
public class ProfileService {

  private final GeoLocationService locationService;

  private final SessionService sessionService;

  private final PersistentStore store;

  private final DirectCallService directCallService;

  public ProfileService(GeoLocationService locationService, SessionService sessionService, PersistentStore store,
    DirectCallService directCallService) {
    this.locationService = locationService;
    this.sessionService = sessionService;
    this.store = store;
    this.directCallService = directCallService;
  }

  @MessageMapping("/profile")
  public Mono<? extends BaseRequest> setupProfile(Update update) {
    Message message = update.message();
    Integer userId = message.from().id();
    return store.getPhone(userId.toString())
      .single()
      .then(getProfileAsString(userId))
      .map(profile -> profileMenu(message.chat().id(), profile))
      .onErrorReturn(new SendMessage(message.chat().id(), "Share your phone number first!")
        .replyMarkup(new ReplyKeyboardMarkup(new KeyboardButton[] {
          new KeyboardButton("Share phone")
            .requestContact(true) })
          .oneTimeKeyboard(true)
          .resizeKeyboard(true))
      );
  }

  private Mono<String> getProfileAsString(Integer userId) {
    return store.getSearchProfileAsString(userId.toString())
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

  @MessageMapping(step = ProfileSetupStep.SET_MY_AGE)
  public Mono<? extends BaseRequest> setAge(Update update) {
    Message message = update.message();
    Long chatId = message.chat().id();
    Integer userId = message.from().id();
    return Mono.just(message.text())
      .map(Integer::valueOf)
      .flatMap(age -> store.setAge(userId.toString(), age))
      .then(sessionService.clear(userId))
      .thenReturn(profileMenu(chatId, "Anything else?"))
      .onErrorReturn(ageValidationFailMessage(chatId));
  }

  @MessageMapping("/location")
  public Mono<? extends BaseRequest> setLocation(Update update) {
    Message message = update.message();
    Long chatId = message.chat().id();
    Location location = message.location();
    return locationService.update(message.from().id().toString(), location.latitude(), location.longitude())
      .thenReturn(new SendMessage(chatId, "Your location is updated, I'll let others know"))
      .onErrorReturn(new SendMessage(chatId, "Something went wrong"));
  }

  @MessageMapping("/profile/match/gender/male")
  public Flux<? extends BaseRequest> setMatchGenderToMale(Update update) {
    return setMatchGender(update, Gender.MALE);
  }

  @MessageMapping("/profile/match/gender/female")
  public Flux<? extends BaseRequest> setMatchGenderToFemale(Update update) {
    return setMatchGender(update, Gender.FEMALE);
  }

  @MessageMapping("/profile/match/gender/both")
  public Flux<? extends BaseRequest> setMatchGenderToBoth(Update update) {
    return setMatchGender(update, Gender.BOTH);
  }

  private Flux<? extends BaseRequest> setMatchGender(Update update, Gender gender) {
    CallbackQuery cb = update.callbackQuery();
    Long chatId = cb.message().chat().id();
    return store.setMatchGender(cb.from().id().toString(), gender)
      .thenMany(Flux.just(
        new AnswerCallbackQuery(cb.id()),
        new SendMessage(chatId, "Now looking for " + gender.toString()),
        profileMenu(chatId, "Anything else?")
      ));
  }

  @MessageMapping("/profile/me/gender/male")
  public Flux<? extends BaseRequest> setGenderToMale(Update update) {
    return setGender(update, Gender.MALE);
  }

  @MessageMapping("/profile/me/gender/female")
  public Flux<? extends BaseRequest> setGenderToFemale(Update update) {
    return setGender(update, Gender.FEMALE);
  }

  private Flux<? extends BaseRequest> setGender(Update update, Gender gender) {
    CallbackQuery cb = update.callbackQuery();
    Long chatId = cb.message().chat().id();
    return store.setGender(cb.from().id().toString(), gender)
      .thenMany(Flux.just(
        new AnswerCallbackQuery(cb.id()),
        new SendMessage(chatId, String.format("You are %s, understood", gender.toString())),
        profileMenu(chatId, "Anything else?")
      ));
  }

  @MessageMapping(step = ProfileSetupStep.SET_MATCH_MIN_AGE)
  public Mono<? extends BaseRequest> setMatchMinAge(Update update) {
    Message message = update.message();
    Long chatId = message.chat().id();
    Integer userId = message.from().id();
    return Mono.just(message.text())
      .map(Integer::valueOf)
      .flatMap(age -> store.setMatchMinAge(userId.toString(), age))
      .then(sessionService.clear(userId))
      .thenReturn(profileMenu(chatId, "Anything else?"))
      .onErrorReturn(ageValidationFailMessage(chatId));
  }

  @MessageMapping(step = ProfileSetupStep.SET_MATCH_MAX_AGE)
  public Mono<? extends BaseRequest> setMatchMaxAge(Update update) {
    Message message = update.message();
    Long chatId = message.chat().id();
    Integer userId = message.from().id();
    return Mono.just(message.text())
      .map(Integer::valueOf)
      .flatMap(age -> store.setMatchMaxAge(userId.toString(), age))
      .then(sessionService.clear(userId))
      .thenReturn(profileMenu(chatId, "Anything else?"))
      .onErrorReturn(ageValidationFailMessage(chatId));
  }

  private SendMessage ageValidationFailMessage(Long chatId) {
    return new SendMessage(chatId, "Respond with a number please");
  }

  @MessageMapping("/profile/done")
  public Flux<? extends BaseRequest> leaveProfileConfiguration(Update update) {
    CallbackQuery cb = update.callbackQuery();
    Long chatId = cb.message().chat().id();
    return getProfileAsString(cb.from().id())
      .flatMapMany(profile -> Flux.just(
        new SendMessage(chatId, profile),
        new AnswerCallbackQuery(cb.id()),
        new DeleteMessage(chatId, cb.message().messageId())
      ));
  }

  @MessageMapping("/contact")
  public Mono<? extends BaseRequest> setContact(Update update) {
    Message message = update.message();
    Contact contact = message.contact();
    if (contact.userId() == null || !contact.userId().equals(message.from().id())) {
      return Mono.just(new SendMessage(message.chat().id(), "I don't believe you"));
    }
    Integer id = update.message().from().id();
    return store.setContact(id.toString(), com.epam.match.domain.Contact.builder()
      .phone(contact.phoneNumber())
      .firstName(contact.firstName())
      .lastName(contact.lastName())
      .chatId(message.chat().id().toString())
      .build()
    )
      .then(directCallService.setDefaultImage(id))
      .thenReturn(profileMenu(message.chat().id(), "Now, who are we looking for?"));
  }

  @MessageMapping("/photo")
  public Mono<? extends BaseRequest> setImage(Update update) {
    Message message = update.message();
    PhotoSize photo = message.photo()[0];
    String photoId = photo.fileId();
    return store.setImage(message.from().id().toString(), photoId)
      .thenReturn(new SendMessage(message.chat().id(), "Updated your photo!"));
  }
}
