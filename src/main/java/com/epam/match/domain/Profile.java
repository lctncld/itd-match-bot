package com.epam.match.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@Builder
public class Profile {

  private final Optional<Integer> age;

  private final Optional<Gender> gender;

  private final Optional<Integer> matchMinAge;

  private final Optional<Integer> matchMaxAge;

  private final Optional<Gender> matchGender;
}
