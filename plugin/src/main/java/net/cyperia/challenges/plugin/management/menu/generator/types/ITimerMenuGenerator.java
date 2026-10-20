package net.cyperia.challenges.plugin.management.menu.generator.types;

import net.cyperia.challenges.plugin.management.menu.generator.IMenuGenerator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface ITimerMenuGenerator extends IMenuGenerator {

  void openCountdownMenu(@NotNull Player player);

  void openTimeMenu(@NotNull Player player);

}
