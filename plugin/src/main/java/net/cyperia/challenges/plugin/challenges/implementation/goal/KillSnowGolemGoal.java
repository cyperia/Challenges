package net.cyperia.challenges.plugin.challenges.implementation.goal;

import org.bukkit.entity.EntityType;
import net.cyperia.challenges.plugin.challenges.type.abstraction.KillEntityGoal;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

public class KillSnowGolemGoal extends KillEntityGoal {

  public KillSnowGolemGoal() {
    super(EntityType.SNOW_GOLEM);
    this.killerNeeded = true;
  }

  @NotNull
  @Override
  public SoundSample getStartSound() {
    return new SoundSample().addSound(Sound.ENTITY_SNOW_GOLEM_DEATH, 1);
  }

}
