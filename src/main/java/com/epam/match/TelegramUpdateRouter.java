package com.epam.match;

import com.epam.match.service.ProfileService;
import com.epam.match.service.QuestionService;
import com.epam.match.service.SessionService;
import com.epam.match.service.StubService;
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

  private final StubService stubService;

  private final SessionService sessionService;

  public TelegramUpdateRouter(ProfileService profileService, QuestionService questionService, StubService stubService,
      SessionService sessionService) {
    this.profileService = profileService;
    this.questionService = questionService;
    this.stubService = stubService;
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
              case UNKNOWN:
              default:
                return stubService.unknownCommand(update);
            }
          })
          .then(sessionService.clear(userId.toString()));
    }
    switch (command) {
      case "/help":
        return stubService.help(update);
      case "/overview":
        return stubService.overview(update);
      case "/register":
        return profileService.setupProfile(update);
      case "/profile/me/gender":
        return questionService.askGender(update);
      case "/profile/me/gender/male":
      case "/profile/me/gender/female":
        return profileService.setGender(update);
      case "/profile/me/age":
        return questionService.askAge(update);
      case "/profile/match/gender":
        return questionService.askMatchGender(update);
      case "/profile/match/gender/male":
      case "/profile/match/gender/female":
      case "/profile/match/gender/both":
        return profileService.setMatchGender(update);
      case "/profile/match/age/min":
        return questionService.askMatchMinAge(update);
      case "/profile/match/age/max":
        return questionService.askMatchMaxAge(update);
      case "/location":
        return profileService.setLocation(update);
      default:
        return stubService.unknownCommand(update);
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
