package net.cyperia.challenges.plugin.challenges.implementation.challenge.world;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.common.config.Document;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SnakeChallenge extends Setting {

  private final ArrayList<Block> blocks = new ArrayList<>();

  public SnakeChallenge() {
    setUsesGlobalPunish(true);
  }

  @Override
  protected void onDisable() {
    blocks.clear();
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    super.writeGameState(document);

    List<Location> locations = blocks.stream().map(Block::getLocation).toList();
    document.set("blocks", locations);
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    super.loadGameState(document);

    blocks.addAll(document.getSerializableList("blocks", Location.class).stream().map(Location::getBlock).toList());
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onMove(@NotNull PlayerMoveEvent event) {
    if (event.getTo() == null) return;
    if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;
    if (!shouldExecuteEffect(event.getPlayer())) return;

    Block from = event.getFrom().clone().subtract(0, 1, 0).getBlock();
    Block to = event.getTo().clone().subtract(0, 0.15, 0).getBlock();

    if (from.getType().isSolid()) {
      from.setType(BlockUtils.getTerracotta(getPlayersColor(event.getPlayer())), false);
      blocks.add(from);
    }

    if (blocks.contains(to)) {
      getChallengeMessageKey("failed").broadcast(Prefix.CHALLENGES, event.getPlayer());
      ChallengeHelper.punish(event.getPlayer());
      return;
    }

    if (to.getType().isSolid()) {
      to.setType(Material.BLACK_TERRACOTTA, false);

      Block block = event.getPlayer().getLocation().getBlock();
      if (!block.getType().isSolid()) {
        block.breakNaturally();
      }
    }

  }
//
//  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
//  public void onBreak(@NotNull BlockBreakEvent event) {
//    if (!shouldExecuteEffect()) return;
//    if (ignorePlayer(event.getPlayer())) return;
//
//    if (blocks.contains(event.getBlock())) {
//      event.setCancelled(true);
//      SoundSample.BASS_OFF.play(event.getPlayer());
//      getChallengeMessageKey("break").send(event.getPlayer(), Prefix.CHALLENGES);
//    }
//  }

  public int getPlayersColor(@NotNull Player player) {
    int i = 0;
    for (Player currentPlayer : ChallengeHelper.getIngamePlayers()) {
      i++;
      if (i > 17) i = 0;
      if (currentPlayer == player) return i;
    }
    return 0;
  }

}
