package com.epam.match.spring.web;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Contact;
import com.pengrad.telegrambot.model.Location;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Update;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class CommandExtractor {

  public String getCommand(Update update) {
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
      ? stripPathVariable(text)
      : null;
  }

  private String stripPathVariable(String command) {
    boolean hasPathVariable = command.matches("(/.+/)(\\d+)");
    return hasPathVariable
      ? command.replaceFirst("(\\d+)", "")
      : command;
  }
}
