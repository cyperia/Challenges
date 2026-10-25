package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.challenges.type.abstraction.KillMobsGoal;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class KillAllMobsGoal extends KillMobsGoal {

  public KillAllMobsGoal() {
    super(getAllMobsToKill(), true);
  }

  static List<EntityType> getAllMobsToKill() {
    LinkedList<EntityType> list = new LinkedList<>(Arrays.asList(EntityType.values()));
    list.removeIf(type -> !type.isAlive());
    list.remove(EntityType.GIANT);
    list.remove(EntityType.ILLUSIONER);
    list.remove(EntityType.PLAYER);
    list.remove(EntityType.ARMOR_STAND);
    list.remove(EntityType.MANNEQUIN);
    return list;
  }

}
