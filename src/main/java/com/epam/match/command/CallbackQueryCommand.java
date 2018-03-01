package com.epam.match.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CallbackQueryCommand {

  private final String queryId;
}
