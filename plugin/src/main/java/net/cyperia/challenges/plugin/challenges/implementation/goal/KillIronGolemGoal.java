package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.challenges.type.abstraction.KillEntityGoal;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class KillIronGolemGoal extends KillEntityGoal {

  public KillIronGolemGoal() {
    super(EntityType.IRON_GOLEM);
    this.killerNeeded = true;
  }

  @NotNull
  @Override
  public SoundSample getStartSound() {
    return new SoundSample().addSound(Sound.ENTITY_IRON_GOLEM_HURT, 1);
  }

}
