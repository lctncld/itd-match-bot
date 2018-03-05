package com.epam.match;

public final class RedisKeys {

  public static String user(Integer id) {
    return id.toString() + ":profile";
  }

  public static String session(Integer id) {
    return id.toString() + ":session";
  }

  public static String locations() {
    return "locations";
  }

  public static String phone(Integer id) {
    return id.toString() + ":phone";
  }
}
