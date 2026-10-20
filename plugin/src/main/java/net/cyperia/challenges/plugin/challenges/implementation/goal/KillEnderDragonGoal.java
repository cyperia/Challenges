package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.challenges.type.abstraction.KillEntityGoal;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.World.Environment;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public class KillEnderDragonGoal extends KillEntityGoal {

  public KillEnderDragonGoal() {
    super(EntityType.ENDER_DRAGON, Environment.THE_END, true);
    setOneWinner(false);
  }

  @Nullable
  @Override
  public SoundSample getWinSound() {
    return null;
  }

}
