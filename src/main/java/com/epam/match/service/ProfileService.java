package com.epam.match.service;

import com.epam.match.command.profile.my.age.AskForMyAgeCommand;
import com.epam.match.command.profile.SetupProfileCommand;
import com.epam.match.command.profile.SetLocationCommand;
import com.epam.match.command.profile.my.age.SetMyAgeCommand;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ProfileService {

  private final TelegramBot bot;

  private final RedisReactiveCommands<String, String> commands;

  public ProfileService(TelegramBot bot, RedisReactiveCommands<String, String> commands) {
    this.bot = bot;
    this.commands = commands;
  }

  public Mono<Void> setupProfile(SetupProfileCommand command) {
    SendMessage sendMenuCommand = new SendMessage(command.getChatId(), "TODO: get current profile")
        .replyMarkup(new InlineKeyboardMarkup(
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
                    .callbackData("/profile/match/age/from"),
                new InlineKeyboardButton("Max age")
                    .callbackData("/profile/match/age/to")
            },
            new InlineKeyboardButton[] {
                new InlineKeyboardButton("No more changes needed")
                    .callbackData("/profile/done")
            }
        ));
    return Mono.just(sendMenuCommand)
        .map(bot::execute)
        .then();
  }

  public Mono<Void> askAge(AskForMyAgeCommand command) {
    SendMessage cmd = new SendMessage(command.getChatId(), "So, what's your age?");
    return Mono.just(cmd)
        .map(bot::execute)
        .then();
  }

  public Mono<Void> setAge(SetMyAgeCommand command) {
    return commands.hset("users", command.getUserId().toString(), command.getAge().toString())
        .thenReturn(new SendMessage(command.getChatId(), String.format("Okay, your age is %s", command.getAge())))
        .map(a -> bot.execute(a))
        .then();
  }

  public Mono<Void> setLocation(SetLocationCommand command) {
    SendMessage cmd = new SendMessage(command.getChatId(), "Your location is updated");
    return Mono.just(cmd)
        .map(bot::execute)
        .then();
  }
}
