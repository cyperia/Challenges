package net.cyperia.challenges.plugin.challenges.custom;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

class CustomChallengeTest {

  @Test
  void hasNoFallbackNamesWithoutACatalogEntry() {
    CustomChallenge challenge = mock(CustomChallenge.class, CALLS_REAL_METHODS);

    assertEquals(List.of(), challenge.getFallbackNames());
  }

}
