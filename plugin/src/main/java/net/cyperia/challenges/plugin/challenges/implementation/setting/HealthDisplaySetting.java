package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.management.server.scoreboard.PlayerScoreboards;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;

public class HealthDisplaySetting extends Setting {

  public static final String OBJECTIVE_NAME = "health_display";

  public HealthDisplaySetting() {
    super(true);
  }

  @Override
  protected void onEnable() {
    broadcastAudience(this::show);
  }

  @Override
  protected void onDisable() {
    broadcastAudience(this::hide);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onJoin(@NotNull PlayerJoinEvent event) {
    if (isEnabled()) {
      show(event.getPlayer());
    } else {
      hide(event.getPlayer());
    }
  }

  private void show(@NotNull Player player) {
    PlayerScoreboards.use(player, scoreboard -> {
      Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
      if (objective == null)
        objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, "health", OBJECTIVE_NAME);
      objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);

      objective.setRenderType(RenderType.HEARTS);
      objective.setAutoUpdateDisplay(true);

      try {
        for (Player current : plugin.getPlayerManager().getAudience()) {
          objective.getScore(current.getName()).setScore((int) current.getHealth());
        }
      } catch (IllegalStateException _) {
        // the health objective can be read-only
      }
    });
  }

  private void hide(@NotNull Player player) {
    PlayerScoreboards.useExisting(player, scoreboard -> {
      Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
      if (objective == null) return;

      try {
        objective.unregister();
      } catch (Exception ex) {
        Challenges.getInstance().getILogger().severe("Error while unregistering tablist hearts objective");
      }
    });
  }

}
