package com.epam.match.service.telegram;

import com.epam.match.MessageSourceAdapter;
import com.epam.match.domain.Gender;
import com.epam.match.service.geo.GeoLocationService;
import com.epam.match.service.session.ProfileSetupStep;
import com.epam.match.service.session.SessionService;
import com.epam.match.service.store.PersistentStore;
import com.epam.match.spring.annotation.MessageMapping;
import com.epam.match.spring.annotation.TelegramBotController;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Contact;
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

  private final MessageSourceAdapter messageSource;

  public ProfileService(GeoLocationService locationService, SessionService sessionService, PersistentStore store,
    DirectCallService directCallService, MessageSourceAdapter messageSource) {
    this.locationService = locationService;
    this.sessionService = sessionService;
    this.store = store;
    this.directCallService = directCallService;
    this.messageSource = messageSource;
  }

  @MessageMapping("/profile")
  public Mono<? extends BaseRequest> setupProfile(Update update) {
    Message message = update.message();
    Integer userId = message.from().id();
    return store.getPhone(userId.toString())
      .single()
      .then(getProfileAsString(userId))
      .map(profile -> profileMenu(message.chat().id(), profile))
      .onErrorReturn(new SendMessage(message.chat().id(), messageSource.get("profile.share.phone.alert"))
        .replyMarkup(new ReplyKeyboardMarkup(new KeyboardButton[] {
          new KeyboardButton(messageSource.get("profile.share.phone.share_button"))
            .requestContact(true) })
          .oneTimeKeyboard(true)
          .resizeKeyboard(true))
      );
  }

  private Mono<String> getProfileAsString(Integer userId) {
    return store.getSearchProfile(userId.toString())
      .map(profile -> {
          boolean selfNotEmpty = profile.getAge().isPresent() || profile.getGender().isPresent();
          boolean matchNotEmpty = profile.getMatchMinAge().isPresent()
            || profile.getMatchMaxAge().isPresent()
            || profile.getMatchGender().isPresent();
          boolean minAndMaxAgeSpecified = profile.getMatchMinAge().isPresent() && profile.getMatchMaxAge().isPresent();

          StringBuilder out = new StringBuilder();

          if (selfNotEmpty) {
            out.append(messageSource.get("profile.you.prefix"))
              .append(" ")
              .append(profile.getAge().map(String::valueOf).orElse(""))
              .append(profile.getGender().map(Gender::getLocalization).map(messageSource::get).orElse(""));
          }

          if (matchNotEmpty) {
            out.append("\n")
              .append(messageSource.get("profile.match.prefix"))
              .append(" ")
              .append(profile.getMatchMinAge().map(String::valueOf).orElse(""))
              .append(minAndMaxAgeSpecified
                ? "-"
                : "")
              .append(profile.getMatchMaxAge().map(String::valueOf).orElse(""))
              .append(profile.getMatchGender().map(Gender::getLocalization).map(messageSource::get).orElse(""));
          }

          return out.toString();
        }
      )
      .switchIfEmpty(Mono.just(messageSource.get("profile.blank")));
  }

  private SendMessage profileMenuWithDefaultMessage(Long chatId) {
    return profileMenu(chatId, messageSource.get("profile.set.callback"));
  }

  private SendMessage profileMenu(Long chatId, String message) {
    InlineKeyboardMarkup actions = new InlineKeyboardMarkup(
      new InlineKeyboardButton[] {
        new InlineKeyboardButton(messageSource.get("profile.set.button.your.gender"))
          .callbackData("/profile/me/gender"),
        new InlineKeyboardButton(messageSource.get("profile.set.button.your.age"))
          .callbackData("/profile/me/age"),
      },
      new InlineKeyboardButton[] {
        new InlineKeyboardButton(messageSource.get("profile.set.button.match.gender"))
          .callbackData("/profile/match/gender"),
        new InlineKeyboardButton(messageSource.get("profile.set.button.match.age.from"))
          .callbackData("/profile/match/age/min"),
        new InlineKeyboardButton(messageSource.get("profile.set.button.match.age.to"))
          .callbackData("/profile/match/age/max")
      },
      new InlineKeyboardButton[] {
        new InlineKeyboardButton(messageSource.get("profile.set.button.done"))
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
      .thenReturn(profileMenuWithDefaultMessage(chatId))
      .onErrorReturn(ageValidationFailMessage(chatId));
  }

  @MessageMapping("/location")
  public Mono<? extends BaseRequest> setLocation(Update update) {
    Message message = update.message();
    Long chatId = message.chat().id();
    return Mono.justOrEmpty(message.location())
      .flatMap(location -> {
        String userId = message.from().id().toString();
        return locationService.update(userId, location.latitude(), location.longitude())
          .thenReturn(new SendMessage(chatId, messageSource.get("profile.set.location")));
      })
      .switchIfEmpty(Mono.just(new SendMessage(chatId, messageSource.get("profile.set.location.error"))));
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
        profileMenuWithDefaultMessage(chatId)
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
        profileMenuWithDefaultMessage(chatId)
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
      .thenReturn(profileMenuWithDefaultMessage(chatId))
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
      .thenReturn(profileMenuWithDefaultMessage(chatId))
      .onErrorReturn(ageValidationFailMessage(chatId));
  }

  private SendMessage ageValidationFailMessage(Long chatId) {
    return new SendMessage(chatId, messageSource.get("profile.validate.number"));
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
      return Mono.just(new SendMessage(message.chat().id(), messageSource.get("profile.validate.contact")));
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
      .thenReturn(profileMenu(message.chat().id(), messageSource.get("profile.set.greeting")));
  }

  @MessageMapping("/photo")
  public Mono<? extends BaseRequest> setImage(Update update) {
    Message message = update.message();
    PhotoSize photo = message.photo()[0];
    String photoId = photo.fileId();
    return store.setImage(message.from().id().toString(), photoId)
      .thenReturn(new SendMessage(message.chat().id(), messageSource.get("profile.set.photo")));
  }

  @MessageMapping("/reset")
  public Mono<? extends BaseRequest> resetProfile(Update update) {
    Integer id = update.message().from().id();
    return store.resetProfile(id.toString())
      .then(locationService.delete(id.toString()))
      .thenReturn(new SendMessage(update.message().chat().id(), messageSource.get("profile.cleared")));
  }
}
