package net.cyperia.challenges.plugin.challenges.type.abstraction;

import lombok.Setter;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import org.bukkit.World.Environment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class KillEntityGoal extends SettingGoal {

  protected final EntityType entity;
  protected final Environment environment;
  @Setter
  protected boolean oneWinner = true;
  protected boolean killerNeeded = false;
  protected Player winner;

  public KillEntityGoal(@NotNull EntityType entity) {
    this(entity, false);
  }

  public KillEntityGoal(@NotNull EntityType entity, boolean enabledByDefault) {
    this(entity, null, enabledByDefault);
  }

  public KillEntityGoal(@NotNull EntityType entity, @Nullable Environment world) {
    this(entity, world, false);
  }

  public KillEntityGoal(@NotNull EntityType entity, @Nullable Environment world, boolean enabledByDefault) {
    super(enabledByDefault);
    this.entity = entity;
    this.environment = world;
  }

  @Override
  public void getWinnersOnEnd(@NotNull List<Player> winners) {
    if (oneWinner && winner != null) winners.add(winner);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onKill(@NotNull EntityDeathEvent event) {
    if (!isEnabled() || !ChallengeAPI.isStarted()) return;
    LivingEntity entity = event.getEntity();
    if (entity.getType() != this.entity) return;
    if (environment != null && event.getEntity().getWorld().getEnvironment() != environment)
      return;
    if (oneWinner) {
      winner = event.getEntity().getKiller();
      if (killerNeeded && winner == null) {
        return;
      }
    }
    ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_REACHED);
  }

}
