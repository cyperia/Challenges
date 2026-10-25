package net.cyperia.challenges.plugin.challenges.type;

import net.cyperia.challenges.catalog.Challenge;
import net.cyperia.challenges.catalog.Definition;
import net.cyperia.challenges.catalog.Icon;
import net.cyperia.challenges.catalog.Release;
import org.bukkit.permissions.Permissible;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EntryAccessTest {

  private static final String PERMISSION = "challenges.play.test";

  @Test
  void anEntryWithoutPermissionIsOpenToEveryone() {
    Challenge open = entry(null, false);
    Permissible player = mock(Permissible.class);

    assertFalse(EntryAccess.isLocked(open, player));
    assertTrue(EntryAccess.isListed(open, player));
  }

  @Test
  void anEntryIsLockedWithoutItsPermission() {
    Challenge gated = entry(PERMISSION, false);

    assertTrue(EntryAccess.isLocked(gated, mock(Permissible.class)));
    assertFalse(EntryAccess.isLocked(gated, withPermission()));
  }

  @Test
  void aLockedEntryIsStillListedUnlessHidden() {
    Permissible player = mock(Permissible.class);

    assertTrue(EntryAccess.isListed(entry(PERMISSION, false), player));
    assertFalse(EntryAccess.isListed(entry(PERMISSION, true), player));
  }

  @Test
  void aHiddenEntryIsListedForWhoHasThePermission() {
    assertTrue(EntryAccess.isListed(entry(PERMISSION, true), withPermission()));
  }

  private static Permissible withPermission() {
    Permissible player = mock(Permissible.class);
    when(player.hasPermission(PERMISSION)).thenReturn(true);
    return player;
  }

  private static Challenge entry(String permission, boolean hidden) {
    return new Challenge("test", java.util.List.of(), "test", "misc_goal", new Icon("STONE", null),
      new Definition.Implementation("Test", null), null, false, false, true, new Release(null, null, false), permission, hidden);
  }

}
