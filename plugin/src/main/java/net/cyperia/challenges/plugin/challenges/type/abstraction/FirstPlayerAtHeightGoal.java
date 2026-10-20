package net.cyperia.challenges.plugin.challenges.type.abstraction;

import lombok.Getter;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@Getter
public abstract class FirstPlayerAtHeightGoal extends SettingGoal {

  private final int worldHeightDifference;
  private int heightToGetTo;

  public FirstPlayerAtHeightGoal() {
    worldHeightDifference = calcWorldHeightDifference();
  }

  private int calcWorldHeightDifference() {
    World world = ChallengeAPI.getGameWorld(World.Environment.NORMAL);
    int max = world.getMaxHeight();
    int min = world.getMinHeight();
    return max - min;
  }

  @Override
  protected void onEnable() {
    bossbar.setContent((bar, player) -> {
      float progress = Math.clamp((float) player.getLocation().getY() / heightToGetTo, 0, 1);
      bar.setColor(BossBar.Color.GREEN);
      bar.setProgress(progress);
      bar.setTitle(MessageKey.of("challenge.generic.bossbar-height"), getHeightToGetTo());
    });
    bossbar.show();
  }

  @Override
  protected void onDisable() {
    bossbar.hide();
  }

  @Override
  public void getWinnersOnEnd(@NotNull List<Player> winners) {
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onMove(PlayerMoveEvent event) {
    if (event.getTo() == null) return;
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getTo().getBlockY() == event.getFrom().getBlockY()) return;
    bossbar.update(event.getPlayer());
    if (event.getTo().getBlockY() == heightToGetTo) {
      MessageKey.of("challenge.generic.height-reached").broadcast(Prefix.CHALLENGES, event.getPlayer(), getHeightToGetTo());
      ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_REACHED, () -> Collections.singletonList(event.getPlayer()));
    }
  }

  @NotNull
  @Override
  public LocalizableMessage getDescription() {
    return super.getDescription().withArgs(heightToGetTo);
  }

  protected void setHeightToGetTo(int heightToGetTo) {
    this.heightToGetTo = heightToGetTo;
  }

}
