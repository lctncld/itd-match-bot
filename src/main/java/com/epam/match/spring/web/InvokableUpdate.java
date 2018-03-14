package com.epam.match.spring.web;

import com.pengrad.telegrambot.model.Update;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.method.HandlerMethod;

@Data
@Builder
public class InvokableUpdate {

  private final HandlerMethod method;

  private final Update update;
}
