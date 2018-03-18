package com.epam.match;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageSourceAdapter {

  private final MessageSource messageSource;

  public MessageSourceAdapter(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public String get(String code) {
    return messageSource.getMessage(code, new Object[] {}, Locale.getDefault());
  }

  public String get(String code, String... args) {
    return messageSource.getMessage(code, args, Locale.getDefault());
  }
}
