package com.epam.match;

import java.util.function.Function;

public final class Functions {

  public static Function<Boolean, Boolean> negate() {
    return exists -> !exists;
  }
}
