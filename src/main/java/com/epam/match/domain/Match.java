package com.epam.match.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Match {
  private final String id;
  private final String name;
  private final String image;
}
