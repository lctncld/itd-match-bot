package com.epam.match;

public final class RedisKeys {

  public static String user(Object id) {
    return id + ":profile";
  }

  public static String session(Object id) {
    return id + ":session";
  }

  public static String locations() {
    return "locations";
  }

  public static String image(Object id) {
    return id + ":image";
  }

  public static String likes(Object id) {
    return id + ":likes";
  }

  public static String dislikes(Integer id) {
    return id + ":dislikes";
  }

  public static String contact(Object id) {
    return id + ":contact";
  }

  public static final class Contact {

    public static String phone(Object id) {
      return id + ":contact:phone";
    }

    public static String firstName(Object id) {
      return id + ":contact:first_name";
    }

    public static String lastName(Object id) {
      return id + ":contact:last_name";
    }

    public static String chatId(Object id) {
      return id + ":contact:chat_id";
    }
  }
}
