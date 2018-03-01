package com.epam.match.command.profile.my.age;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AskForMyAgeCommand {

  private final Long chatId;
}
