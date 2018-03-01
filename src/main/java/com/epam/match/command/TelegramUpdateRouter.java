package com.epam.match.command;

import com.epam.match.command.profile.SetLocationCommand;
import com.epam.match.command.profile.SetupProfileCommand;
import com.epam.match.command.profile.my.age.AskForMyAgeCommand;
import com.epam.match.command.profile.my.age.SetMyAgeCommand;
import com.epam.match.service.ProfileService;
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

  private final StubService stubService;

  private final SessionService sessionService;

  public TelegramUpdateRouter(ProfileService profileService, StubService stubService, SessionService sessionService) {
    this.profileService = profileService;
    this.stubService = stubService;
    this.sessionService = sessionService;
  }

  public Mono<?> route(Update update) {
    String command = command(update);
    Long chatId = chatId(update);
    if (command == null) {
      Integer userId = update.message().from().id();
      return sessionService.get(userId.toString())
          .flatMap(step -> {
            switch (step) {
              case SET_MY_AGE:
                Message message = update.message();
                SetMyAgeCommand cmd = SetMyAgeCommand.builder()
                    .chatId(message.chat().id())
                    .userId(message.from().id())
                    .age(Integer.valueOf(message.text()))
                    .build();
                return profileService.setAge(cmd);
              case UNKNOWN:
              default:
                return stubService.unknownCommand(UnrecognizedCommand.builder()
                    .chatId(chatId)
                    .text(update.message().text())
                    .build());
            }
          })
          .then(sessionService.clear(userId.toString()));
    }
    switch (command) {
      case "/help":
        return stubService.help(NoArgCommand.builder()
            .chatId(chatId)
            .build()
        );
      case "/overview":
        CallbackQueryCommand queryCommand = CallbackQueryCommand.builder()
            .queryId(update.callbackQuery().id())
            .build();
        return stubService.overview(queryCommand);
      case "/register":
        SetupProfileCommand cmd = SetupProfileCommand.builder()
            .chatId(chatId)
            .build();
        return profileService.setupProfile(cmd);
      case "/profile/me/age":
        AskForMyAgeCommand cmd1 = AskForMyAgeCommand.builder()
            .chatId(chatId)
            .userId(update.callbackQuery().from().id())
            .build();
        return profileService.askAge(cmd1);
      case "/location":
        Location location = update.message().location();
        SetLocationCommand setLocationCommand = SetLocationCommand.builder()
            .chatId(chatId)
            .latitude(location.latitude())
            .longitude(location.longitude())
            .build();
        return profileService.setLocation(setLocationCommand);
      default:
        UnrecognizedCommand cmd2 = UnrecognizedCommand.builder()
            .chatId(chatId)
            .build();
        return stubService.unknownCommand(cmd2);
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

  private Long chatId(Update update) {
    CallbackQuery callback = update.callbackQuery();
    if (callback != null) {
      return callback.message().chat().id();
    }
    Message message = update.message();
    if (message != null) {
      return message.chat().id();
    }
    throw new RuntimeException("Both callback and message are null");
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
