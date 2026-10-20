package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets;

import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.ExtremeForceBattleGoal;
import net.cyperia.challenges.plugin.content.i18n.ArgumentFormat;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DamageTarget extends ForceTarget<Integer> {

  public DamageTarget(Integer target) {
    super(target);
  }

  @Override
  public boolean check(Player player) {
    return false;
  }

  /**
   * @implNote the target is counted in half hearts, which is what {@link ArgumentFormat#HEARTS} expects
   */
  @NotNull
  @Override
  public LocalizableMessage getDisplayName() {
    return ArgumentFormat.HEARTS.apply(target);
  }

  @Override
  public ExtremeForceBattleGoal.TargetType getType() {
    return ExtremeForceBattleGoal.TargetType.DAMAGE;
  }

}
