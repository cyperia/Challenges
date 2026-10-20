package net.cyperia.challenges.plugin.management.menu;

import org.bukkit.permissions.Permissible;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MenuManagerTest {

  @Test
  void everyoneMayChangeSettingsWhenNoPermissionIsRequired() {
    assertTrue(MenuManager.maySettingsChange(false, mock(Permissible.class)));
  }

  @Test
  void requiredPermissionGatesSettingsChanges() {
    Permissible without = mock(Permissible.class);
    Permissible with = mock(Permissible.class);
    when(with.hasPermission(MenuManager.MANAGE_GUI_PERMISSION)).thenReturn(true);

    assertFalse(MenuManager.maySettingsChange(true, without));
    assertTrue(MenuManager.maySettingsChange(true, with));
  }

}
