package com.epam.match;

import com.epam.match.domain.Gender;
import com.epam.match.service.session.SessionService;
import com.epam.match.service.telegram.MatchService;
import com.epam.match.service.telegram.MessageService;
import com.epam.match.service.telegram.ProfileService;
import com.epam.match.service.telegram.QuestionService;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Contact;
import com.pengrad.telegrambot.model.Location;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TelegramUpdateRouter {

  private final Logger log = LoggerFactory.getLogger(TelegramUpdateRouter.class);

  private final ProfileService profileService;

  private final QuestionService questionService;

  private final MessageService messageService;

  private final SessionService sessionService;

  private final MatchService matchService;

  public TelegramUpdateRouter(ProfileService profileService, QuestionService questionService,
    MessageService messageService,
    SessionService sessionService, MatchService matchService) {
    this.profileService = profileService;
    this.questionService = questionService;
    this.messageService = messageService;
    this.sessionService = sessionService;
    this.matchService = matchService;
  }

  public Mono<?> route(Update update) {
    String command = command(update);
    if (command == null) {
      Integer userId = update.message().from().id();
      return sessionService.get(userId)
        .flatMap(step -> {
          switch (step) {
            case SET_MY_AGE:
              return profileService.setAge(update);
            case SET_MATCH_MIN_AGE:
              return profileService.setMatchMinAge(update);
            case SET_MATCH_MAX_AGE:
              return profileService.setMatchMaxAge(update);
            case UNKNOWN:
            default:
              return messageService.unknownCommand(update);
          }
        });
    }
    switch (command) {
      case "/help":
      case "/start":
        return messageService.help(update);
      case "/overview":
        return messageService.overview(update);
      case "/profile":
        return profileService.setupProfile(update);
      case "/profile/me/gender":
        return questionService.askGender(update);
      case "/profile/me/gender/male":
        return profileService.setGender(update, Gender.MALE);
      case "/profile/me/gender/female":
        return profileService.setGender(update, Gender.FEMALE);
      case "/profile/me/age":
        return questionService.askAge(update);
      case "/profile/match/gender":
        return questionService.askMatchGender(update);
      case "/profile/match/gender/male":
        return profileService.setMatchGender(update, Gender.MALE);
      case "/profile/match/gender/female":
        return profileService.setMatchGender(update, Gender.FEMALE);
      case "/profile/match/gender/both":
        return profileService.setMatchGender(update, Gender.BOTH);
      case "/profile/match/age/min":
        return questionService.askMatchMinAge(update);
      case "/profile/match/age/max":
        return questionService.askMatchMaxAge(update);
      case "/profile/done":
        return profileService.leaveProfileConfiguration(update);
      case "/location":
        return profileService.setLocation(update);
      case "/contact":
        return profileService.setContact(update);
      case "/photo":
        return profileService.setImage(update);
      case "/roll":
        return matchService.suggest(update);
      default:
        String prefix = command.split("/")[1];
        switch (prefix) {
          case "like":
            return matchService.like(update);
          case "dislike":
            return matchService.dislike(update);
          default:
            return messageService.unknownCommand(update);
        }
    }
  }

  private String command(Update update) {
    CallbackQuery callback = update.callbackQuery();
    if (callback != null) {
      String callbackData = callback.data();
      return extractCommand(callbackData);
    }

    Message message = update.message();
    if (message != null) {

      String messageText = message.text();
      if (messageText != null) {
        return extractCommand(messageText);
      }

      Location location = message.location();
      if (location != null) {
        return "/location";
      }
      Contact contact = message.contact();
      if (contact != null) {
        return "/contact";
      }
      PhotoSize[] photo = message.photo();
      if (photo != null && photo.length > 0) {
        return "/photo";
      }
    }
    return null;
  }

  // TODO
  private String extractCommand(String text) {
    boolean isCommand = text != null && text.startsWith("/");
    if (!isCommand) {
      log.info("String {} is not a command", text);
    }
    return isCommand
      ? text
      : null;
  }
}
