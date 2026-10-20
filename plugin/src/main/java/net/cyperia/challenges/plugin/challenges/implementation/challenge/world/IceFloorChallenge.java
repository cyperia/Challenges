package net.cyperia.challenges.plugin.challenges.implementation.challenge.world;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class IceFloorChallenge extends Setting {

  private final List<Player> ignoredPlayers = new ArrayList<>();

  @Override
  protected void onEnable() {
    bossbar.setContent((bossbar, player) -> {
      bossbar.setTitle(getChallengeMessageKey("bossbar"), ignoreIce(player) ? MessageKey.of("generic.disabled") : MessageKey.of("generic.enabled"));
      bossbar.setColor(ignoreIce(player) ? BossBar.Color.RED : BossBar.Color.GREEN);
    });
    bossbar.show();
  }

  @Override
  protected void onDisable() {
    bossbar.hide();
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerMove(@NotNull PlayerMoveEvent event) {
    Player player = event.getPlayer();
    if (!shouldExecuteEffect(player)) return;
    if (ignoredPlayers.contains(player)) return;
    createIceFloorForPlayer(player);
  }

  private void createIceFloorForPlayer(@NotNull Player player) {
    Block middleBlock = player.getLocation().clone().subtract(0, 1, 0).getBlock();
    createIceFloor(middleBlock);
  }

  private void createIceFloor(@NotNull Block middleBlock) {

    for (int x = -1; x <= 1; x++) {
      for (int z = -1; z <= 1; z++) {
        Location iceLocation = middleBlock.getLocation().add(x, 0, z);
        Block iceBlock = iceLocation.getBlock();
        if (iceBlock.getType().isSolid()) continue;
        if (!iceBlock.getType().isAir()) {
          ChallengeHelper.breakBlock(iceBlock, new ItemStack(Material.AIR));
        }
        iceBlock.setType(Material.PACKED_ICE);
      }
    }

  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onSneak(@NotNull PlayerToggleSneakEvent event) {
    if (!event.isSneaking()) return;
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (ignoredPlayers.contains(event.getPlayer())) {
      ignoredPlayers.remove(event.getPlayer());
      event.getPlayer().setVelocity(new Vector(0, 0, 0));
      createIceFloorForPlayer(event.getPlayer());
    } else {
      ignoredPlayers.add(event.getPlayer());
    }
    bossbar.update(event.getPlayer());
  }

  private boolean ignoreIce(@NotNull Player player) {
    return ignoredPlayers.contains(player);
  }

}
