package com.epam.match.command.profile.my.age;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SetMyAgeCommand {

  private final Integer userId;

  private final Long chatId;

  private final Integer age;
}
