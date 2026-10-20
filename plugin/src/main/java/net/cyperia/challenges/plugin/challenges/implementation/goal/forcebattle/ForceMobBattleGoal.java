package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle;

import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets.MobTarget;
import net.cyperia.challenges.plugin.challenges.type.abstraction.ForceBattleDisplayGoal;
import net.cyperia.commons.common.config.Document;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class ForceMobBattleGoal extends ForceBattleDisplayGoal<MobTarget> {

  @Override
  protected MobTarget[] getTargetsPossibleToFind() {
    List<EntityType> entityTypes = MobTarget.getPossibleMobs();
    return entityTypes.stream().map(MobTarget::new).toArray(MobTarget[]::new);
  }

  @Override
  public MobTarget getTargetFromDocument(Document document, String path) {
    return new MobTarget(document.getEnum(path, EntityType.class));
  }

  @Override
  public List<MobTarget> getListFromDocument(Document document, String path) {
    return document.getEnumList(path, EntityType.class).stream().map(MobTarget::new).collect(Collectors.toList());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onKill(@NotNull EntityDeathEvent event) {
    LivingEntity entity = event.getEntity();
    Player killer = entity.getKiller();
    if (killer == null) return;
    if (!shouldExecuteEffect(killer)) return;
    if (currentTarget.get(killer.getUniqueId()) == null) return;
    if (entity.getType() == currentTarget.get(killer.getUniqueId()).getTarget()) {
      handleTargetFound(killer);
    }
  }

}
