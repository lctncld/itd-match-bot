package com.epam.match.command.profile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SetLocationCommand {

  private final Long chatId;

  private final Float longitude;

  private final Float latitude;
}
