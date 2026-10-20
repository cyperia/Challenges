package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets;

import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.ExtremeForceBattleGoal;
import org.bukkit.entity.Player;

public class HeightTarget extends ForceTarget<Integer> {

  public HeightTarget(Integer target) {
    super(target);
  }

  @Override
  public boolean check(Player player) {
    return player.getLocation().getBlockY() == target;
  }

  @Override
  public ExtremeForceBattleGoal.TargetType getType() {
    return ExtremeForceBattleGoal.TargetType.HEIGHT;
  }

}
