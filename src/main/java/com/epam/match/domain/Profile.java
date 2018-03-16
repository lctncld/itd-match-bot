package com.epam.match.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@Builder
public class Profile {

  private final String id;

  private final Optional<Integer> age;

  private final Optional<Gender> gender;

  private final Optional<Integer> matchMinAge;

  private final Optional<Integer> matchMaxAge;

  private final Optional<Gender> matchGender;

  public static Profile empty(String id) {
    return Profile.builder()
      .id(id)
      .age(Optional.empty())
      .gender(Optional.empty())
      .matchMinAge(Optional.empty())
      .matchMaxAge(Optional.empty())
      .matchGender(Optional.empty())
      .build();
  }
}
