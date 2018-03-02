package com.epam.match;

import com.epam.match.domain.Gender;
import com.epam.match.service.MessageService;
import com.epam.match.service.ProfileService;
import com.epam.match.service.QuestionService;
import com.epam.match.service.SessionService;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Location;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class TelegramUpdateRouter {

  private final ProfileService profileService;

  private final QuestionService questionService;

  private final MessageService messageService;

  private final SessionService sessionService;

  public TelegramUpdateRouter(ProfileService profileService, QuestionService questionService,
      MessageService messageService,
      SessionService sessionService) {
    this.profileService = profileService;
    this.questionService = questionService;
    this.messageService = messageService;
    this.sessionService = sessionService;
  }

  public Mono<?> route(Update update) {
    String command = command(update);
    if (command == null) {
      Integer userId = update.message().from().id();
      return sessionService.get(userId.toString())
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
          })
          .then(sessionService.clear(userId.toString()));
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
      default:
        return messageService.unknownCommand(update);
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
    }
    return null;
  }

  // TODO
  private String extractCommand(String text) {
    boolean isCommand = text != null && text.startsWith("/");
    if (!isCommand) {
      log.info("String {} is not a command", text);
    }
    return isCommand ? text : null;
  }
}
