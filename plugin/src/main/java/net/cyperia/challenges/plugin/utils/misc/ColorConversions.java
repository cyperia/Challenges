package net.cyperia.challenges.plugin.utils.misc;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Deprecated
public final class ColorConversions {

  private static final Map<ChatColor, Color> colorsByChatColor = new HashMap<>();

  static {
    colorsByChatColor.put(ChatColor.BLACK, Color.decode("#000000"));
    colorsByChatColor.put(ChatColor.DARK_BLUE, Color.decode("#0000A8"));
    colorsByChatColor.put(ChatColor.DARK_GREEN, Color.decode("#00A800"));
    colorsByChatColor.put(ChatColor.DARK_AQUA, Color.decode("#00A8A8"));
    colorsByChatColor.put(ChatColor.DARK_RED, Color.decode("#A80000"));
    colorsByChatColor.put(ChatColor.DARK_PURPLE, Color.decode("#A800A8"));
    colorsByChatColor.put(ChatColor.GOLD, Color.decode("#FBA800"));
    colorsByChatColor.put(ChatColor.GRAY, Color.decode("#A8A8A8"));
    colorsByChatColor.put(ChatColor.DARK_GRAY, Color.decode("#545454"));
    colorsByChatColor.put(ChatColor.BLUE, Color.decode("#5454FB"));
    colorsByChatColor.put(ChatColor.GREEN, Color.decode("#54FB54"));
    colorsByChatColor.put(ChatColor.AQUA, Color.decode("#54FBFB"));
    colorsByChatColor.put(ChatColor.RED, Color.decode("#FB5454"));
    colorsByChatColor.put(ChatColor.LIGHT_PURPLE, Color.decode("#FB54FB"));
    colorsByChatColor.put(ChatColor.YELLOW, Color.decode("#FBFB54"));
    colorsByChatColor.put(ChatColor.WHITE, Color.decode("#FBFBFB"));
  }

  private ColorConversions() {
  }

  @NotNull
  public static ChatColor convertDyeColorToChatColor(@NotNull DyeColor color) {
    switch (color) {
      case RED:
        return ChatColor.RED;
      case BLUE:
        return ChatColor.DARK_BLUE;
      case CYAN:
        return ChatColor.DARK_AQUA;
      case GRAY:
        return ChatColor.DARK_GRAY;
      case LIME:
        return ChatColor.GREEN;
      case GREEN:
        return ChatColor.DARK_GREEN;
      case PURPLE:
        return ChatColor.DARK_PURPLE;
      case YELLOW:
        return ChatColor.YELLOW;
      case LIGHT_BLUE:
        return ChatColor.BLUE;
      case LIGHT_GRAY:
        return ChatColor.GRAY;
      case BLACK:
        return ChatColor.BLACK;
      case BROWN:
      case ORANGE:
        return ChatColor.GOLD;
      case PINK:
      case MAGENTA:
        return ChatColor.LIGHT_PURPLE;
      case WHITE:
      default:
        return ChatColor.WHITE;
    }
  }

  @NotNull
  public static Material convertDyeColorToMaterial(@NotNull DyeColor color) {
    switch (color) {
      case YELLOW:
        return Material.YELLOW_DYE;
      case RED:
        return Material.RED_DYE;
      case GREEN:
        return Material.GREEN_DYE;
      case BLACK:
        return Material.INK_SAC;
      case GRAY:
        return Material.GRAY_DYE;
      case LIGHT_GRAY:
        return Material.LIGHT_GRAY_DYE;
      case BLUE:
        return Material.LAPIS_LAZULI;
      case LIGHT_BLUE:
        return Material.LIGHT_BLUE_DYE;
      case MAGENTA:
        return Material.MAGENTA_DYE;
      case BROWN:
        return Material.COCOA_BEANS;
      case PURPLE:
        return Material.PURPLE_DYE;
      case ORANGE:
        return Material.ORANGE_DYE;
      case PINK:
        return Material.PINK_DYE;
      case LIME:
        return Material.LIME_DYE;
      case CYAN:
        return Material.CYAN_DYE;
      case WHITE:
      default:
        return Material.BONE_MEAL;
    }
  }

  @NotNull
  public static ChatColor convertAwtColorToChatColor(@NotNull Color color) {
    return colorsByChatColor.entrySet().stream()
      .min((o1, o2) -> (int) ((calculateDifferenceBetweenColors(color, o1.getValue()) - calculateDifferenceBetweenColors(color, o2.getValue())) * 100))
      .orElseThrow(() -> new IllegalStateException("Could not find a ChatColor for the given input"))
      .getKey();
  }

  @NotNull
  public static Color convertChatColorToAwtColor(@NotNull ChatColor color) {
    return Optional.ofNullable(colorsByChatColor.get(color)).orElseThrow(() -> new IllegalStateException("Could not find a color for ChatColor." + color.name()));
  }

  @NotNull
  public static float[] convertAwtColorToHSB(@NotNull Color color) {
    return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
  }

  public static double calculateDifferenceBetweenColors(@NotNull Color color1, @NotNull Color color2) {

    int diffRed = Math.abs(color1.getRed() - color2.getRed());
    int diffGreen = Math.abs(color1.getGreen() - color2.getGreen());
    int diffBlue = Math.abs(color1.getBlue() - color2.getBlue());

    float pctDiffRed = (float) diffRed / 255f;
    float pctDiffGreen = (float) diffGreen / 255f;
    float pctDiffBlue = (float) diffBlue / 255f;

    return (pctDiffRed + pctDiffGreen + pctDiffBlue) / 3f * 100;
  }

  public static boolean isValidColorCode(char code) {
    for (ChatColor color : ChatColor.values()) {
      if (color.isColor() && color.getChar() == code)
        return true;
    }
    return false;
  }

  public static boolean isValidColorCode(@NotNull String code) {
    if (code.length() != 1) return false;
    return isValidColorCode(code.toCharArray()[0]);
  }

}
