package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.*;

public class LocatorBarSetting extends Setting {

  public LocatorBarSetting() {
    super(true);
  }

  @Override
  protected void onEnable() {
    setLocatorBar(true);
  }

  @Override
  protected void onDisable() {
    setLocatorBar(false);
  }

  // the game rule is stored in the world itself, restore the vanilla state instead of leaving it disabled.
  // A regionised server disables plugins on its shutdown thread, where game rules cannot change.
  @Override
  public void handleShutdown() {
    if (Bukkit.isGlobalTickThread()) setLocatorBar(true);
  }

  private void setLocatorBar(boolean enabled) {
    Regions.global(plugin, () -> {
      for (World world : ChallengeAPI.getGameWorlds()) {
        world.setGameRule(GameRules.LOCATOR_BAR, enabled);
      }
    });
  }

}
