package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle;

import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets.DamageTarget;
import net.cyperia.challenges.plugin.challenges.type.abstraction.ForceBattleGoal;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.commons.common.config.Document;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class ForceDamageBattleGoal extends ForceBattleGoal<DamageTarget> {

  @Override
  protected DamageTarget[] getTargetsPossibleToFind() {
    return new DamageTarget[0]; //Not used
  }

  @Override
  protected DamageTarget getRandomTarget(Player player) {
    return new DamageTarget(globalRandom.range(1, 19));
  }

  @Override
  public DamageTarget getTargetFromDocument(Document document, String path) {
    return new DamageTarget(document.getInt(path));
  }

  @Override
  public List<DamageTarget> getListFromDocument(Document document, String path) {
    return document.getIntegerList(path).stream().map(DamageTarget::new).collect(Collectors.toList());
  }

  @Override
  protected boolean shouldRegisterDupedTargetsSetting() {
    return false;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onDamage(@NotNull EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    if (!shouldExecuteEffect(player)) return;
    if (currentTarget.get(player.getUniqueId()) == null) return;
    DamageTarget target = currentTarget.get(player.getUniqueId());
    int damage = (int) ChallengeHelper.getFinalDamage(event);
    if (damage != target.getTarget()) return;
    handleTargetFound(player);
  }

}
