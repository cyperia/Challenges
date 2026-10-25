package net.cyperia.challenges.plugin.management.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Glyph advances of the vanilla {@code minecraft:default} font, in gui pixels: the glyph plus the one pixel of
 * spacing the client draws after it.
 */
final class MinecraftFont {

  static final int SPACE_WIDTH = 4;

  // Most glyphs of the unicode pages, small caps included
  private static final int FALLBACK_WIDTH = 6;
  private static final int BOLD_EXTRA_WIDTH = 1;
  private static final int[] ASCII_WIDTHS = new int[128];

  static {
    Arrays.fill(ASCII_WIDTHS, FALLBACK_WIDTH);
    fill(2, "!'.,:;i|");
    fill(3, "`l");
    fill(4, " *I[]t\"");
    fill(5, "(){}<>fk");
    fill(7, "@");
  }

  private MinecraftFont() {
  }

  private static void fill(int width, @NotNull String characters) {
    for (char character : characters.toCharArray()) {
      ASCII_WIDTHS[character] = width;
    }
  }

  // Only text is measured: a translated name has no width here, as its text is only known to the client
  static int widthOf(@NotNull Component component) {
    return widthOf(component, false);
  }

  private static int widthOf(@NotNull Component component, boolean parentBold) {
    TextDecoration.State state = component.decoration(TextDecoration.BOLD);
    boolean bold = state == TextDecoration.State.TRUE || state == TextDecoration.State.NOT_SET && parentBold;
    int width = 0;
    if (component instanceof TextComponent text) {
      String content = text.content();
      for (int i = 0; i < content.length(); i++) {
        char character = content.charAt(i);
        width += (character < ASCII_WIDTHS.length ? ASCII_WIDTHS[character] : FALLBACK_WIDTH) + (bold ? BOLD_EXTRA_WIDTH : 0);
      }
    }
    for (Component child : component.children()) {
      width += widthOf(child, bold);
    }
    return width;
  }

}
