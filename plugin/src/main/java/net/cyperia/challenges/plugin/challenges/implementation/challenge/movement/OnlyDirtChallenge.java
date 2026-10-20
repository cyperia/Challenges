package net.cyperia.challenges.plugin.challenges.implementation.challenge.movement;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class OnlyDirtChallenge extends Setting {

  public OnlyDirtChallenge() {
    setUsesGlobalPunish(true);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerMove(@NotNull PlayerMoveEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getTo() == null) return;

    Block blockBelow = BlockUtils.getBlockBelow(event.getTo());
    if (blockBelow == null) return;
    if (blockBelow.getType() != Material.DIRT && !blockBelow.getType().isAir()) {
      getChallengeMessageKey("failed").broadcast(Prefix.CHALLENGES, event.getPlayer());
      ChallengeHelper.punish(event.getPlayer());
    }

  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onBlockSpread(@NotNull BlockSpreadEvent event) {
    if (!shouldExecuteEffect()) return;

    if (event.getNewState().getType() == Material.GRASS_BLOCK) {
      event.setCancelled(true);
    }

  }

}
