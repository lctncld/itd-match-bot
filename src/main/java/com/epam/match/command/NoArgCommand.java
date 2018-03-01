package com.epam.match.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NoArgCommand {

  private final Long chatId;
}
