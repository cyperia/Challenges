package net.cyperia.challenges.plugin.content.i18n.impl;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmptyTranslationTest {

  private static boolean isEmpty(String... lines) {
    MessageKeyImpl key = new MessageKeyImpl("test");
    key.setValue(Locale.ENGLISH, lines);
    return key.isEmptyTranslation(Locale.ENGLISH);
  }

  @Test
  void emptyLinesAreEmpty() {
    assertTrue(isEmpty(""));
    assertTrue(isEmpty("", ""));
  }

  @Test
  void spacerAndTextAreNotEmpty() {
    assertFalse(isEmpty(" "));
    assertFalse(isEmpty("", "text"));
  }

  @Test
  void missingTranslationIsNotEmpty() {
    assertFalse(new MessageKeyImpl("test").isEmptyTranslation(Locale.ENGLISH));
  }

}
