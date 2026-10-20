package net.cyperia.challenges.plugin.challenges.custom.settings.action;

import com.google.common.collect.Lists;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeExecutionData;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public interface IEntityTargetAction extends IChallengeAction {

  static List<Entity> getTargets(Entity triggerTarget, Map<String, String[]> subActions) {
    return getTargets(triggerTarget, subActions, SubSettingsHelper.TARGET_ENTITY);
  }

  static List<Entity> getTargets(Entity triggerTarget, Map<String, String[]> subActions, String key) {
    if (!subActions.containsKey(key)) {
      return Lists.newLinkedList();
    }
    String targetEntity = subActions.get(key)[0];

    switch (targetEntity) {
      case "random_player":
        List<Player> players = ChallengeAPI.getIngamePlayers();
        if (players.isEmpty()) return new LinkedList<>();
        return Collections.singletonList(players.get(random.nextInt(players.size())));
      case "every_player":
        return Lists.newLinkedList(ChallengeAPI.getIngamePlayers());
      case "current_player":
        return triggerTarget instanceof Player ? Lists.newArrayList(triggerTarget) : Lists.newLinkedList();
      case "every_mob":
        List<Entity> everyList = Lists.newLinkedList();
        for (World world : ChallengeAPI.getGameWorlds()) {
          everyList.addAll(world.getLivingEntities());
        }
        return everyList;
      case "every_mob_except_current":
        List<Entity> exceptList = Lists.newLinkedList();
        for (World world : ChallengeAPI.getGameWorlds()) {
          exceptList.addAll(world.getLivingEntities());
        }
        exceptList.remove(triggerTarget);
        return exceptList;
      case "every_mob_except_players":
        List<Entity> noPlayers = Lists.newLinkedList();
        for (World world : ChallengeAPI.getGameWorlds()) {
          for (LivingEntity entity : world.getLivingEntities()) {
            if (entity.getType() == EntityType.PLAYER) continue;
            noPlayers.add(entity);
          }
        }

        return noPlayers;
      case "console":
        return Collections.singletonList(null);
    }
    if (triggerTarget == null) {
      return Lists.newLinkedList();
    }
    return Lists.newArrayList(triggerTarget);
  }

  @Override
  default void execute(ChallengeExecutionData executionData, Map<String, String[]> subActions) {
    for (Entity target : getTargets(executionData.getEntity(), subActions)) {
      executeFor(target, subActions);
    }
  }

  void executeFor(Entity entity, Map<String, String[]> subActions);

}
