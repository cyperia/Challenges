package net.cyperia.challenges.plugin.management.menu.generator.impl;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.menu.generator.NoOpMenuGenerator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TeamsMenuGenerator extends NoOpMenuGenerator {

  @Override
  public void playDenyEffect(@NotNull Player player) {
    super.playDenyEffect(player);
    MessageKey.of("menu.teams.coming_soon").send(player, Prefix.CHALLENGES);
  }
}
