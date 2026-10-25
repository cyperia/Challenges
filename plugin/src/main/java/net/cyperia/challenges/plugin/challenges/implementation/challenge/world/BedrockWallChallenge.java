package net.cyperia.challenges.plugin.challenges.implementation.challenge.world;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BedrockWallChallenge extends SettingModifier {

  public BedrockWallChallenge() {
    super(1, 60, 30);
    setValueUnit(ValueUnit.SECONDS);
  }

  @EventHandler
  public void onMove(@NotNull PlayerMoveEvent event) {
    if (!shouldExecuteEffect()) return;
    if (event.getPlayer().getGameMode() == GameMode.CREATIVE || event.getPlayer().getGameMode() == GameMode.SPECTATOR)
      return;

    Location location = event.getTo();
    if (location == null) return;
    if (BlockUtils.isSameBlockLocationIgnoreHeight(event.getFrom(), location)) return;

    Regions.asyncLater(plugin, () -> {
      World world = event.getPlayer().getWorld();

      List<Block> blocks = new ArrayList<>();
      for (int y = world.getMinHeight() + 1; y < world.getMaxHeight(); y++) {
        Location blockLocation = location.clone();
        blockLocation.setY(y);
        blocks.add(blockLocation.getBlock());
      }

      Regions.nextTick(plugin, location, () -> {
        for (Block block : blocks) {
          block.setType(Material.BEDROCK, false);
        }
      });

    }, getValue() * 20L);

  }

}
