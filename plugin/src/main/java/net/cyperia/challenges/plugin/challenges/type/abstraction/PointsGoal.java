package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.challenges.type.helper.GoalHelper;
import net.cyperia.challenges.plugin.challenges.type.helper.GoalLeaderboard;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import net.cyperia.commons.common.config.Document;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PointsGoal extends SettingGoal {

  private final Map<UUID, Integer> points = new HashMap<>();

  public PointsGoal() {
  }

  public PointsGoal(boolean enabledByDefault) {
    super(enabledByDefault);
  }

  @Override
  protected void onEnable() {
    super.onEnable();
    scoreboard.setContent(GoalLeaderboard.scoreboardContent(() -> getPoints(new AtomicInteger(), true)));
    scoreboard.show();
  }

  @Override
  protected void onDisable() {
    super.onDisable();
    scoreboard.hide();
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    super.loadGameState(document);

    Document scores = document.getDocument("scores");
    for (String key : scores.keys()) {
      try {
        UUID uuid = UUID.fromString(key);
        int value = scores.getInt(key);
        points.put(uuid, value);
      } catch (Exception ex) {
        Logger.error("Could not load scores for {}", key);
      }
    }
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    super.writeGameState(document);

    Document scores = document.getDocument("scores");
    points.forEach((uuid, points) -> scores.set(uuid.toString(), points));
  }

  @Override
  public void getWinnersOnEnd(@NotNull List<Player> winners) {
    GoalHelper.getWinnersOnEnd(winners, getPoints(new AtomicInteger(), false));
  }

  @NotNull
  protected Map<Player, Integer> getPoints(@NotNull AtomicInteger mostPoints, boolean zeros) {
    return GoalHelper.createPointsFromValues(mostPoints, points, (uuid, integer) -> integer, zeros);
  }

  protected void collect(@NotNull Player player) {
    collect(player, 1);
  }

  protected void collect(@NotNull Player player, int amount) {
    points.compute(player.getUniqueId(), (uuid, points) -> points == null ? amount : points + amount);
    scoreboard.update();
  }

  protected void setPoints(@NotNull UUID uuid, int amount) {
    points.put(uuid, amount);
    scoreboard.update();
  }

  protected void addPoints(@NotNull UUID uuid, int amount) {
    points.put(uuid, getPoints(uuid) + amount);
    scoreboard.update();
  }

  protected void removePoints(@NotNull UUID uuid, int amount) {
    points.put(uuid, getPoints(uuid) - amount);
    scoreboard.update();
  }

  protected int getPoints(@NotNull UUID uuid) {
    Integer points = this.points.get(uuid);
    return points == null ? 0 : points;
  }

}
