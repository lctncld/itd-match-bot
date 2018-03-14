package com.epam.match.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchProfile {

  private final Long age;

  private final Gender gender;

  private final Long matchMinAge;

  private final Long matchMaxAge;

  private final Gender matchGender;
}
