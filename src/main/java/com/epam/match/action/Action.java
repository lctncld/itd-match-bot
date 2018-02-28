package com.epam.match.action;

import com.pengrad.telegrambot.request.BaseRequest;

public interface Action {

  BaseRequest toCommand();
}
