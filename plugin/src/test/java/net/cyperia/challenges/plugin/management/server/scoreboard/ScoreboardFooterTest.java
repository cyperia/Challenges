package net.cyperia.challenges.plugin.management.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreboardFooterTest {

  @Test
  void textIsMeasuredPerGlyph() {
    assertEquals(6 + 2 + 3, MinecraftFont.widthOf(Component.text("a.l")));
  }

  @Test
  void boldAddsAPixelPerGlyphAndIsInherited() {
    Component bold = Component.text("a").decorate(TextDecoration.BOLD).append(Component.text("b"));
    assertEquals(7 + 7, MinecraftFont.widthOf(bold));
  }

  @Test
  void theFooterIsPaddedToTheMiddleOfTheWidestLine() {
    Component title = Component.text("x".repeat(20)); // 120 px
    Component footer = Component.text("y".repeat(10)); // 60 px, 30 px either side
    Component centered = ChallengeScoreboard.centered(footer, title, List.of(Component.text("short")));
    assertEquals(Component.text(" ".repeat(7)).append(footer), centered);
  }

  @Test
  void aFooterWiderThanEveryLineStaysAsItIs() {
    Component footer = Component.text("y".repeat(10));
    assertEquals(footer, ChallengeScoreboard.centered(footer, Component.text("x"), List.of()));
  }

}
