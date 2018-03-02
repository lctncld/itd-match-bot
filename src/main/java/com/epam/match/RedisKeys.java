package com.epam.match;

public final class RedisKeys {

  private static final String KEY = "users";

  public static String user(Integer id) {
    return KEY + ":" + id.toString();
  }

  public static String locations() {
    return "locations";
  }
}
