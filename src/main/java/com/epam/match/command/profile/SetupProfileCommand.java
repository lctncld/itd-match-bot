package com.epam.match.command.profile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SetupProfileCommand {

  private final Long chatId;
}
