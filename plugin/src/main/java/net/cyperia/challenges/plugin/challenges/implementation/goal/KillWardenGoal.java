package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.challenges.type.abstraction.KillEntityGoal;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class KillWardenGoal extends KillEntityGoal {

  public KillWardenGoal() {
    super(EntityType.WARDEN);
  }

  @NotNull
  @Override
  public SoundSample getStartSound() {
    return new SoundSample().addSound(Sound.ENTITY_WARDEN_EMERGE, 0.2f);
  }

}
