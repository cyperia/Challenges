package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.challenges.type.abstraction.PointsGoal;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

public class MostOresGoal extends PointsGoal {

  private int getPointsForOre(Material material) {
    switch (material) {
      case EMERALD_ORE:
      case DEEPSLATE_EMERALD_ORE:
        return 15;
      case DIAMOND_ORE:
      case DEEPSLATE_DIAMOND_ORE:
        return 10;
      case LAPIS_ORE:
      case DEEPSLATE_LAPIS_ORE:
        return 8;
      case GOLD_ORE:
      case DEEPSLATE_GOLD_ORE:
        return 6;
      case IRON_ORE:
      case DEEPSLATE_IRON_ORE:
        return 4;
      case COAL_ORE:
      case DEEPSLATE_COAL_ORE:
      case REDSTONE_ORE:
      case DEEPSLATE_REDSTONE_ORE:
        return 2;
      default:
        return 0;
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onUpdate(@NotNull BlockBreakEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    int points = getPointsForOre(event.getBlock().getType());
    if (points > 0) {
      MessageKey.of("challenge.generic.points").send(event.getPlayer(), Prefix.CHALLENGES, "+" + points);
      SoundSample.PLING.play(event.getPlayer());
      addPoints(event.getPlayer().getUniqueId(), points);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onUpdate(@NotNull BlockPlaceEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    int points = getPointsForOre(event.getBlock().getType());
    if (points > 0) {
      SoundSample.BASS_OFF.play(event.getPlayer());
      MessageKey.of("challenge.generic.points").send(event.getPlayer(), Prefix.CHALLENGES, "-" + points);
      removePoints(event.getPlayer().getUniqueId(), points);
    }
  }

}
