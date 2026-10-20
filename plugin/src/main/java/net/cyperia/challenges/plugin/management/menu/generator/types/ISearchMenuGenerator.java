package net.cyperia.challenges.plugin.management.menu.generator.types;

import net.cyperia.challenges.plugin.management.menu.generator.IMenuGenerator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface ISearchMenuGenerator extends IMenuGenerator {

  void openMenu(@NotNull Player player, @NotNull String query);

}
