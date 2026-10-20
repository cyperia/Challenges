package net.cyperia.challenges.plugin.content.i18n;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PrefixTest {

  @Test
  void theFallbackKeepsTheBrandGradientInLegacyText() {
    // §x§5§b§3§f§e§4 is the gradient's first stop, #5B3FE4, on the C of Challenges
    assertTrue(Prefix.FALLBACK.contains("§x§5§b§3§f§e§4C"), Prefix.FALLBACK);
  }

}
