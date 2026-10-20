package net.cyperia.commons.common.collection;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static java.awt.Color.decode;

public final class Colors {

  private Colors() {
  }

  public static final Color
    ONLINE = decode("#40AC7B"),
    DO_NOT_DISTURB = decode("#E84444"),
    IDLE = decode("#F09F19"),
    OFFLINE = decode("#747F8D"),
    STREAMING = decode("#573591"),

  EMBED = decode("#2F3136"),
    NO_RANK = decode("#CCD8DE"),

  LIGHT_BLACK = decode("#1c1c1c");

  @NotNull
  public static String asHex(@NotNull Color color) {
    String red = Integer.toHexString(color.getRed());
    String green = Integer.toHexString(color.getGreen());
    String blue = Integer.toHexString(color.getBlue());
    return "#" + (red.length() == 1 ? "0" + red : red) +
      (green.length() == 1 ? "0" + green : green) +
      (blue.length() == 1 ? "0" + blue : blue);
  }


}
