package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.utils.bukkit.command.PlayerCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TeamsCommand implements PlayerCommand {

  @Override
  public void onCommand(@NotNull Player player, @NotNull String[] args) throws Exception {
    MenuType.TEAMS.getMenuGenerator().openMenu(player);
  }

}
