package net.cyperia.challenges.plugin.challenges.implementation.setting;

import org.bukkit.GameRule;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class ImmediateRespawnSetting extends Setting {

  @Override
  protected void onEnable() {
    for (World world : Bukkit.getWorlds()) {
      world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
    }
  }

  @Override
  protected void onDisable() {
    for (World world : Bukkit.getWorlds()) {
      world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false);
    }
  }

}
