package com.epam.match.domain;

import com.epam.match.EmojiResolver;

public enum Gender {
  MALE(EmojiResolver.man()),
  FEMALE(EmojiResolver.woman()),
  BOTH(EmojiResolver.both());

  private final String emoji;

  Gender(String emoji) {
    this.emoji = emoji;
  }

  public String getEmoji() {
    return emoji;
  }
}
