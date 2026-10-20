package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets;

import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.ExtremeForceBattleGoal;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.commons.common.collection.IRandom;
import net.cyperia.commons.common.collection.pair.Tuple;
import net.cyperia.commons.common.config.Document;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PositionTarget extends ForceTarget<Tuple<Double, Double>> {

  public PositionTarget(double x, double z) {
    super(Tuple.of(x, z));
  }

  public PositionTarget(Player player, int radius) {
    super(Tuple.of(
      player.getLocation().getBlockX() + (double) IRandom.singleton().range(-radius, radius),
      player.getLocation().getBlockZ() + (double) IRandom.singleton().range(-radius, radius)
    ));
  }

  public PositionTarget(Player player) {
    this(player, 1500);
  }

  @Override
  public boolean check(Player player) {
    Location playerLocation = player.getLocation().clone();
    playerLocation.setY(0);
    Location targetLocation = new Location(playerLocation.getWorld(), target.getFirst(), 0, target.getSecond());
    return playerLocation.distance(targetLocation) < 5;
  }

  @NotNull
  @Override
  public LocalizableMessage getDisplayName() {
    return getTargetMessageKey("coords").withArgs(target.getFirst(), target.getSecond());
  }

  @Override
  public ExtremeForceBattleGoal.TargetType getType() {
    return ExtremeForceBattleGoal.TargetType.POSITION;
  }

  @Override
  public Object getTargetSaveObject() {
    return Document.create().set("x", target.getFirst()).set("z", target.getSecond());
  }
}
