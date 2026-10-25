package net.cyperia.challenges.plugin.spigot.region;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RespawnPortalAdaptersTest {

  @Test
  void withoutCanvasTheBukkitAdapterIsUsed() {
    assertFalse(RespawnPortalAdapters.isClassPresent(RespawnPortalAdapters.CANVAS_EVENT));
    assertInstanceOf(BukkitRespawnPortalAdapter.class, RespawnPortalAdapters.create());
  }

}
