package net.cyperia.challenges.plugin.challenges.implementation.goal;

import org.bukkit.Particle;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifierGoal;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import net.cyperia.challenges.plugin.utils.misc.ParticleUtils;
import net.cyperia.commons.common.collection.IRandom;
import net.cyperia.commons.common.config.Document;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class RaceGoal extends SettingModifierGoal {

  protected long seed = IRandom.create().getSeed();

  private Location goal;

  public RaceGoal() {
    super(1, 30, 5);
    setValueUnit(ValueUnit.BLOCKS.scaled(100).describedBy(getChallengeMessageKey("settings-desc")));
  }

  @Override
  public void getWinnersOnEnd(@NotNull List<Player> winners) {
  }

  @Override
  protected void onEnable() {
    reloadGoalLocation();
    bossbar.setContent((bar, player) -> {
      if (player.getWorld() == goal.getWorld()) {

        Location relativeGoal = goal.clone();
        relativeGoal.setY(player.getLocation().getY());
        relativeGoal.add(0.5, 0, 0.5);
        int distance = (int) Math.round(player.getLocation().distance(relativeGoal));

        bar.setColor(BossBar.Color.GREEN);
        bar.setTitle(getChallengeMessageKey("bossbar.distance"), goal.getBlockX(), goal.getBlockZ(), distance);
      } else {
        bar.setColor(BossBar.Color.RED);
        bar.setTitle(getChallengeMessageKey("bossbar.dimension"), goal.getBlockX(), goal.getBlockZ());
      }

    });
    bossbar.show();
  }

  @Override
  protected void onDisable() {
    bossbar.hide();
    goal = null;
  }

  @Override
  protected void onValueChange() {
    reloadGoalLocation();
    bossbar.update();
  }

  @ScheduledTask(ticks = 20)
  public void onSecond() {
    if (goal == null) return;
    bossbar.update();
    broadcastAudience(player -> {
      Location relativeGoal = goal.clone();
      relativeGoal.setY(player.getLocation().getY());
      relativeGoal.add(0.5, 0, 0.5);
      ParticleUtils.drawLine(player, player.getLocation(), relativeGoal, Particle.DUST,
        new DustOptions(Color.LIME, 1), 1, 0.5, 50);

      if (player.getWorld() != goal.getWorld()) return;
      if (player.getLocation().distance(relativeGoal) > 20) return;
      ParticleUtils.spawnParticleCylinderAroundRadius(Challenges.getInstance(), relativeGoal,
        Particle.INSTANT_EFFECT, Color.WHITE, 0.75, 0.5);
    });
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onMove(PlayerMoveEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (BlockUtils.isSameBlockLocationIgnoreHeight(event.getFrom(), event.getTo())) return;
    if (event.getTo().getWorld() != goal.getWorld()) return;
    bossbar.update(event.getPlayer());
    if (BlockUtils.isSameBlockLocationIgnoreHeight(event.getTo(), goal)) {
      getChallengeMessageKey("reached").broadcast(Prefix.CHALLENGES, event.getPlayer());
      ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_REACHED, () -> Collections.singletonList(event.getPlayer()));
      ParticleUtils.spawnParticleCylinderAroundRadius(Challenges.getInstance(), event.getTo(),
        Particle.ENTITY_EFFECT, null, 0.75, 2);
    }
  }

  private void reloadGoalLocation() {

    World world = ChallengeAPI.getGameWorld(Environment.NORMAL);
    int y = 999;

    IRandom iRandom = IRandom.create(seed);
    // Some more calculations to prevent the result cords to always end with the same digits.
    int max = getValue() * 100 - iRandom.nextInt(getValue() - getValue() / 2);

    int x = iRandom.nextInt(max * 2) - max;
    int z = iRandom.nextInt(max * 2) - max;
    goal = new Location(world, x, y, z);
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    if (!document.contains("seed")) {
      seed = IRandom.create().getSeed();
      return;
    }

    long seed = document.getLong("seed");
    if (this.seed == seed) return;
    this.seed = seed;

    if (!isEnabled()) return;
    reloadGoalLocation();
    bossbar.update();
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    document.set("seed", seed);
  }

}
