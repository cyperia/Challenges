package net.cyperia.challenges.plugin.challenges.custom.settings.action;

import com.google.common.collect.Lists;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeExecutionData;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public interface IEntityTargetAction extends IChallengeAction {

  // The mobs of a world are only visible from the region they stand in, so those targets complete once every region was scanned
  static CompletableFuture<List<Entity>> getTargets(Entity triggerTarget, Map<String, String[]> subActions) {
    return getTargets(triggerTarget, subActions, SubSettingsHelper.TARGET_ENTITY);
  }

  static CompletableFuture<List<Entity>> getTargets(Entity triggerTarget, Map<String, String[]> subActions, String key) {
    if (!subActions.containsKey(key)) {
      return CompletableFuture.completedFuture(Lists.newLinkedList());
    }
    String targetEntity = subActions.get(key)[0];

    switch (targetEntity) {
      case "random_player":
        List<Player> players = ChallengeAPI.getIngamePlayers();
        if (players.isEmpty()) return CompletableFuture.completedFuture(new LinkedList<>());
        return CompletableFuture.completedFuture(Collections.singletonList(players.get(random.nextInt(players.size()))));
      case "every_player":
        return CompletableFuture.completedFuture(Lists.newLinkedList(ChallengeAPI.getIngamePlayers()));
      case "current_player":
        return CompletableFuture.completedFuture(triggerTarget instanceof Player ? Lists.newArrayList(triggerTarget) : Lists.newLinkedList());
      case "every_mob":
        return everyMob(entity -> true);
      case "every_mob_except_current":
        return everyMob(entity -> entity != triggerTarget);
      case "every_mob_except_players":
        return everyMob(entity -> entity.getType() != EntityType.PLAYER);
      case "console":
        return CompletableFuture.completedFuture(Collections.singletonList(null));
    }
    if (triggerTarget == null) {
      return CompletableFuture.completedFuture(Lists.newLinkedList());
    }
    return CompletableFuture.completedFuture(Lists.newArrayList(triggerTarget));
  }

  private static CompletableFuture<List<Entity>> everyMob(Predicate<LivingEntity> filter) {
    List<Entity> found = new CopyOnWriteArrayList<>();
    CompletableFuture<?>[] scans = ChallengeAPI.getGameWorlds().stream()
      .map(world -> Regions.forEachRegion(Challenges.getInstance(), world, () -> {
        for (LivingEntity entity : world.getLivingEntities()) {
          if (filter.test(entity)) found.add(entity);
        }
      }))
      .toArray(CompletableFuture<?>[]::new);
    return CompletableFuture.allOf(scans).thenApply(_ -> found);
  }

  @Override
  default void execute(ChallengeExecutionData executionData, Map<String, String[]> subActions) {
    getTargets(executionData.getEntity(), subActions).thenAccept(targets -> {
      for (Entity target : targets) {
        if (target == null) {
          executeFor(null, subActions);
          continue;
        }
        Regions.entity(Challenges.getInstance(), target, () -> executeFor(target, subActions));
      }
    });
  }

  void executeFor(Entity entity, Map<String, String[]> subActions);

}
