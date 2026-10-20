package net.cyperia.challenges.plugin.content.i18n.impl.format;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComponentFormatterChatLinesTest {

  @Test
  void prefixesEveryLineButBlankOnes() {
    List<Component> lines = ComponentFormatter.deserializeChatLinesWithArgs("P ", new String[]{"one", " ", "two"}, new Object[0]);
    assertEquals(List.of("P one", " ", "P two"), lines.stream().map(ComponentFormatter::serializePlainText).toList());
  }

  @Test
  void splitsAMultiLineArgumentIntoLines() {
    List<Component> lines = ComponentFormatter.deserializeChatLinesWithArgs("P ", new String[]{"a {0}"}, new Object[]{"b\nc"});
    assertEquals(List.of("P a b", "P c"), lines.stream().map(ComponentFormatter::serializePlainText).toList());
  }

  @Test
  void joinsTheSameLinesWithNewlinesAsOneComponent() {
    Component joined = ComponentFormatter.deserializeLines("P ", new String[]{"one", "two"});
    assertEquals("P one\nP two", ComponentFormatter.serializePlainText(joined));
  }

}
