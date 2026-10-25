package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VillageCommand implements ChallengeCommand {

  @NotNull
  @Override
  public CommandInfo info() {
    return CommandInfo.command("village", ChallengesPermissions.COMMAND_VILLAGE);
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    root.executes(nodes.runAsPlayer((player, context) -> teleportToVillage(player)));
  }

  private static void teleportToVillage(@NotNull Player player) {
    player.setNoDamageTicks(10);
    MessageKey.of("command.village.searching").send(player, Prefix.CHALLENGES);
    Regions.nextTick(Challenges.getInstance(), player, () -> {
      World world = player.getWorld();
      Location village = world.locateNearestStructure(player.getLocation(), StructureType.VILLAGE, 5000, true);
      if (village == null) {
        MessageKey.of("command.village.none").send(player, Prefix.CHALLENGES);
        return;
      }

      world.getChunkAtAsync(village).thenAccept(chunk -> Regions.at(Challenges.getInstance(), village, () -> {
        Location target = world.getHighestBlockAt(village).getLocation().add(0.5, 1, 0.5);
        Regions.later(Challenges.getInstance(), player, () -> player.teleportAsync(target).thenAccept(success -> {
          SoundSample.TELEPORT.play(player);
          MessageKey.of("command.village.done").send(player, Prefix.CHALLENGES);
        }), 20 /* run after 1 second to give the chunks/world time to load/generate */);
      }));
    });
  }

}
