package net.cyperia.challenges.plugin.management.menu.generator.impl.custom;

import net.cyperia.challenges.plugin.challenges.custom.settings.SettingType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface IParentCustomGenerator {

  /**
   * @param player the player that has the menu open
   * @param type   the type of the current setting. Only needed if parent is the first setting menu.
   * @param data   a map that contains all the data of the settings
   */
  void accept(@NotNull Player player, @Nullable SettingType type, @NotNull Map<String, String[]> data);

  void decline(@NotNull Player player);

}
