package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle;

import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets.HeightTarget;
import net.cyperia.challenges.plugin.challenges.type.abstraction.ForceBattleGoal;
import net.cyperia.challenges.plugin.management.scheduler.policy.TimerPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.commons.common.config.Document;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class ForceHeightBattleGoal extends ForceBattleGoal<HeightTarget> {

  @Override
  protected HeightTarget[] getTargetsPossibleToFind() {
    return new HeightTarget[0]; //Not used
  }

  @Override
  protected HeightTarget getRandomTarget(Player player) {
    World world = player.getWorld();
    return new HeightTarget(globalRandom.range(world.getMinHeight(), world.getMaxHeight()));
  }

  @Override
  public HeightTarget getTargetFromDocument(Document document, String path) {
    return new HeightTarget(document.getInt(path));
  }

  @Override
  public List<HeightTarget> getListFromDocument(Document document, String path) {
    return document.getIntegerList(path).stream().map(HeightTarget::new).collect(Collectors.toList());
  }

  @Override
  protected boolean shouldRegisterDupedTargetsSetting() {
    return false;
  }

  @ScheduledTask(ticks = 5, async = false, timerPolicy = TimerPolicy.STARTED)
  public void checkHeights() {
    if (!shouldExecuteEffect()) return;
    broadcastFiltered(player -> {
      HeightTarget target = currentTarget.get(player.getUniqueId());
      if (target != null && target.check(player)) {
        handleTargetFound(player);
      }
    });
  }
}
