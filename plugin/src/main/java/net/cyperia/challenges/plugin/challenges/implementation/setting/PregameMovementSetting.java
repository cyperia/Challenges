package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class PregameMovementSetting extends Setting {

  public PregameMovementSetting() {
    super(true);
  }

  @EventHandler
  public void onMove(@NotNull PlayerMoveEvent event) {
    if (ChallengeAPI.isStarted() || isEnabled()) return;
    if (event.getPlayer().getGameMode() == GameMode.SPECTATOR || event.getPlayer().getGameMode() == GameMode.CREATIVE)
      return;

    Location to = event.getTo();
    if (to == null) return;
    if (BlockUtils.isSameLocationIgnoreHeight(event.getFrom(), to)) return;

    Location target = event.getFrom().clone();
    target.setY(target.getBlockY());

    BoundingBox playerBoundingBox = event.getPlayer().getBoundingBox();
    if (event.getPlayer().getLocation().equals(event.getTo())) {
      Vector shiftVector = event.getFrom().toVector().subtract(event.getPlayer().getLocation().toVector());
      playerBoundingBox = playerBoundingBox.clone().shift(shiftVector);
    }
    findMaxBlockHeightBelow(target, playerBoundingBox);

    target.setPitch(to.getPitch());
    target.setYaw(to.getYaw());
    event.setTo(target);

    if (ChallengeAPI.isCountdown()) {
      getChallengeMessageKey("title_countdown").sendTitleInstantly(event.getPlayer(), ChallengeAPI.formatTime(ChallengeAPI.getCountdownSecondsLeft()));
    } else {
      getChallengeMessageKey("title").sendTitleInstantly(event.getPlayer());
    }

  }

  private void findMaxBlockHeightBelow(@NotNull Location location, @NotNull BoundingBox playerBounds) {
    Location originalPlayerLocation = location.clone();

    while (location.getBlockY() > location.getWorld().getMinHeight()
      && (location.getBlock().isPassable() || getTrueStandY(location.getBlock(), originalPlayerLocation, playerBounds) == location.getBlockY())) {
      location.subtract(0, 1, 0);
    }

    location.setY(getTrueStandY(location.getBlock(), originalPlayerLocation, playerBounds));
  }

  public double getTrueStandY(@NotNull Block block, @NotNull Location playerLocation, @NotNull BoundingBox playerBounds) {
    // getCollisionShape() tracks the actual physical barriers (1.5 for walls/fences)
    Collection<BoundingBox> boxes = block.getCollisionShape().getBoundingBoxes();

    double playerHalfWidth = playerBounds.getWidthX() / 2.0;
    double playerHalfDepth = playerBounds.getWidthZ() / 2.0;

    double maxHeight = 0;
    for (BoundingBox box : boxes) {
      if (playerLocation.getX() - block.getX() + playerHalfWidth < box.getMinX()
        || playerLocation.getX() - block.getX() - playerHalfWidth > box.getMaxX()
        || playerLocation.getZ() - block.getZ() + playerHalfDepth < box.getMinZ()
        || playerLocation.getZ() - block.getZ() - playerHalfDepth > box.getMaxZ()) {
        continue; // collision box not applicable
      }

      double currentBoxAbsoluteTopY = box.getMaxY() + block.getY();
      if (currentBoxAbsoluteTopY > maxHeight) {
        maxHeight = currentBoxAbsoluteTopY;
      }
    }

    if (maxHeight == 0) { // passable block
      maxHeight = block.getY();
    }

    return maxHeight;
  }

}
