package net.cyperia.challenges.plugin.management.menu.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class MenuBackgroundTest {

  @Test
  void contrastSitsInTopLeftAndBottomRightCorners() {
    assertArrayEquals(new int[]{0, 1, 2, 9, 42, 43, 44, 35}, MenuBackground.contrastSlots(5 * 9));
    assertArrayEquals(new int[]{0, 1, 2, 9, 24, 25, 26, 17}, MenuBackground.contrastSlots(3 * 9));
  }

  @Test
  void tooSmallForCornersHasNoContrast() {
    assertArrayEquals(new int[0], MenuBackground.contrastSlots(2 * 9));
  }

}
