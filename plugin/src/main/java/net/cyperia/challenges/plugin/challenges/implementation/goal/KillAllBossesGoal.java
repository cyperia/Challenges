package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.challenges.type.abstraction.KillMobsGoal;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class KillAllBossesGoal extends KillMobsGoal {

  public KillAllBossesGoal() {
    super(Arrays.asList(EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.ELDER_GUARDIAN));
  }

}
