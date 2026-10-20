package net.cyperia.challenges.plugin.utils.misc;

import org.bukkit.ChatColor;

import java.util.LinkedList;

public class FontUtils {

  private static final char[] SMALL_CAPS_ALPHABET = "ᴀʙᴄᴅᴇғɢʜɪᴊᴋʟᴍɴᴏᴘǫʀsᴛᴜᴠᴡxʏᴢ".toCharArray();

  public static String[] toSmallCaps(String[] text) {
    LinkedList<String> strings = new LinkedList<>();
    for (String s : text) {
      strings.add(toSmallCaps(s));
    }
    return strings.toArray(new String[0]);
  }

  public static String toSmallCaps(String text) {
    if (null == text) {
      return null;
    }
    int length = text.length();
    StringBuilder smallCaps = new StringBuilder(length);
    for (int i = 0; i < length; ++i) {
      char c = text.charAt(i);
      if (c >= 'a' && c <= 'z') {
        if (i >= 1) {
          char charBefore = text.charAt(i - 1);
          if (charBefore == '§') {
            if (ChatColor.getByChar(c) != null) {
              smallCaps.append(c);
              continue;
            }
          }
        }
        smallCaps.append(SMALL_CAPS_ALPHABET[c - 'a']);
      } else {
        smallCaps.append(c);
      }
    }
    return smallCaps.toString();
  }

}
