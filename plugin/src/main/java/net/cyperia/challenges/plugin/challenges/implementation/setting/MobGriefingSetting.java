package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;

public class MobGriefingSetting extends Setting {

  @Override
  protected void onEnable() {
    for (World world : Bukkit.getWorlds()) {
      world.setGameRule(GameRule.MOB_GRIEFING, false);
    }
  }

  @Override
  protected void onDisable() {
    for (World world : Bukkit.getWorlds()) {
      world.setGameRule(GameRule.MOB_GRIEFING, true);
    }
  }
}
