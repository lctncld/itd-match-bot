package com.epam.match.service.telegram;

import com.epam.match.spring.MessageSourceAdapter;
import com.epam.match.spring.annotation.MessageMapping;
import com.epam.match.spring.annotation.TelegramBotController;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@TelegramBotController
public class SimpleReplyService {

  private final MessageSourceAdapter messageSource;

  public SimpleReplyService(MessageSourceAdapter messageSource) {
    this.messageSource = messageSource;
  }

  @MessageMapping("/unknown_command")
  public Mono<BaseRequest> unknownCommand(Update update) {
    Message message = update.message();

    if (message == null) {
      if (update.callbackQuery() != null) {
        message = update.callbackQuery().message();
      } else if (update.editedMessage() != null) {
        message = update.editedMessage();
      }
    }
    return Mono.just(
      new SendMessage(message.chat().id(), messageSource.get("unknown_command"))
        .replyMarkup(new ReplyKeyboardRemove())
    );
  }

  @MessageMapping("/help")
  public Mono<BaseRequest> help(Update update) {
    Long chatId = update.message().chat().id();
    return Mono.just(
      new SendMessage(chatId, messageSource.get("help")).replyMarkup(
        new InlineKeyboardMarkup(
          new InlineKeyboardButton[] {
            new InlineKeyboardButton(messageSource.get("help.button.title"))
              .callbackData("/overview"),
          })
      )
    );
  }

  @MessageMapping(value = "/overview")
  public Flux<BaseRequest> overview(Update update) {
    return Flux.just(
      new AnswerCallbackQuery(update.callbackQuery().id()),
      new SendMessage(update.callbackQuery().message().chat().id(), messageSource.get("description"))
    );
  }
}
