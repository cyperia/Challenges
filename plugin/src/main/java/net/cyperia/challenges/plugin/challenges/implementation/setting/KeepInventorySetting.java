package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;

public class KeepInventorySetting extends Setting {

  @Override
  protected void onEnable() {
    for (World world : Bukkit.getWorlds()) {
      world.setGameRule(GameRule.KEEP_INVENTORY, true);
    }
  }

  @Override
  protected void onDisable() {
    for (World world : Bukkit.getWorlds()) {
      world.setGameRule(GameRule.KEEP_INVENTORY, false);
    }
  }
}
