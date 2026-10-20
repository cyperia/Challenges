package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.challenges.type.abstraction.KillMobsGoal;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class KillAllMonsterGoal extends KillMobsGoal {

  public KillAllMonsterGoal() {
    super(getAllMobsToKill());
  }

  static List<EntityType> getAllMobsToKill() {
    LinkedList<EntityType> list = new LinkedList<>(Arrays.asList(EntityType.values()));
    list.removeIf(type -> !type.isAlive());
    list.removeIf(type -> {
      assert type.getEntityClass() != null;
      return !Monster.class.isAssignableFrom(type.getEntityClass());
    });
    list.add(EntityType.PHANTOM);
    list.add(EntityType.ENDER_DRAGON);
    list.add(EntityType.SHULKER);
    list.add(EntityType.GHAST);
    list.add(EntityType.MAGMA_CUBE);
    list.add(EntityType.SLIME);
    list.remove(EntityType.GIANT);
    list.remove(EntityType.ILLUSIONER);
    list.add(EntityType.HOGLIN);
    return list;
  }

}
