package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.utils.bukkit.command.PlayerCommand;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VillageCommand implements PlayerCommand {

  @Override
  public void onCommand(@NotNull Player player, @NotNull String[] args) throws Exception {
    player.setNoDamageTicks(10);
    MessageKey.of("command.village.searching").send(player, Prefix.CHALLENGES);

    Bukkit.getScheduler().runTask(Challenges.getInstance(), () -> {

      Location village = player.getWorld().locateNearestStructure(player.getLocation(), StructureType.VILLAGE, 5000, true);
      if (village == null) {
        MessageKey.of("command.village.none").send(player, Prefix.CHALLENGES);
        return;
      }

      village = player.getWorld().getHighestBlockAt(village).getLocation().add(0.5, 1, 0.5);
      village.getChunk().load(true);

      Location finalVillage = village;
      Bukkit.getScheduler().runTaskLater(Challenges.getInstance(), () -> {
        player.teleport(finalVillage);
        SoundSample.TELEPORT.play(player);
        MessageKey.of("command.village.done").send(player, Prefix.CHALLENGES);
      }, 20 /* run after 1 second to give the chunks/world time to load/generate */);

    });

  }

}
