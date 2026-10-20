package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle;

import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets.BiomeTarget;
import net.cyperia.challenges.plugin.challenges.type.abstraction.ForceBattleGoal;
import net.cyperia.challenges.plugin.management.scheduler.policy.TimerPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.commons.common.config.Document;
import org.bukkit.block.Biome;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ForceBiomeBattleGoal extends ForceBattleGoal<BiomeTarget> {

  @Override
  protected BiomeTarget[] getTargetsPossibleToFind() {
    List<Biome> biomes = BiomeTarget.getPossibleBiomes();
    return biomes.stream().map(BiomeTarget::new).toArray(BiomeTarget[]::new);
  }

  @Override
  public BiomeTarget getTargetFromDocument(Document document, String path) {
    return BiomeTarget.fromSaved(Objects.requireNonNull(document.getObject(path)));
  }

  @Override
  public List<BiomeTarget> getListFromDocument(Document document, String path) {
    return document.getStringList(path).stream()
      .map(BiomeTarget::fromSaved)
      .collect(Collectors.toList());
  }

  @ScheduledTask(ticks = 5, async = false, timerPolicy = TimerPolicy.STARTED)
  public void checkBiomes() {
    if (!shouldExecuteEffect()) return;
    broadcastFiltered(player -> {
      BiomeTarget target = currentTarget.get(player.getUniqueId());
      if (target != null && target.check(player)) {
        handleTargetFound(player);
      }
    });
  }

}
