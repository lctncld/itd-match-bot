package com.epam.match;

public final class RedisKeys {

  public static String user(Integer id) {
    return "users:" + id.toString();
  }

  public static String session(Integer id) {
    return "session:" + id.toString();
  }

  public static String locations() {
    return "locations";
  }
}
