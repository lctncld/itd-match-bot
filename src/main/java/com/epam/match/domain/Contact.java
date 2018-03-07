package com.epam.match.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Contact {

  private final String firstName;

  private final String lastName;

  private final String chatId;

  private final String phone;
}
