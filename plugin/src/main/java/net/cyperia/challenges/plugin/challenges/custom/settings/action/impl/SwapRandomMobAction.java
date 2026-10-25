package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeExecutionData;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.ChallengeAction;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.IEntityTargetAction;
import net.cyperia.challenges.plugin.challenges.implementation.challenge.randomizer.RandomTeleportOnHitChallenge;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import net.cyperia.commons.common.collection.IRandom;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SwapRandomMobAction extends ChallengeAction {

  public SwapRandomMobAction(String name) {
    super(name, SubSettingsHelper.createEntityTargetSettingsBuilder(true)
      .addChild(SubSettingsHelper.createEntityTargetSettingsBuilder(true).setKey("swap_targets")));
  }

  @Override
  public void execute(ChallengeExecutionData executionData, Map<String, String[]> subActions) {

    IEntityTargetAction.getTargets(executionData.getEntity(), subActions)
      .thenAcceptBoth(IEntityTargetAction.getTargets(executionData.getEntity(), subActions, "swap_targets"), this::swap);
  }

  private void swap(List<Entity> targets, List<Entity> swapTargets) {
    swapTargets.removeIf(entity -> !(entity instanceof LivingEntity));
    long seed = random.nextLong();
    Collections.shuffle(swapTargets, IRandom.create(seed).asRandom());
    Collections.shuffle(swapTargets, IRandom.create(seed).asRandom());
    for (World world : ChallengeAPI.getGameWorlds()) {
      for (Entity target : targets) {
        if (swapTargets.isEmpty()) break;
        if (!(target instanceof LivingEntity)) continue;
        if (target.getWorld() == world) {
          int i = targets.indexOf(target);
          int targetIndex = i + 1;
          if (targetIndex >= swapTargets.size()) targetIndex = 0;
          LivingEntity teleportTarget = (LivingEntity) swapTargets.remove(targetIndex);
          RandomTeleportOnHitChallenge.switchEntityLocations(teleportTarget, (LivingEntity) target);
        }
      }
    }

  }
}
