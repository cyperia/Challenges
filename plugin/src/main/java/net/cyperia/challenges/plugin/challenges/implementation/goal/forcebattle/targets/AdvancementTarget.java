package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets;

import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.ExtremeForceBattleGoal;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdvancementTarget extends ForceTarget<Advancement> {

  public AdvancementTarget(Advancement target) {
    super(target);
  }

  @Override
  public boolean check(Player player) {
    return player.getAdvancementProgress(target).isDone();
  }

  // Paper wraps the advancement in a new CraftAdvancement on every lookup, without equals
  public boolean matches(Advancement advancement) {
    return target.getKey().equals(advancement.getKey());
  }

  // Partial progress stays; a finished advancement is reopened, since PlayerAdvancementDoneEvent
  // fires only once per completion
  public void resetIfDone(Player player) {
    AdvancementProgress progress = player.getAdvancementProgress(target);
    if (!progress.isDone()) return;
    progress.getAwardedCriteria().forEach(progress::revokeCriteria);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof AdvancementTarget advancementTarget && matches(advancementTarget.target);
  }

  @Override
  public int hashCode() {
    return target.getKey().hashCode();
  }

  public static List<Advancement> getPossibleAdvancements() {
    List<Advancement> advancements = new ArrayList<>();
    Bukkit.getServer().advancementIterator().forEachRemaining(advancement -> {
      String string = advancement.getKey().toString();
      if (!string.contains(":recipes/") && !string.endsWith("root")) {
        advancements.add(advancement);
      }
    });
    return advancements;
  }

  @Override
  public ExtremeForceBattleGoal.TargetType getType() {
    return ExtremeForceBattleGoal.TargetType.ADVANCEMENT;
  }

  @Override
  public Object getTargetSaveObject() {
    return target.getKey().toString();
  }

}
