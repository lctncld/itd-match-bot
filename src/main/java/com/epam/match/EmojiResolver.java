package com.epam.match;

import com.vdurmont.emoji.EmojiManager;

public final class EmojiResolver {

  public static String man() {
    return EmojiManager.getForAlias("man").getUnicode();
  }

  public static String woman() {
    return EmojiManager.getForAlias("woman").getUnicode();
  }

  public static String both() {
    return EmojiManager.getForAlias("busts_in_silhouette").getUnicode();
  }
}
